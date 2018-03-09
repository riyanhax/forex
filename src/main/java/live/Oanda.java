package live;

import broker.Account;
import broker.AccountID;
import broker.Context;
import broker.ForexBroker;
import broker.MarketOrderRequest;
import broker.OpenPositionRequest;
import broker.OrderCreateRequest;
import broker.OrderCreateResponse;
import broker.Price;
import broker.PricingGetRequest;
import broker.PricingGetResponse;
import broker.Quote;
import broker.StopLossDetails;
import broker.TakeProfitDetails;
import broker.TradeCloseRequest;
import broker.TradeCloseResponse;
import broker.TradeSpecifier;
import broker.TradeSummary;
import com.google.common.collect.Maps;
import market.AccountSnapshot;
import market.Instrument;
import market.InstrumentHistoryService;
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
import java.util.Map;
import java.util.Optional;

import static broker.Quote.doubleFromPippetes;
import static market.MarketTime.ZONE;
import static market.MarketTime.ZONE_UTC;

@Service
public class Oanda implements ForexBroker {

    private static final Logger LOG = LoggerFactory.getLogger(Oanda.class);
    private final InstrumentHistoryService service;
    private final Map<String, OandaTrader> tradersByAccountId;
    private final MarketTime clock;

    public Oanda(MarketTime clock, InstrumentHistoryService service, LiveTraders traders) {
        this.clock = clock;
        this.service = service;
        this.tradersByAccountId = Maps.uniqueIndex(traders.getTraders(), ForexTrader::getAccountNumber);
    }

    @Override
    public AccountSnapshot getAccountSnapshot(ForexTrader trader) throws Exception {
        LocalDateTime now = clock.now();

        Account account = getAccount(trader);

        return new AccountSnapshot(account, now);
    }

    @Override
    public Quote getQuote(ForexTrader trader, Instrument pair) throws Exception {
        String symbol = pair.getSymbol();

        AccountID accountId = new AccountID(trader.getAccountNumber());

        PricingGetRequest request = new PricingGetRequest(accountId, Collections.singleton(symbol));
        PricingGetResponse resp = getContext(trader).pricing().get(request);
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

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(new AccountID(trader.getAccountNumber()));
        orderCreateRequest.setOrder(marketOrderRequest);

        OrderCreateResponse orderCreateResponse = getContext(trader).order().create(orderCreateRequest);
        LOG.info(orderCreateResponse.toString());
    }

    @Override
    public void closePosition(ForexTrader trader, TradeSummary position, @Nullable Long limit) throws Exception {

        Instrument pair = Instrument.bySymbol.get(position.getInstrument()).getBrokerInstrument();

        Account account = getAccount(trader);
        List<TradeSummary> trades = account.getTrades();

        Optional<TradeSummary> tradeSummary = trades.stream()
                .filter(it -> it.getInstrument().equals(pair.getSymbol()))
                .findFirst();

        if (tradeSummary.isPresent()) {

            TradeSpecifier tradeSpecifier = new TradeSpecifier(tradeSummary.get());
            TradeCloseRequest closeRequest = new TradeCloseRequest(new AccountID(account.getId().getId()), tradeSpecifier);
            closeRequest.setUnits("ALL");

            TradeCloseResponse response = getContext(trader).trade().close(closeRequest);
            LOG.info(response.toString());
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

        for (ForexTrader trader : tradersByAccountId.values()) {
            trader.processUpdates(this);
        }
    }

    private Account getAccount(ForexTrader trader) throws Exception {
        return tradersByAccountId.get(trader.getAccountNumber()).getAccount();
    }

    private Context getContext(ForexTrader trader) throws Exception {
        return tradersByAccountId.get(trader.getAccountNumber()).getContext();
    }

    private static String roundToFiveDecimalPlaces(long value) {
        return "" + doubleFromPippetes(value);
    }
}
