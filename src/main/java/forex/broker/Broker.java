package forex.broker;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import forex.market.AccountSnapshot;
import forex.market.Instrument;
import forex.market.InstrumentDataRetriever;
import forex.market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import forex.trader.ForexTrader;

import javax.annotation.Nullable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import static forex.broker.CandlePrice.ASK;
import static forex.broker.CandlePrice.BID;
import static forex.broker.CandlePrice.MID;
import static forex.broker.CandlestickGranularity.D;
import static forex.broker.CandlestickGranularity.W;
import static forex.broker.Quote.invert;
import static forex.market.MarketTime.ZONE;
import static java.util.Collections.singleton;
import static java.util.EnumSet.of;
import static forex.market.MarketTime.END_OF_TRADING_DAY_HOUR;
import static forex.market.MarketTime.WEEKLY_ALIGNMENT;

@Service
public class Broker implements ForexBroker {

    private static final Logger LOG = LoggerFactory.getLogger(Broker.class);
    private final Map<String, ForexTrader> tradersByAccountId;
    private final MarketTime clock;
    private final InstrumentDataRetriever instrumentDataRetriever;

    public Broker(MarketTime clock, LiveTraders traders, InstrumentDataRetriever instrumentDataRetriever) {
        this.clock = clock;
        this.tradersByAccountId = Maps.uniqueIndex(traders.getTraders(), ForexTrader::getAccountNumber);
        this.instrumentDataRetriever = instrumentDataRetriever;
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

        return new BrokerQuote(price);
    }

    @Override
    public boolean isClosed() {
        LocalDateTime now = clock.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();

        if (ALWAYS_OPEN_DAYS.contains(now.getDayOfWeek())) {
            return false;
        }

        return dayOfWeek == DayOfWeek.SATURDAY || (dayOfWeek == DayOfWeek.SUNDAY && now.getHour() < 16) ||
                (dayOfWeek == DayOfWeek.FRIDAY && now.getHour() > 15);
    }

    @Override
    public void openPosition(ForexTrader trader, OpenPositionRequest request) throws Exception {
        Preconditions.checkArgument(request.getUnits() > 0,
                "Can only request positive units! Open a long position on the inverse for short stances!");

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
    public void processUpdates() {

        try {
            instrumentDataRetriever.retrieveClosedCandles();
        } catch (RequestException e) {
            LOG.error("Unable to retrieve closed candles!", e);
        }

        if (isClosed()) {
            LOG.info("Market is closed.");
            return;
        }

        for (ForexTrader trader : tradersByAccountId.values()) {
            try {
                trader.processUpdates(this);
            } catch (Exception e) {
                LOG.error("Unable to process trader: {}", trader, e);
            }
        }
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getOneDayCandles(ForexTrader trader, Instrument pair, Range<LocalDateTime> closed) throws RequestException {
        return getCandles(trader, D, closed, pair);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getFourHourCandles(ForexTrader trader, Instrument pair, Range<LocalDateTime> closed) throws RequestException {
        return getCandles(trader, CandlestickGranularity.H4, closed, pair);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getOneWeekCandles(ForexTrader trader, Instrument pair, Range<LocalDateTime> closed) throws RequestException {
        return getCandles(trader, W, closed, pair);
    }

    private NavigableMap<LocalDateTime, CandlestickData> getCandles(ForexTrader trader, CandlestickGranularity granularity, Range<LocalDateTime> closed, Instrument pair) throws RequestException {

        LocalDateTime exclusiveEnd = closed.upperEndpoint();

        InstrumentCandlesRequest request = new InstrumentCandlesRequest(pair);
        request.setPrice(of(BID, MID, ASK));
        request.setGranularity(granularity);
        request.setFrom(closed.lowerEndpoint());
        request.setTo(exclusiveEnd);
        request.setIncludeFirst(true);

        if (granularity == D) {
            request.setAlignmentTimezone(ZONE);
            request.setDailyAlignment(END_OF_TRADING_DAY_HOUR);
        } else if (granularity == W) {
            request.setWeeklyAlignment(WEEKLY_ALIGNMENT);
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
        Optional<Account> account = tradersByAccountId.get(trader.getAccountNumber()).getAccount();
        if (!account.isPresent()) {
            throw new RequestException("Unable to find the account for the trader!");
        }
        return account.get();
    }

    private Context getContext(ForexTrader trader) {
        return tradersByAccountId.get(trader.getAccountNumber()).getContext();
    }
}
