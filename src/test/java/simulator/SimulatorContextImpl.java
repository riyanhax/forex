package simulator;

import broker.Account;
import broker.AccountChangesRequest;
import broker.AccountChangesResponse;
import broker.AccountContext;
import broker.AccountGetResponse;
import broker.AccountID;
import broker.Candlestick;
import broker.CandlestickData;
import broker.CandlestickGranularity;
import broker.InstrumentCandlesRequest;
import broker.InstrumentCandlesResponse;
import broker.InstrumentContext;
import broker.MarketOrderRequest;
import broker.OrderContext;
import broker.OrderCreateRequest;
import broker.OrderCreateResponse;
import broker.Price;
import broker.PricingContext;
import broker.PricingGetRequest;
import broker.PricingGetResponse;
import broker.RequestException;
import broker.Stance;
import broker.TradeCloseRequest;
import broker.TradeCloseResponse;
import broker.TradeContext;
import broker.TradeSpecifier;
import broker.TradeSummary;
import broker.TransactionID;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import market.AccountSnapshot;
import market.Instrument;
import market.InstrumentHistoryService;
import market.MarketEngine;
import market.MarketTime;
import market.OrderListener;
import market.order.BuyMarketOrder;
import market.order.OrderRequest;
import market.order.Orders;
import market.order.SellMarketOrder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

class SimulatorContextImpl implements OrderListener, SimulatorContext {

    private final MarketTime clock;
    private final MarketEngine marketEngine;
    private final SimulatorProperties simulatorProperties;
    private final InstrumentHistoryService instrumentHistoryService;
    private final Map<String, Account> mostRecentPortfolio = new HashMap<>();
    private final Map<String, AccountID> accountIdsByOrderId = new HashMap<>();
    private final Map<String, SortedSet<TradeHistory>> closedTrades = new HashMap<>();
    private final Map<String, TraderData> traderDataById = new HashMap<>();
    private final Map<AccountID, MarketOrderRequest> stopLossTakeProfitsById = new HashMap<>();

    @Override
    public boolean isAvailable() {
        return marketEngine.isAvailable();
    }

    @Override
    public void beforeTraders() throws RequestException {
        // Update prices and process any limit/stop orders
        marketEngine.processUpdates();

        for (Account account : mostRecentPortfolio.values()) {
            handleStopLossTakeProfits(account);
        }
    }

    @Override
    public void afterTraders() {
        // Process any submitted orders
        marketEngine.processUpdates();
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

        AccountID accountID = accountIdsByOrderId.remove(filled.getId());
        Account oldPortfolio = mostRecentPortfolio(accountID);

        ImmutableMap<Instrument, TradeSummary> positionsByInstrument = Maps.uniqueIndex(oldPortfolio.getTrades(),
                TradeSummary::getInstrument);
        Map<Instrument, TradeSummary> newPositions = new HashMap<>(positionsByInstrument);
        TradeSummary existingPosition = positionsByInstrument.get(instrument);
        long newPipsProfit = oldPortfolio.getPl();

        LocalDateTime now = clock.now();
        if (filled.isSellOrder()) {
            Objects.requireNonNull(existingPosition, "Go long on the inverse pair, instead of shorting the primary pair.");

            long opened = existingPosition.getPrice();
            long profitLoss = price - opened;

            TradeSummary closedTrade = new TradeSummary(
                    existingPosition.getInstrument(),
                    existingPosition.getCurrentUnits(),
                    existingPosition.getPrice(),
                    profitLoss,
                    0L,
                    existingPosition.getOpenTime(),
                    now,
                    now.toString());

            newPipsProfit += closedTrade.getRealizedProfitLoss();

            newPositions.remove(instrument);

            NavigableMap<LocalDateTime, CandlestickData> candles;
            try {
                Range<LocalDateTime> tradeTime = Range.closed(existingPosition.getOpenTime(), now);
                candles = instrumentHistoryService.getOneMinuteCandles(instrument, tradeTime);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            TradeHistory history = new TradeHistory(closedTrade, candles);

            SortedSet<TradeHistory> closedTradesForAccount = closedTrades.get(accountID.getId());
            if (closedTradesForAccount == null) {
                closedTradesForAccount = new TreeSet<>();
            }
            closedTradesForAccount.add(history);
            closedTrades.put(accountID.getId(), closedTradesForAccount);

            long expectedPipettes = closedTradesForAccount.stream()
                    .mapToLong(TradeHistory::getRealizedProfitLoss)
                    .sum();
            Preconditions.checkArgument(expectedPipettes == newPipsProfit);

        } else if (filled.isBuyOrder()) {
            Preconditions.checkArgument(existingPosition == null, "Shouldn't have more than one position open for a pair at a time!");
            Preconditions.checkArgument(!positionsByInstrument.containsKey(instrument.getOpposite()), "Shouldn't have more than one position open for a pair at a time!");

            TradeSummary filledPosition = positionValue(new TradeSummary(
                    instrument, 1, price,
                    0L, 0L, now, null, now.toString()));

            Preconditions.checkArgument(filledPosition.getUnrealizedPL() == -simulatorProperties.getPippeteSpread(),
                    "Immediately after filling a position it should have an unrealized loss of the spread!");

            newPositions.put(instrument, filledPosition);
        }

        List<TradeSummary> tradeSummaries = new ArrayList<>(newPositions.values());
        Account account = new Account(accountID, getLatestTransactionId(accountID),
                tradeSummaries, newPipsProfit);

        mostRecentPortfolio.put(accountID.getId(), account);
        getTraderData(accountID.getId()).addSnapshot(accountSnapshot(account));
    }

    private class SimulatorPricingContext implements PricingContext {
        @Override
        public PricingGetResponse get(PricingGetRequest request) throws RequestException {
            // TODO: Should handle multiple instruments
            Instrument pair = request.getInstruments().iterator().next();

            long price = marketEngine.getPrice(pair);
            long bid = adjustPriceForSpread(price, pair, Stance.SHORT);
            long ask = adjustPriceForSpread(price, pair, Stance.LONG);

            List<Price> prices = new ArrayList<>();
            prices.add(new Price(bid, ask));

            return new PricingGetResponse(prices);
        }
    }

    private class SimulatorOrderContext implements OrderContext {
        @Override
        public OrderCreateResponse create(OrderCreateRequest request) throws RequestException {

            // Open a long position on USD/EUR to simulate a short position for EUR/USD
            MarketOrderRequest marketOrder = request.getOrder();
            Instrument pair = marketOrder.getInstrument();
            int units = marketOrder.getUnits();

            if (units < 0) {
                pair = pair.getOpposite();
                units = abs(units);
            }

            BuyMarketOrder order = Orders.buyMarketOrder(units, pair);
            OrderRequest submitted = marketEngine.submit(SimulatorContextImpl.this, order);
            AccountID accountID = request.getAccountID();
            accountIdsByOrderId.put(submitted.getId(), accountID);
            stopLossTakeProfitsById.put(accountID, marketOrder);

            return new OrderCreateResponse();
        }
    }

    private class SimulatorTradeContext implements TradeContext {
        @Override
        public TradeCloseResponse close(TradeCloseRequest request) throws RequestException {
            AccountID accountID = request.getAccountID();
            TradeSummary position = mostRecentPortfolio(accountID).getTrades().iterator().next();
            SellMarketOrder order = Orders.sellMarketOrder(1, position.getInstrument());
            OrderRequest submitted = marketEngine.submit(SimulatorContextImpl.this, order);
            accountIdsByOrderId.put(submitted.getId(), accountID);
            stopLossTakeProfitsById.remove(accountID);

            return new TradeCloseResponse();
        }
    }

    private class SimulatorAccountContext implements AccountContext {
        @Override
        public AccountChangesResponse changes(AccountChangesRequest request) throws RequestException {
            return new AccountChangesResponse(getLatestTransactionId(request.getAccountID()));
        }

        @Override
        public AccountGetResponse get(AccountID accountID) throws RequestException {
            TransactionID latestTransactionId = getLatestTransactionId(accountID);

            Account account = mostRecentPortfolio(accountID);
            AccountSnapshot accountSnapshot = accountSnapshot(account);

            List<TradeSummary> trades = accountSnapshot.getPositionValues();

            return new AccountGetResponse(new Account(accountID, latestTransactionId,
                    new ArrayList<>(trades),
                    accountSnapshot.getPipettesProfit()));
        }
    }

    private TransactionID getLatestTransactionId(AccountID accountID) {
        String transactionId = clock.now().toString();
        return new TransactionID(transactionId);
    }

    private AccountSnapshot accountSnapshot(Account account) {
        Account newAccount = new Account(account.getId(), account.getLastTransactionID(),
                positionValues(account.getTrades()), account.getPl());

        return new AccountSnapshot(newAccount, clock.now());
    }

    private List<TradeSummary> positionValues(List<TradeSummary> positions) {
        return positions.stream()
                .map(this::positionValue)
                .collect(Collectors.toList());
    }

    private TradeSummary positionValue(TradeSummary position) {
        Instrument instrument = position.getInstrument();
        long price = marketEngine.getPrice(instrument);
        // If the trade is currently long, then we are quoting a SHORT for selling
        Stance stance = position.getCurrentUnits() > 0 ? Stance.SHORT : Stance.LONG;
        long currentPrice = adjustPriceForSpread(price, instrument, stance);
        long unrealizedProfitLoss = currentPrice - position.getPrice();

        return new TradeSummary(instrument,
                position.getCurrentUnits(),
                position.getPrice(),
                0L,
                unrealizedProfitLoss,
                position.getOpenTime(),
                null,
                clock.now().toString());
    }

    private class SimulatorInstrumentContext implements InstrumentContext {
        @Override
        public InstrumentCandlesResponse candles(InstrumentCandlesRequest request) throws RequestException {

            List<Candlestick> candlesticks = new ArrayList<>();

            Instrument pair = request.getInstrument();
            LocalDateTime from = request.getFrom();
            LocalDateTime to = request.getTo();
            Range<LocalDateTime> range = Range.closed(from, to);
            CandlestickGranularity granularity = request.getGranularity();

            Preconditions.checkArgument(!to.isAfter(clock.now()), "Can't request candles after the current minute!");

            NavigableMap<LocalDateTime, CandlestickData> data;
            try {
                if (CandlestickGranularity.H4.equals(granularity)) {
                    data = instrumentHistoryService.getFourHourCandles(pair, range);
                } else if (CandlestickGranularity.D.equals(granularity)) {
                    data = instrumentHistoryService.getOneDayCandles(pair, range);
                } else {
                    throw new UnsupportedOperationException("Need to support granularity: " + granularity);
                }
                data.forEach((time, ohlc) -> candlesticks.add(new Candlestick(time, null, null, ohlc)));
            } catch (Exception e) {
                throw new RequestException(e.getMessage(), e);
            }

            return new InstrumentCandlesResponse(pair, granularity, candlesticks);
        }
    }

    SimulatorContextImpl(MarketTime clock, InstrumentHistoryService instrumentHistoryService,
                         MarketEngine marketEngine, SimulatorProperties simulatorProperties) {
        this.clock = clock;
        this.instrumentHistoryService = instrumentHistoryService;
        this.marketEngine = marketEngine;
        this.simulatorProperties = simulatorProperties;
    }

    @Override
    public SortedSet<TradeHistory> closedTradesForAccountId(String id) {
        return closedTrades.get(id);
    }

    private Account mostRecentPortfolio(AccountID accountID) {
        return mostRecentPortfolio.getOrDefault(accountID.getId(),
                new Account(accountID, getLatestTransactionId(accountID),
                        Collections.emptyList(), 0));
    }

    @Override
    public PricingContext pricing() {
        return new SimulatorPricingContext();
    }

    @Override
    public OrderContext order() {
        return new SimulatorOrderContext();
    }

    @Override
    public TradeContext trade() {
        return new SimulatorTradeContext();
    }

    @Override
    public AccountContext account() {
        return new SimulatorAccountContext();
    }

    @Override
    public InstrumentContext instrument() {
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
     */
    private void handleStopLossTakeProfits(Account account) throws RequestException {
        AccountID id = account.getId();
        MarketOrderRequest openedPosition = stopLossTakeProfitsById.get(id);

        if (openedPosition != null) {
            AccountSnapshot accountSnapshot = accountSnapshot(account);
            List<TradeSummary> positions = accountSnapshot.getPositionValues();

            TradeSummary positionValue = positions.iterator().next();
            long pipsProfit = positionValue.getUnrealizedPL();

            // Close once we've lost or gained enough pipettes or if it's noon Friday
            long stopLossPrice = openedPosition.getStopLossOnFill().getPrice();
            long takeProfitPrice = openedPosition.getTakeProfitOnFill().getPrice();

            long stopLoss = openedPosition.getUnits() > 0 ? positionValue.getPrice() - stopLossPrice :
                    stopLossPrice - positionValue.getPrice();
            long takeProfit = openedPosition.getUnits() > 0 ? takeProfitPrice - positionValue.getPrice() :
                    positionValue.getPrice() - takeProfitPrice;

            if (pipsProfit < -stopLoss || pipsProfit > takeProfit) {
                trade().close(new TradeCloseRequest(account.getId(), new TradeSpecifier(positionValue.getId())));
                stopLossTakeProfitsById.remove(id);
            }
        }
    }

    @Override
    public TraderData getTraderData(String accountNumber) {
        TraderData traderData = traderDataById.get(accountNumber);
        if (traderData == null) {
            traderData = new TraderData();
            traderDataById.put(accountNumber, traderData);
        }

        return traderData;
    }
}
