package simulator;

import broker.Account;
import broker.AccountChangesRequest;
import broker.AccountChangesResponse;
import broker.AccountContext;
import broker.AccountGetResponse;
import broker.AccountID;
import broker.Candlestick;
import broker.CandlestickData;
import broker.Context;
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

public class SimulatorContext implements Context, OrderListener {

    private final MarketTime clock;
    private final MarketEngine marketEngine;
    private final Simulation simulation;
    private final InstrumentHistoryService instrumentHistoryService;
    private final Map<String, Account> mostRecentPortfolio = new HashMap<>();
    private final Map<String, AccountID> accountIdsByOrderId = new HashMap<>();
    private final Map<String, SortedSet<TradeSummary>> closedTrades = new HashMap<>();

    @Override
    public void orderCancelled(OrderRequest filled) {

    }

    @Override
    public void orderFilled(OrderRequest filled) {
        Instrument instrument = filled.getInstrument();
        long executionPrice = filled.getExecutionPrice().get();
        long price = adjustPriceForSpread(executionPrice, instrument, filled.isBuyOrder() ? Stance.LONG : Stance.SHORT);

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

            SortedSet<TradeSummary> closedTradesForAccount = closedTrades.get(accountID.getId());
            if (closedTradesForAccount == null) {
                closedTradesForAccount = new TreeSet<>();
            }
            closedTradesForAccount.add(closedTrade);
            closedTrades.put(accountID.getId(), closedTradesForAccount);

            long expectedPipettes = closedTradesForAccount.stream()
                    .mapToLong(TradeSummary::getRealizedProfitLoss)
                    .sum();
            Preconditions.checkArgument(expectedPipettes == newPipsProfit);

        } else if (filled.isBuyOrder()) {
            Preconditions.checkArgument(existingPosition == null, "Shouldn't have more than one position open for a pair at a time!");
            Preconditions.checkArgument(!positionsByInstrument.containsKey(instrument.getOpposite()), "Shouldn't have more than one position open for a pair at a time!");

            newPositions.put(instrument, positionValue(new TradeSummary(
                    instrument, 1, price,
                    0L, 0L, now, null, now.toString())));
        }

        List<TradeSummary> tradeSummaries = new ArrayList<>(newPositions.values());
        Account account = new Account(accountID, getLatestTransactionId(accountID),
                tradeSummaries, newPipsProfit);

        mostRecentPortfolio.put(accountID.getId(), account);
    }

    private class SimulatorPricingContext implements PricingContext {
        @Override
        public PricingGetResponse get(PricingGetRequest request) throws RequestException {
            // TODO: Should handle multiple instruments
            Instrument pair = request.getInstruments().iterator().next();

            long price = marketEngine.getPrice(pair);
            long bid = adjustPriceForSpread(price, pair, Stance.LONG);
            long ask = adjustPriceForSpread(price, pair, Stance.SHORT);

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
            OrderRequest submitted = marketEngine.submit(SimulatorContext.this, order);
            accountIdsByOrderId.put(submitted.getId(), request.getAccountID());

            return new OrderCreateResponse();
        }
    }

    private class SimulatorTradeContext implements TradeContext {
        @Override
        public TradeCloseResponse close(TradeCloseRequest request) throws RequestException {
            TradeSummary position = mostRecentPortfolio(request.getAccountID()).getTrades().iterator().next();
            SellMarketOrder order = Orders.sellMarketOrder(1, position.getInstrument());
            OrderRequest submitted = marketEngine.submit(SimulatorContext.this, order);
            accountIdsByOrderId.put(submitted.getId(), request.getAccountID());

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
        Stance stance = position.getCurrentUnits() > 0 ? Stance.LONG : Stance.SHORT;
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

            try {
                NavigableMap<LocalDateTime, CandlestickData> fourHourCandles = instrumentHistoryService.getFourHourCandles(pair, Range.closed(from, to));
                fourHourCandles.forEach((time, ohlc) ->
                        candlesticks.add(new Candlestick(time, null, null, ohlc)));
            } catch (Exception e) {
                throw new RequestException(e.getMessage(), e);
            }

            return new InstrumentCandlesResponse(request.getInstrument(), request.getGranularity(), candlesticks);
        }
    }

    public SimulatorContext(MarketTime clock, InstrumentHistoryService instrumentHistoryService,
                            MarketEngine marketEngine, Simulation simulation) {
        this.clock = clock;
        this.instrumentHistoryService = instrumentHistoryService;
        this.marketEngine = marketEngine;
        this.simulation = simulation;
    }

    SortedSet<TradeSummary> closedTradesForAccountId(String id) {
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
        return stance == Stance.LONG ? bid : ask;
    }

    private long halfSpread(Instrument pair) {
        return (simulation.pippeteSpread / 2);
    }
}