package forex.simulator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import forex.broker.Account;
import forex.broker.AccountChangesRequest;
import forex.broker.AccountChangesResponse;
import forex.broker.AccountChangesState;
import forex.broker.AccountContext;
import forex.broker.AccountGetResponse;
import forex.broker.AccountSummary;
import forex.broker.BaseContext;
import forex.broker.CalculatedTradeState;
import forex.broker.Candlestick;
import forex.broker.CandlestickData;
import forex.broker.CandlestickGranularity;
import forex.broker.InstrumentCandlesRequest;
import forex.broker.InstrumentCandlesResponse;
import forex.broker.InstrumentContext;
import forex.broker.LimitOrderRequest;
import forex.broker.LimitOrderTransaction;
import forex.broker.MarketOrderTransaction;
import forex.broker.OrderContext;
import forex.broker.OrderCreateRequest;
import forex.broker.OrderCreateResponse;
import forex.broker.Price;
import forex.broker.PricingContext;
import forex.broker.PricingGetRequest;
import forex.broker.PricingGetResponse;
import forex.broker.RequestException;
import forex.broker.Stance;
import forex.broker.Trade;
import forex.broker.TradeCloseRequest;
import forex.broker.TradeCloseResponse;
import forex.broker.TradeContext;
import forex.broker.TradeListRequest;
import forex.broker.TradeListResponse;
import forex.broker.TradeSpecifier;
import forex.broker.TradeState;
import forex.broker.TradeStateFilter;
import forex.broker.TradeSummary;
import forex.market.AccountSnapshot;
import forex.market.CandleTimeFrame;
import forex.market.Instrument;
import forex.market.InstrumentHistoryService;
import forex.market.MarketEngine;
import forex.market.MarketTime;
import forex.market.OrderListener;
import forex.market.order.BuyMarketOrder;
import forex.market.order.OrderRequest;
import forex.market.order.Orders;
import forex.market.order.SellMarketOrder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static forex.broker.Quote.pippetesFromDouble;
import static forex.market.MarketTime.formatTimestamp;
import static forex.market.order.Orders.buyLimitOrder;
import static forex.market.order.Orders.buyMarketOrder;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

class SimulatorContextImpl extends BaseContext implements OrderListener, SimulatorContext {

    private final MarketTime clock;
    private final MarketEngine marketEngine;
    private final SimulatorProperties simulatorProperties;
    private final InstrumentHistoryService instrumentHistoryService;
    private final SequenceService sequenceService;
    private final TradeService tradeService;

    private final Map<String, String> accountIdsByOrderId = new HashMap<>();
    private final Map<String, TraderData> traderDataById = new HashMap<>();
    private final Map<String, forex.broker.OrderRequest> stopLossTakeProfitsById = new HashMap<>();
    private final Map<String, AccountChangesResponse> accountChangesById = new HashMap<>();

    @Override
    public boolean isAvailable() {
        return marketEngine.isAvailable();
    }

    @Override
    public void beforeTraders() throws RequestException {
        // Update prices and process any limit/stop orders
        marketEngine.processUpdates();

        boolean orderSubmitted = false;
        for (AccountSummary account : tradeService.accounts()) {
            orderSubmitted |= handleStopLossTakeProfits(account);
        }

        // Process any stop loss / take profits
        if (orderSubmitted) {
            marketEngine.processUpdates();
        }
    }

    @Override
    public void afterTraders() {
        // Process any submitted orders
        marketEngine.processUpdates();

        tradeService.accounts().forEach(account -> {
            getTraderData(account.getId()).addSnapshot(accountSnapshot(account));
        });
    }

    @Override
    public void orderCancelled(OrderRequest filled) {

    }

    @Override
    public void orderFilled(OrderRequest filled) {
        Instrument instrument = filled.getInstrument();
        long executionPrice = filled.getExecutionPrice().get();
        long price = adjustPriceForSpread(executionPrice, instrument, filled.isBuyOrder() ? Stance.LONG : Stance.SHORT);

        Preconditions.checkArgument((filled.isBuyOrder() && price > executionPrice) ||
                filled.isSellOrder() && price < executionPrice, "The spread doesn't seem to be used correctly!");

        String accountID = accountIdsByOrderId.remove(filled.getId());
        AccountSummary oldPortfolio = mostRecentPortfolio(accountID);

        ImmutableMap<Instrument, TradeSummary> positionsByInstrument = Maps.uniqueIndex(oldPortfolio.getTrades(),
                TradeSummary::getInstrument);
        TradeSummary existingPosition = positionsByInstrument.get(instrument);

        LocalDateTime now = clock.now();
        Integer transactionId = sequenceService.nextAccountTransactionID(accountID);

        AccountSummary account;

        if (filled.isSellOrder()) {
            Objects.requireNonNull(existingPosition, "Go long on the inverse pair, instead of shorting the primary pair.");
            Preconditions.checkArgument(existingPosition.getCurrentUnits() == filled.getUnits(), "Partial position closes aren't supported!");

            long opened = existingPosition.getPrice();
            long profitLoss = (price - opened) * filled.getUnits();

            TradeSummary closedTrade = new TradeSummary(
                    existingPosition.getTradeId(), existingPosition.getAccountId(), existingPosition.getInstrument(), existingPosition.getPrice(), existingPosition.getOpenTime(),
                    existingPosition.getCurrentUnits(), 0, profitLoss, 0L, now);

            NavigableMap<LocalDateTime, CandlestickData> candles;
            try {
                Range<LocalDateTime> tradeTime = Range.closed(existingPosition.getOpenTime(), now);
                candles = instrumentHistoryService.getOneMinuteCandles(instrument, tradeTime);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            account = oldPortfolio.positionClosed(closedTrade, getLatestTransactionId(accountID));
            List<TradeSummary> openTrades = account.getTrades();

            AccountChangesResponse accountChangesResponse = stagedAccountChanges(accountID);
            accountChangesById.put(accountID, accountChangesResponse.tradeClosed(transactionId, closedTrade, new AccountChangesState(account.getNetAssetValue(),
                    openTrades.stream().mapToLong(TradeSummary::getUnrealizedProfitLoss).sum(),
                    CalculatedTradeState.fromAll(openTrades))
            ));

            TradeHistory history = new TradeHistory(closedTrade, candles);

            SortedSet<TradeHistory> closedTradesForAccount = tradeService.tradeClosed(accountID, history);

            long expectedPipettes = closedTradesForAccount.stream()
                    .mapToLong(TradeHistory::getRealizedProfitLoss)
                    .sum();
            Preconditions.checkArgument(expectedPipettes == account.getProfitLoss());
        } else {
            Preconditions.checkArgument(filled.isBuyOrder(), "What other type of order was it?");
            Preconditions.checkArgument(existingPosition == null, "Shouldn't have more than one position open for a pair at a time!");
            Preconditions.checkArgument(!positionsByInstrument.containsKey(instrument.getOpposite()), "Shouldn't have more than one position open for a pair at a time!");

            TradeSummary filledPosition = positionValue(new TradeSummary(
                    filled.getId(), accountID, instrument, price, now, filled.getUnits(), filled.getUnits(), 0L, 0L, null));

            account = oldPortfolio.positionOpened(filledPosition, getLatestTransactionId(accountID));
            List<TradeSummary> openTrades = account.getTrades();

            AccountChangesResponse accountChangesResponse = stagedAccountChanges(accountID);
            accountChangesById.put(accountID, accountChangesResponse.tradeOpened(transactionId, stopLossTakeProfitsById.get(accountID),
                    filledPosition, new AccountChangesState(account.getNetAssetValue(),
                    openTrades.stream().mapToLong(TradeSummary::getUnrealizedProfitLoss).sum(),
                    CalculatedTradeState.fromAll(openTrades))
            ));

            Preconditions.checkArgument(filledPosition.getUnrealizedProfitLoss() == -simulatorProperties.getPippeteSpread(),
                    "Immediately after filling a position it should have an unrealized loss of the spread!");

        }

        tradeService.update(account);
    }

    private AccountChangesResponse stagedAccountChanges(String accountID) {

        accountChangesById.computeIfAbsent(accountID, it -> {
            AccountSummary account = mostRecentPortfolio(accountID);
            List<TradeSummary> openTrades = account.getTrades();
            AccountChangesState state = new AccountChangesState(account.getNetAssetValue(),
                    openTrades.stream().mapToLong(TradeSummary::getUnrealizedProfitLoss).sum(),
                    CalculatedTradeState.fromAll(openTrades)
            );

            return AccountChangesResponse.empty(getLatestTransactionId(accountID), state);
        });

        return accountChangesById.get(accountID);
    }

    private class SimulatorPricingContext implements PricingContext {
        @Override
        public PricingGetResponse get(PricingGetRequest request) throws RequestException {

            List<Price> prices = request.getInstruments().stream()
                    .distinct()
                    .map(it -> {
                        long price = marketEngine.getPrice(it);
                        long bid = adjustPriceForSpread(price, it, Stance.SHORT);
                        long ask = adjustPriceForSpread(price, it, Stance.LONG);

                        return new Price(it, bid, ask);
                    })
                    .collect(toList());

            return new PricingGetResponse(prices);
        }
    }

    private class SimulatorOrderContext implements OrderContext {
        @Override
        public OrderCreateResponse create(OrderCreateRequest request) throws RequestException {

            forex.broker.OrderRequest requestedOrder = request.getOrder();
            boolean limit = false;
            if (requestedOrder == null) {
                requestedOrder = request.getLimitOrder();
                limit = true;
            }
            Instrument pair = requestedOrder.getInstrument();
            int units = requestedOrder.getUnits();

            String accountID = request.getAccountID();
            LimitOrderTransaction limitOrderTransaction = null;
            MarketOrderTransaction marketOrderTransaction = null;
            OrderRequest submitted;
            if (limit) {
                long price = ((LimitOrderRequest) requestedOrder).getPrice();
                submitted = marketEngine.submit(SimulatorContextImpl.this,
                        buyLimitOrder(units, pair, price));

                limitOrderTransaction = new LimitOrderTransaction(submitted.getId(), accountID,
                        submitted.getSubmissionDate(), submitted.getInstrument(), submitted.getUnits(), price);
            } else {
                BuyMarketOrder order = buyMarketOrder(units, pair);
                submitted = marketEngine.submit(SimulatorContextImpl.this, order);

                marketOrderTransaction = new MarketOrderTransaction(submitted.getId(), accountID,
                        submitted.getSubmissionDate(), submitted.getInstrument(), submitted.getUnits());
            }

            accountIdsByOrderId.put(submitted.getId(), accountID);
            stopLossTakeProfitsById.put(accountID, requestedOrder);

            return new OrderCreateResponse(pair, marketOrderTransaction, limitOrderTransaction, null, null);
        }
    }

    private class SimulatorTradeContext implements TradeContext {
        @Override
        public TradeCloseResponse close(TradeCloseRequest request) throws RequestException {
            String requestedCloseId = request.getTradeSpecifier().getId();

            String accountID = request.getAccountID();
            TradeSummary position = mostRecentPortfolio(accountID).getTrades().iterator().next();
            String positionId = position.getTradeId();

            Preconditions.checkArgument(requestedCloseId.equals(positionId),
                    "Trade with id [%s] not found.  Found [%s]", requestedCloseId, positionId);

            SellMarketOrder order = Orders.sellMarketOrder(position.getCurrentUnits(), position.getInstrument());
            OrderRequest submitted = marketEngine.submit(SimulatorContextImpl.this, order);
            accountIdsByOrderId.put(submitted.getId(), accountID);
            stopLossTakeProfitsById.remove(accountID);

            MarketOrderTransaction orderCreateTransaction = new MarketOrderTransaction(submitted.getId(), accountID,
                    submitted.getSubmissionDate(), submitted.getInstrument(), submitted.getUnits());

            return new TradeCloseResponse(orderCreateTransaction);
        }

        @Override
        public TradeListResponse list(TradeListRequest request) throws RequestException {

            if (request.getCount() > 500) {
                throw new RequestException("Invalid request: A maximum of 500 trades can be retrieved");
            }

            String accountID = request.getAccountID();

            SortedSet<Trade> trades = new TreeSet<>(Comparator.comparing(Trade::getOpenTime).reversed());

            TradeStateFilter filter = request.getFilter();
            if (filter == TradeStateFilter.CLOSE_WHEN_TRADEABLE) {
                throw new UnsupportedOperationException("Unsupported filter: " + filter);
            }

            if (filter == TradeStateFilter.CLOSED || filter == null) {

                // TradeHistory should be using Trade and not TradeSummary
                trades.addAll(closedTradesForAccountId(accountID)
                        .stream()
                        .map(it ->
                                new Trade(it.getId(), accountID, it.getInstrument(), it.getTrade().getPrice(), it.getOpenTime(), TradeState.CLOSED,
                                        it.getTrade().getInitialUnits(), 0, it.getRealizedProfitLoss(), 0L, 0L, it.getCandles().lastEntry().getValue().getO(),
                                        emptyList(), 0L, it.getTrade().getCloseTime())
                        )
                        .collect(toList()));
            }

            if (filter == TradeStateFilter.OPEN || filter == null) {
                trades.addAll(tradeService.getOpenTradesForAccountID(accountID)
                        .stream()
                        .map(Trade::new)
                        .collect(toList()));
            }

            List<Trade> result = new ArrayList<>(trades);

            Set<String> tradeIds = request.getTradeIds();
            if (!tradeIds.isEmpty()) {
                result.removeIf(it -> !tradeIds.contains(it.getTradeId()));
            }

            if (request.getCount() > 0) {
                result = result.subList(0, Math.min(request.getCount(), result.size()));
            }

            return new TradeListResponse(result, result.isEmpty() ? null : getLatestTransactionId(accountID));
        }
    }

    private class SimulatorAccountContext implements AccountContext {
        @Override
        public AccountChangesResponse changes(AccountChangesRequest request) throws RequestException {

            String accountID = request.getAccountID();
            String latestTransactionId = getLatestTransactionId(accountID);

            if (latestTransactionId.equals(request.getSinceTransactionID())) {
                TraderData traderData = getTraderData(accountID);
                AccountSnapshot mostRecentSnapshot = traderData.getMostRecentPortfolio();
                AccountSummary account = mostRecentSnapshot.getAccount();

                return AccountChangesResponse.empty(latestTransactionId,
                        new AccountChangesState(account.getNetAssetValue(),
                                mostRecentSnapshot.unrealizedProfitAndLoss(),
                                CalculatedTradeState.fromAll(account.getTrades())));
            }

            AccountChangesResponse stagedChanges = stagedAccountChanges(accountID);
            accountChangesById.remove(accountID);

            return stagedChanges;
        }

        @Override
        public AccountGetResponse get(String accountID) throws RequestException {
            return new AccountGetResponse(getTraderData(accountID)
                    .getMostRecentPortfolio().getAccount());
        }
    }

    private String getLatestTransactionId(String accountID) {
        return sequenceService.getLatestTransactionId(accountID).toString();
    }

    private AccountSnapshot accountSnapshot(AccountSummary account) {
        List<TradeSummary> newTradeValues = positionValues(account.getTrades());

        Account newAccount = new Account(account.getId(), account.getBalance(), account.getLastTransactionID(),
                account.getProfitLoss());
        // TODO: Add support for pending orders
        AccountSummary summary = new AccountSummary(newAccount, newTradeValues, forex.broker.Orders.empty());

        return new AccountSnapshot(summary, clock.now());
    }

    private List<TradeSummary> positionValues(List<TradeSummary> positions) {
        return positions.stream()
                .map(this::positionValue)
                .collect(toList());
    }

    private TradeSummary positionValue(TradeSummary position) {
        Instrument instrument = position.getInstrument();
        long price = marketEngine.getPrice(instrument);
        // If the trade is currently long, then we are quoting a SHORT for selling
        Stance stance = position.getCurrentUnits() > 0 ? Stance.SHORT : Stance.LONG;
        long currentPrice = adjustPriceForSpread(price, instrument, stance);
        long unrealizedProfitLoss = currentPrice - position.getPrice();

        return new TradeSummary(position.getTradeId(), position.getAccountId(), instrument, position.getPrice(), position.getOpenTime(), position.getInitialUnits(),
                position.getCurrentUnits(), 0L, unrealizedProfitLoss, null);
    }

    private class SimulatorInstrumentContext implements InstrumentContext {
        @Override
        public InstrumentCandlesResponse candles(InstrumentCandlesRequest request) throws RequestException {

            List<Candlestick> candlesticks = new ArrayList<>();

            Instrument pair = request.getInstrument();
            CandlestickGranularity granularity = request.getGranularity();
            LocalDateTime from = request.getFrom();
            LocalDateTime to = request.getTo();
            int count = request.getCount();

            if (to == null) {
                if (count < 0) {
                    throw new RequestException("Invalid value specified for 'count'");
                } else if (count == 0) {
                    return new InstrumentCandlesResponse(pair, granularity, candlesticks);
                }
                CandleTimeFrame timeFrame = CandleTimeFrame.from(request.getGranularity());
                to = from;

                for (int i = 0; i < count - 1; i++) {
                    to = timeFrame.nextCandle(to);
                }
            } else if (count > 0) {
                throw new RequestException("'count' cannot be specified when 'to' and 'from' parameters are set");
            }

            Range<LocalDateTime> range = Range.closed(from, to);

            LocalDateTime now = clock.now();

            NavigableMap<LocalDateTime, CandlestickData> data;
            try {
                Preconditions.checkArgument(!from.isAfter(now), "Can't request candles after the current minute! Now: %s, Request From: %s",
                        formatTimestamp(now), formatTimestamp(from));

                if (CandlestickGranularity.H4.equals(granularity)) {
                    data = instrumentHistoryService.getFourHourCandles(pair, range);
                } else if (CandlestickGranularity.D.equals(granularity)) {
                    data = instrumentHistoryService.getOneDayCandles(pair, range);
                } else if (CandlestickGranularity.W.equals(granularity)) {
                    data = instrumentHistoryService.getOneWeekCandles(pair, range);
                } else if (CandlestickGranularity.M1.equals(granularity)) {
                    data = instrumentHistoryService.getOneMinuteCandles(pair, range);
                } else {
                    throw new UnsupportedOperationException("Need to support granularity: " + granularity);
                }
                data.forEach((time, ohlc) -> {
                    long halfSpread = simulatorProperties.getPippeteSpread() / 2;
                    CandlestickData bid = new CandlestickData(ohlc.getO() - halfSpread, ohlc.getH() - halfSpread, ohlc.getL() - halfSpread, ohlc.getC() - halfSpread);
                    CandlestickData ask = new CandlestickData(ohlc.getO() + halfSpread, ohlc.getH() + halfSpread, ohlc.getL() + halfSpread, ohlc.getC() + halfSpread);

                    candlesticks.add(new Candlestick(time, bid, ask, ohlc));
                });
            } catch (Exception e) {
                throw new RequestException(e.getMessage(), e);
            }

            return new InstrumentCandlesResponse(pair, granularity, candlesticks);
        }
    }

    SimulatorContextImpl(MarketTime clock, InstrumentHistoryService instrumentHistoryService,
                         SequenceService sequenceService, TradeService tradeService,
                         MarketEngine marketEngine, SimulatorProperties simulatorProperties) {
        this.clock = clock;
        this.instrumentHistoryService = instrumentHistoryService;
        this.sequenceService = sequenceService;
        this.tradeService = tradeService;
        this.marketEngine = marketEngine;
        this.simulatorProperties = simulatorProperties;
    }

    @Override
    public SortedSet<TradeHistory> closedTradesForAccountId(String id) {
        return tradeService.getClosedTradesForAccountID(id);
    }

    private AccountSummary mostRecentPortfolio(String id) {
        return tradeService.getAccount(id, it -> {
            long balance = pippetesFromDouble(simulatorProperties.getAccountBalanceDollars());

            return new AccountSummary(new Account.Builder(id)
                    .withBalance(balance)
                    .withLastTransactionID(getLatestTransactionId(id))
                    .build(), emptyList(), forex.broker.Orders.empty());
        });
    }

    @Override
    protected PricingContext pricing() {
        return new SimulatorPricingContext();
    }

    @Override
    protected OrderContext order() {
        return new SimulatorOrderContext();
    }

    @Override
    protected TradeContext trade() {
        return new SimulatorTradeContext();
    }

    @Override
    protected AccountContext account() {
        return new SimulatorAccountContext();
    }

    @Override
    protected InstrumentContext instrument() {
        return new SimulatorInstrumentContext();
    }

    private long adjustPriceForSpread(long price, Instrument instrument, Stance stance) {
        long halfSpread = halfSpread(instrument);

        long bid = price - halfSpread;
        long ask = price + halfSpread;

        // Selling gets the bid price, buying gets the ask price.
        return stance == Stance.LONG ? ask : bid;
    }

    private long halfSpread(Instrument pair) {
        return (simulatorProperties.getPippeteSpread() / 2);
    }

    /*
     * All of this logic should be moved to be handled with orders in the market.
     * @param trader
     * @return true if an order was submitted
     */
    private boolean handleStopLossTakeProfits(AccountSummary account) throws RequestException {
        String id = account.getId();
        forex.broker.OrderRequest openedPosition = stopLossTakeProfitsById.get(id);

        if (openedPosition != null) {
            AccountSnapshot accountSnapshot = accountSnapshot(account);
            List<TradeSummary> positions = accountSnapshot.getPositionValues();
            if (positions.isEmpty()) { // Limit order that hasn't been filled yet
                return false;
            }

            TradeSummary positionValue = positions.iterator().next();
            long pipsProfit = positionValue.getUnrealizedProfitLoss();

            // Close once we've lost or gained enough pipettes or if it's noon Friday
            long stopLossPrice = openedPosition.getStopLossOnFill().getPrice();
            long takeProfitPrice = openedPosition.getTakeProfitOnFill().getPrice();

            long stopLoss = openedPosition.getUnits() > 0 ? positionValue.getPrice() - stopLossPrice :
                    stopLossPrice - positionValue.getPrice();
            long takeProfit = openedPosition.getUnits() > 0 ? takeProfitPrice - positionValue.getPrice() :
                    positionValue.getPrice() - takeProfitPrice;

            if (pipsProfit < -stopLoss || pipsProfit > takeProfit) {
                trade().close(new TradeCloseRequest(account.getId(), new TradeSpecifier(positionValue.getTradeId())));
                stopLossTakeProfitsById.remove(id);
                return true;
            }
        }
        return false;
    }

    @Override
    public TraderData getTraderData(String accountNumber) {
        traderDataById.computeIfAbsent(accountNumber, it -> {
            AccountSummary account = mostRecentPortfolio(accountNumber);

            TraderData traderData = new TraderData();
            traderData.addSnapshot(accountSnapshot(account));

            return traderData;
        });
        return traderDataById.get(accountNumber);
    }
}
