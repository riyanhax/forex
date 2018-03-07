package live;

import broker.ForexBroker;
import broker.OpenPositionRequest;
import broker.Quote;
import broker.Stance;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.oanda.v20.Context;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.Account;
import com.oanda.v20.account.AccountChangesRequest;
import com.oanda.v20.account.AccountChangesResponse;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.Price;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeCloseResponse;
import com.oanda.v20.trade.TradeSpecifier;
import com.oanda.v20.trade.TradeSummary;
import com.oanda.v20.transaction.StopLossDetails;
import com.oanda.v20.transaction.TakeProfitDetails;
import com.oanda.v20.transaction.TransactionID;
import market.ForexPortfolio;
import market.ForexPortfolioValue;
import market.ForexPosition;
import market.ForexPositionValue;
import market.Instrument;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import trader.ForexTrader;

import javax.annotation.Nullable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import static broker.Quote.doubleFromPippetes;
import static broker.Quote.pippetesFromDouble;
import static java.util.Collections.emptySortedSet;
import static market.MarketTime.ZONE;
import static market.MarketTime.ZONE_UTC;

@Service
class Oanda implements ForexBroker {

    private static final Logger LOG = LoggerFactory.getLogger(Oanda.class);
    private final OandaHistoryService service;
    private final List<OandaTrader> traders;
    private final SystemTime clock;
    private final Context ctx;
    private final LoadingCache<String, Account> accounts = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Account>() {
                @Override
                public Account load(String id) throws Exception {
                    Stopwatch timer = Stopwatch.createStarted();

                    AccountID accountId = new AccountID(id);
                    Account account = ctx.account.get(accountId).getAccount();

                    LOG.info("Loaded account {} in {}", accountId, timer);
                    return account;
                }
            });

    public Oanda(SystemTime clock, OandaProperties properties, OandaHistoryService service, LiveTraders traders) {
        this.clock = clock;
        this.ctx = new Context(properties.getApi().getEndpoint(), properties.getApi().getToken());
        this.service = service;
        this.traders = traders.getTraders();
    }

    @Override
    public ForexPortfolioValue getPortfolioValue(ForexTrader trader) throws Exception {
        LocalDateTime now = clock.now();

        Account account = getAccount(trader.getAccountNumber());

        Set<ForexPositionValue> positionValues = account.getTrades().stream()
                .map(it -> {
                    Instrument pair = Instrument.bySymbol.get(it.getInstrument().toString())
                            .getBrokerInstrument();
                    double units = it.getCurrentUnits().doubleValue();
                    boolean inverse = units < 0d;

                    long price = pippetesFromDouble(inverse ? (1 / it.getPrice().doubleValue()) :
                            it.getPrice().doubleValue());

                    long pl = pippetesFromDouble(it.getUnrealizedPL().doubleValue());
                    long currentPrice = price + pl;

                    ZonedDateTime utcOpened = service.parseToZone(it.getOpenTime(), ZONE_UTC);
                    LocalDateTime localOpened = utcOpened.withZoneSameInstant(ZONE).toLocalDateTime();

                    ForexPosition position = new ForexPosition(localOpened, pair, Stance.LONG, price);

                    return new ForexPositionValue(position, now, currentPrice);
                }).collect(Collectors.toSet());

        Set<ForexPosition> positions = positionValues.stream().map(ForexPositionValue::getPosition).collect(Collectors.toSet());
        SortedSet<ForexPositionValue> closedTrades = emptySortedSet();

        ForexPortfolio portfolio = new ForexPortfolio(pippetesFromDouble(account.getPl().doubleValue()), positions, closedTrades);

        return new ForexPortfolioValue(portfolio, now, positionValues);
    }

    @Override
    public Quote getQuote(ForexTrader trader, Instrument pair) throws Exception {
        String symbol = pair.getSymbol();

        AccountID accountId = new AccountID(trader.getAccountNumber());

        PricingGetRequest request = new PricingGetRequest(accountId, Collections.singletonList(symbol));
        PricingGetResponse resp = ctx.pricing.get(request);
        List<Price> prices = resp.getPrices();

        if (prices.isEmpty()) {
            throw new IllegalStateException("Prices were empty!");
        }

        Price price = prices.iterator().next();
        LOG.info("Current price for {}: {}", symbol, price.toString());

        return new OandaQuote(price);
    }

    @Override
    public boolean isClosed() {
        LocalDateTime now = clock.now();
        if (ALWAYS_OPEN_DAYS.contains(now.getDayOfWeek())) {
            return false;
        }

        ZonedDateTime utcNow = ZonedDateTime.of(now, MarketTime.ZONE).withZoneSameInstant(ZONE_UTC);
        DayOfWeek dayOfWeek = utcNow.getDayOfWeek();

        boolean dst = ZONE.getRules().isDaylightSavings(utcNow.toInstant());

        return dayOfWeek == DayOfWeek.SATURDAY || (dayOfWeek == DayOfWeek.SUNDAY && utcNow.getHour() < (dst ? 20 : 21)) ||
                (dayOfWeek == DayOfWeek.FRIDAY && utcNow.getHour() > (dst ? 21 : 22));
    }

    @Override
    public boolean isClosed(LocalDate time) {
        return time.getDayOfWeek() == DayOfWeek.SATURDAY || time.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    @Override
    public void openPosition(ForexTrader trader, OpenPositionRequest request) throws Exception {
        Instrument pair = request.getPair();

        boolean shorting = pair.isInverse();
        if (shorting) {
            pair = pair.getOpposite();
        }

        Quote quote = getQuote(trader, pair);
        String symbol = pair.getSymbol();
        long basePrice = shorting ? quote.getAsk() : quote.getBid();

        MarketOrderRequest marketOrderRequest = new MarketOrderRequest();
        marketOrderRequest.setInstrument(symbol);
        marketOrderRequest.setUnits(shorting ? -1 : 1);

        request.getStopLoss().ifPresent(stop -> {
            StopLossDetails stopLoss = new StopLossDetails();
            stopLoss.setPrice(roundToFiveDecimalPlaces(basePrice - stop * (shorting ? -1 : 1)));
            marketOrderRequest.setStopLossOnFill(stopLoss);
        });

        request.getTakeProfit().ifPresent(profit -> {
            TakeProfitDetails takeProfit = new TakeProfitDetails();
            takeProfit.setPrice(roundToFiveDecimalPlaces(basePrice + profit * (shorting ? -1 : 1)));
            marketOrderRequest.setTakeProfitOnFill(takeProfit);
        });

        AccountID accountId = new AccountID(trader.getAccountNumber());
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(accountId);
        orderCreateRequest.setOrder(marketOrderRequest);

        try {
            OrderCreateResponse orderCreateResponse = ctx.order.create(orderCreateRequest);
            LOG.info(orderCreateResponse.toString());
        } catch (RequestException e) {
            throw new Exception(e.getErrorMessage(), e);
        }
    }

    @Override
    public void closePosition(ForexTrader trader, ForexPosition position, @Nullable Long limit) throws Exception {

        Instrument pair = position.getInstrument().getBrokerInstrument();

        Account account = getAccount(trader.getAccountNumber());
        List<TradeSummary> trades = account.getTrades();

        Optional<TradeSummary> tradeSummary = trades.stream()
                .filter(it -> it.getInstrument().toString().equals(pair.getSymbol()))
                .findFirst();

        if (tradeSummary.isPresent()) {

            TradeSpecifier tradeSpecifier = new TradeSpecifier(tradeSummary.get().getId());
            TradeCloseRequest closeRequest = new TradeCloseRequest(account.getId(), tradeSpecifier);
            closeRequest.setUnits("ALL");

            try {
                TradeCloseResponse response = ctx.trade.close(closeRequest);
                LOG.info(response.toString());
            } catch (RequestException e) {
                throw new Exception(e.getErrorMessage(), e);
            }
        } else {
            LOG.error("Didn't find a matching trade with Oanda! Position: {}  Oanda Trades: {}", position, trades);
        }
    }

    @Override
    public void processUpdates() throws Exception {
        if (isClosed()) {
            LOG.info("Market is closed.");
            return;
        }

        for (ForexTrader trader : traders) {
            trader.processUpdates(this);
        }
    }

    private Account getAccount(String id) throws Exception {
        Account account = accounts.get(id);

        AccountChangesRequest request = new AccountChangesRequest(account.getId());
        request.setSinceTransactionID(account.getLastTransactionID());

        AccountChangesResponse changes = this.ctx.account.changes(request);
        TransactionID lastTransactionID = changes.getLastTransactionID();

        if (lastTransactionID.equals(account.getLastTransactionID())) {
            return account;
        }

        // TODO: Could merge the changes here, but for now it's easiest to just retrieve a new account instance
        accounts.invalidate(id);
        return accounts.get(id);
    }

    private static String roundToFiveDecimalPlaces(long value) {
        return "" + doubleFromPippetes(value);
    }
}
