package live;

import broker.Account;
import broker.AccountID;
import broker.CandlestickData;
import broker.CandlestickGranularity;
import broker.Context;
import broker.ForexBroker;
import broker.InstrumentCandlesRequest;
import broker.InstrumentCandlesResponse;
import broker.MarketOrderRequest;
import broker.OpenPositionRequest;
import broker.OrderCreateRequest;
import broker.OrderCreateResponse;
import broker.Price;
import broker.PricingGetRequest;
import broker.PricingGetResponse;
import broker.Quote;
import broker.RequestException;
import broker.StopLossDetails;
import broker.TakeProfitDetails;
import broker.TradeCloseRequest;
import broker.TradeCloseResponse;
import broker.TradeSpecifier;
import broker.TradeSummary;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import market.AccountSnapshot;
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
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static broker.CandlePrice.ASK;
import static broker.CandlePrice.BID;
import static broker.CandlePrice.MID;
import static broker.Quote.invert;
import static java.time.DayOfWeek.FRIDAY;
import static java.util.Collections.singleton;
import static java.util.EnumSet.of;
import static market.MarketTime.END_OF_TRADING_DAY_HOUR;
import static market.MarketTime.ZONE;
import static market.MarketTime.ZONE_UTC;

@Service
public class Oanda implements ForexBroker {

    private static final Logger LOG = LoggerFactory.getLogger(Oanda.class);
    private final Map<String, OandaTrader> tradersByAccountId;
    private final MarketTime clock;

    public Oanda(MarketTime clock, LiveTraders traders) {
        this.clock = clock;
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
        AccountID accountId = new AccountID(trader.getAccountNumber());

        PricingGetRequest request = new PricingGetRequest(accountId, singleton(pair));
        PricingGetResponse resp = getContext(trader).getPricing(request);
        List<Price> prices = resp.getPrices();

        if (prices.isEmpty()) {
            throw new IllegalStateException("Prices were empty!");
        }

        Price price = prices.iterator().next();
        LOG.info("Current price for {}: {}", pair, price.toString());

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

        Quote quote = getQuote(trader, pair);

        MarketOrderRequest marketOrderRequest = createMarketOrderRequest(quote, pair, request.getUnits(),
                request.getStopLoss().orElse(null),
                request.getTakeProfit().orElse(null));

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(new AccountID(trader.getAccountNumber()));
        orderCreateRequest.setOrder(marketOrderRequest);

        OrderCreateResponse orderCreateResponse = getContext(trader).createOrder(orderCreateRequest);
        LOG.info(orderCreateResponse.toString());
    }

    static MarketOrderRequest createMarketOrderRequest(Quote quote, Instrument instrument, int units, @Nullable Long stopLoss, @Nullable Long takeProfit) {
        // Inverse instruments base stop-losses and take-profits from the ask, since they "buy" when closing the position
        boolean inverse = instrument.isInverse();
        long basePrice = inverse ? quote.getAsk() : quote.getBid();

        MarketOrderRequest marketOrderRequest = new MarketOrderRequest();
        marketOrderRequest.setInstrument(instrument);
        marketOrderRequest.setUnits(units);

        if (stopLoss != null) {
            // Can't seem to get the prices right for inverse positions without converting back and forth
            long price = inverse ? invert(invert(basePrice) + stopLoss)
                    : basePrice - stopLoss;

            StopLossDetails sl = new StopLossDetails();
            sl.setPrice(price);
            marketOrderRequest.setStopLossOnFill(sl);
        }

        if (takeProfit != null) {
            // Can't seem to get the prices right for inverse positions without converting back and forth
            long price = inverse ? invert(invert(basePrice) - takeProfit)
                    : basePrice + takeProfit;

            TakeProfitDetails tp = new TakeProfitDetails();
            tp.setPrice(price);
            marketOrderRequest.setTakeProfitOnFill(tp);
        }

        return marketOrderRequest;
    }

    @Override
    public void closePosition(ForexTrader trader, TradeSummary position, @Nullable Long limit) throws Exception {

        TradeSpecifier tradeSpecifier = new TradeSpecifier(position);
        TradeCloseRequest closeRequest = new TradeCloseRequest(getAccount(trader).getId(), tradeSpecifier);
        closeRequest.setUnits(position.getCurrentUnits());

        TradeCloseResponse response = getContext(trader).closeTrade(closeRequest);
        LOG.info(response.toString());
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

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getOneDayCandles(ForexTrader trader, Instrument pair, Range<LocalDateTime> closed) throws RequestException {
        return getCandles(trader, CandlestickGranularity.D, closed, pair);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getFourHourCandles(ForexTrader trader, Instrument pair, Range<LocalDateTime> closed) throws RequestException {
        return getCandles(trader, CandlestickGranularity.H4, closed, pair);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getOneWeekCandles(ForexTrader trader, Instrument pair, Range<LocalDateTime> closed) throws RequestException {
        return getCandles(trader, CandlestickGranularity.W, closed, pair);
    }

    private NavigableMap<LocalDateTime, CandlestickData> getCandles(ForexTrader trader, CandlestickGranularity granularity, Range<LocalDateTime> closed, Instrument pair) throws RequestException {

        LocalDateTime exclusiveEnd = closed.upperEndpoint();

        InstrumentCandlesRequest request = new InstrumentCandlesRequest(pair);
        request.setPrice(of(BID, MID, ASK));
        request.setGranularity(granularity);
        request.setFrom(closed.lowerEndpoint());
        request.setTo(exclusiveEnd);
        request.setIncludeFirst(true);

        if (granularity == CandlestickGranularity.D) {
            request.setAlignmentTimezone(MarketTime.ZONE);
            request.setDailyAlignment(END_OF_TRADING_DAY_HOUR);
        } else if (granularity == CandlestickGranularity.W) {
            request.setWeeklyAlignment(FRIDAY);
        }

        InstrumentCandlesResponse response = getContext(trader).instrumentCandles(request);

        NavigableMap<LocalDateTime, CandlestickData> data = new TreeMap<>();

        response.getCandles().forEach(it -> {
            LocalDateTime timestamp = it.getTime();
            if (timestamp.equals(exclusiveEnd)) { // Force exclusive endpoint behavior
                return;
            }
            CandlestickData c = it.getMid();

            data.put(timestamp, c);
        });
        return data;
    }

    private Account getAccount(ForexTrader trader) throws RequestException {
        return tradersByAccountId.get(trader.getAccountNumber()).getAccount();
    }

    private Context getContext(ForexTrader trader) {
        return tradersByAccountId.get(trader.getAccountNumber()).getContext();
    }
}
