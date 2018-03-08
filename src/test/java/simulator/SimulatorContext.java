package simulator;

import broker.Account;
import broker.AccountChangesRequest;
import broker.AccountChangesResponse;
import broker.AccountContext;
import broker.AccountGetResponse;
import broker.AccountID;
import broker.BidAsk;
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
import market.ForexPortfolio;
import market.ForexPortfolioValue;
import market.ForexPosition;
import market.ForexPositionValue;
import market.Instrument;
import market.InstrumentHistoryService;
import market.MarketEngine;
import market.MarketTime;
import market.OHLC;
import market.OrderListener;
import market.order.BuyMarketOrder;
import market.order.OrderRequest;
import market.order.Orders;
import market.order.SellMarketOrder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static broker.Quote.doubleFromPippetes;
import static market.InstrumentHistoryService.DATE_TIME_FORMATTER;

public class SimulatorContext implements Context, OrderListener {

    private final MarketTime clock;
    private final MarketEngine marketEngine;
    private final Simulation simulation;
    private final InstrumentHistoryService instrumentHistoryService;
    private final Map<String, ForexPortfolio> mostRecentPortfolio = new HashMap<>();
    private final Map<String, AccountID> accountIdsByOrderId = new HashMap<>();

    @Override
    public void orderCancelled(OrderRequest filled) {

    }

    @Override
    public void orderFilled(OrderRequest filled) {
        Instrument instrument = filled.getInstrument();
        long commission = (filled.isBuyOrder() ? -1 : 1) * halfSpread(instrument);
        long price = filled.getExecutionPrice().get() + commission;

        AccountID accountID = accountIdsByOrderId.remove(filled.getId());
        ForexPortfolio oldPortfolio = mostRecentPortfolio.get(accountID.getId());

        ImmutableMap<Instrument, ForexPosition> positionsByInstrument = Maps.uniqueIndex(oldPortfolio.getPositions(), ForexPosition::getInstrument);
        Map<Instrument, ForexPosition> newPositions = new HashMap<>(positionsByInstrument);
        ForexPosition existingPosition = positionsByInstrument.get(instrument);
        long newPipsProfit = oldPortfolio.getPipettesProfit();
        SortedSet<ForexPositionValue> closedTrades = new TreeSet<>(oldPortfolio.getClosedTrades());

        if (filled.isSellOrder()) {
            Objects.requireNonNull(existingPosition, "Go long on the inverse pair, instead of shorting the primary pair.");

            ForexPositionValue closedTrade = new ForexPositionValue(existingPosition, clock.now(), price);
            newPipsProfit += closedTrade.pipettes();

            newPositions.remove(instrument);
            closedTrades.add(closedTrade);
        } else if (filled.isBuyOrder()) {
            Preconditions.checkArgument(existingPosition == null, "Shouldn't have more than one position open for a pair at a time!");
            Preconditions.checkArgument(!positionsByInstrument.containsKey(instrument.getOpposite()), "Shouldn't have more than one position open for a pair at a time!");

            newPositions.put(instrument, new ForexPosition(clock.now(), instrument, Stance.LONG, price));
        }

        ForexPortfolio portfolio = new ForexPortfolio(newPipsProfit, new HashSet<>(newPositions.values()), closedTrades);
        mostRecentPortfolio.put(accountID.getId(), portfolio);
    }

    private class SimulatorPricingContext implements PricingContext {
        @Override
        public PricingGetResponse get(PricingGetRequest request) throws RequestException {
            // TODO: Should handle multiple instruments
            String instrument = request.getInstruments().iterator().next();
            Instrument pair = Instrument.bySymbol.get(instrument);

            long price = marketEngine.getPrice(pair);
            long halfSpread = halfSpread(pair);

            List<Price> prices = new ArrayList<>();
            prices.add(new Price(doubleFromPippetes(price - halfSpread), doubleFromPippetes(price + halfSpread)));

            return new PricingGetResponse(prices);
        }
    }

    private class SimulatorOrderContext implements OrderContext {
        @Override
        public OrderCreateResponse create(OrderCreateRequest request) throws RequestException {

            // Open a long position on USD/EUR to simulate a short position for EUR/USD
            MarketOrderRequest marketOrder = request.getOrder();
            Instrument pair = Instrument.bySymbol.get(marketOrder.getInstrument());

            BuyMarketOrder order = Orders.buyMarketOrder(marketOrder.getUnits(), pair);
            OrderRequest submitted = marketEngine.submit(SimulatorContext.this, order);
            accountIdsByOrderId.put(submitted.getId(), request.getAccountID());

            return new OrderCreateResponse();
        }
    }

    private class SimulatorTradeContext implements TradeContext {
        @Override
        public TradeCloseResponse close(TradeCloseRequest request) throws RequestException {
            ForexPosition position = mostRecentPortfolio(request.getAccountID()).getPositions().iterator().next();
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

        private TransactionID getLatestTransactionId(AccountID accountID) {
            SortedSet<ForexPositionValue> closedTrades = mostRecentPortfolio(accountID).getClosedTrades();
            return new TransactionID(closedTrades.isEmpty() ? "EMPTY" : closedTrades.last().getTimestamp().toString());
        }

        @Override
        public AccountGetResponse get(AccountID accountID) throws RequestException {
            TransactionID latestTransactionId = getLatestTransactionId(accountID);

            ForexPortfolio portfolio = mostRecentPortfolio(accountID);
            ForexPortfolioValue portfolioValue = portfolioValue(portfolio);

            List<TradeSummary> trades = portfolioValue.getPositionValues().stream().map(it ->
                    new TradeSummary(it.getInstrument().getSymbol(), 1, doubleFromPippetes(it.getPrice()),
                            doubleFromPippetes(it.pipettes()), it.getTimestamp().format(DATE_TIME_FORMATTER) + "Z",
                            it.getTimestamp().toString())).collect(Collectors.toList());

            Account account = new Account(accountID, latestTransactionId,
                    trades, doubleFromPippetes(portfolioValue.getPipettesProfit()));

            return new AccountGetResponse(account);
        }

        private ForexPortfolioValue portfolioValue(ForexPortfolio portfolio) {
            Set<ForexPosition> positions = portfolio.getPositions();
            Set<ForexPositionValue> positionValues = positionValues(positions);

            return new ForexPortfolioValue(portfolio, clock.now(), positionValues);
        }

        private Set<ForexPositionValue> positionValues(Set<ForexPosition> positions) {
            return positions.stream()
                    .map(this::positionValue)
                    .collect(Collectors.toSet());
        }

        private ForexPositionValue positionValue(ForexPosition position) {
            Instrument pair = position.getInstrument();
            long price = marketEngine.getPrice(pair);
            long halfSpread = halfSpread(pair);

            BidAsk bidAsk = new BidAsk(price - halfSpread, price + halfSpread);
            return new ForexPositionValue(position, clock.now(), position.getStance() == Stance.LONG ? bidAsk.getBid() : bidAsk.getAsk());
        }
    }

    private class SimulatorInstrumentContext implements InstrumentContext {
        @Override
        public InstrumentCandlesResponse candles(InstrumentCandlesRequest request) throws RequestException {

            List<Candlestick> candlesticks = new ArrayList<>();

            Instrument pair = Instrument.bySymbol.get(request.getInstrument());
            LocalDateTime from = LocalDateTime.parse(request.getFrom(), DATE_TIME_FORMATTER.withZone(MarketTime.ZONE));
            LocalDateTime to = LocalDateTime.parse(request.getTo(), DATE_TIME_FORMATTER.withZone(MarketTime.ZONE));

            try {
                NavigableMap<LocalDateTime, OHLC> fourHourCandles = instrumentHistoryService.getFourHourCandles(pair, Range.closed(from, to));
                fourHourCandles.forEach((time, ohlc) ->
                        candlesticks.add(new Candlestick(time.format(DATE_TIME_FORMATTER), null,
                                new CandlestickData(ohlc.open, ohlc.high, ohlc.low, ohlc.close), null)));
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

    private ForexPortfolio mostRecentPortfolio(AccountID accountID) {
        return mostRecentPortfolio.getOrDefault(accountID.getId(),
                new ForexPortfolio(0, Collections.emptySet(), Collections.emptySortedSet()));
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

    private long halfSpread(Instrument pair) {
        return (simulation.pippeteSpread / 2);
    }
}
