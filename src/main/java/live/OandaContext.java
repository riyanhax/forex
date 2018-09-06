package live;

import broker.Account;
import broker.AccountChangesRequest;
import broker.AccountChangesResponse;
import broker.AccountContext;
import broker.AccountGetResponse;
import broker.AccountID;
import broker.BaseContext;
import broker.CandlePrice;
import broker.Candlestick;
import broker.CandlestickData;
import broker.CandlestickGranularity;
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
import broker.StopLossDetails;
import broker.TakeProfitDetails;
import broker.TradeCloseRequest;
import broker.TradeCloseResponse;
import broker.TradeContext;
import broker.TradeListRequest;
import broker.TradeListResponse;
import broker.TradeSummary;
import broker.TransactionID;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.instrument.WeeklyAlignment;
import com.oanda.v20.order.OrderRequest;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.trade.TradeSpecifier;
import com.oanda.v20.trade.TradeStateFilter;
import market.Instrument;
import market.MarketTime;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static broker.Quote.doubleFromPippetes;
import static broker.Quote.invert;
import static broker.Quote.pippetesFromDouble;
import static java.lang.Math.abs;
import static java.time.LocalDateTime.parse;
import static java.time.format.TextStyle.NARROW;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static market.MarketTime.ZONE;

public class OandaContext extends BaseContext {

    private static DateTimeFormatter ISO_INSTANT_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    public static Context create(String endpoint, String token) {
        return new OandaContext(endpoint, token);
    }

    private class OandaPricing implements PricingContext {

        private final com.oanda.v20.pricing.PricingContext pricing;

        OandaPricing(com.oanda.v20.pricing.PricingContext pricing) {
            this.pricing = pricing;
        }

        @Override
        public PricingGetResponse get(PricingGetRequest request) throws broker.RequestException {

            com.oanda.v20.account.AccountID accountID = new com.oanda.v20.account.AccountID(request.getAccountID().getId());
            Set<String> instruments = request.getInstruments().stream()
                    .map(Instrument::getBrokerInstrument)
                    .map(Instrument::getSymbol).collect(toSet());

            com.oanda.v20.pricing.PricingGetRequest oandaRequest =
                    new com.oanda.v20.pricing.PricingGetRequest(accountID, instruments);

            try {
                com.oanda.v20.pricing.PricingGetResponse oandaResponse = ctx.pricing.get(oandaRequest);

                return convert(request.getInstruments(), oandaResponse);
            } catch (RequestException e) {
                throw new broker.RequestException(e.getErrorMessage(), e);
            } catch (ExecuteException e) {
                throw new broker.RequestException(e.getMessage(), e);
            }
        }
    }

    private class OandaOrder implements OrderContext {
        private final com.oanda.v20.order.OrderContext order;

        OandaOrder(com.oanda.v20.order.OrderContext order) {
            this.order = order;
        }

        @Override
        public OrderCreateResponse create(OrderCreateRequest request) throws broker.RequestException {

            com.oanda.v20.order.OrderCreateRequest oandaRequest = new com.oanda.v20.order.OrderCreateRequest(
                    new com.oanda.v20.account.AccountID(request.getAccountID().getId()));
            oandaRequest.setOrder(convert(request.getOrder()));

            try {
                com.oanda.v20.order.OrderCreateResponse oandaResponse = order.create(oandaRequest);

                return new OrderCreateResponse();
            } catch (RequestException e) {
                throw new broker.RequestException(e.getErrorMessage(), e);
            } catch (ExecuteException e) {
                throw new broker.RequestException(e.getMessage(), e);
            }
        }
    }

    private OrderRequest convert(MarketOrderRequest order) {
        Instrument instrument = order.getInstrument();
        int units = order.getUnits();

        boolean shorting = instrument.isInverse();
        if (shorting) {
            instrument = instrument.getOpposite();
            units = -units;
        }

        com.oanda.v20.transaction.StopLossDetails stopLossDetails = convert(shorting, order.getStopLossOnFill());
        com.oanda.v20.transaction.TakeProfitDetails takeProfitDetails = convert(shorting, order.getTakeProfitOnFill());

        com.oanda.v20.order.MarketOrderRequest oandaOrder = new com.oanda.v20.order.MarketOrderRequest();
        oandaOrder.setInstrument(instrument.getSymbol());
        oandaOrder.setUnits(units);
        oandaOrder.setStopLossOnFill(stopLossDetails);
        oandaOrder.setTakeProfitOnFill(takeProfitDetails);

        LoggerFactory.getLogger(OandaContext.class).info("Converted order {} to {}", order, oandaOrder);

        return oandaOrder;
    }

    private com.oanda.v20.transaction.StopLossDetails convert(boolean inverse, StopLossDetails stopLossOnFill) {
        long price = stopLossOnFill.getPrice();
        if (inverse) {
            price = invert(price);
        }

        com.oanda.v20.transaction.StopLossDetails oandaVersion = new com.oanda.v20.transaction.StopLossDetails();
        oandaVersion.setPrice(doubleFromPippetes(price));

        return oandaVersion;
    }

    private com.oanda.v20.transaction.TakeProfitDetails convert(boolean inverse, TakeProfitDetails takeProfit) {
        long price = takeProfit.getPrice();
        if (inverse) {
            price = invert(price);
        }

        com.oanda.v20.transaction.TakeProfitDetails oandaVersion = new com.oanda.v20.transaction.TakeProfitDetails();
        oandaVersion.setPrice(doubleFromPippetes(price));

        return oandaVersion;
    }

    private class OandaTrade implements TradeContext {
        private final com.oanda.v20.trade.TradeContext trade;

        OandaTrade(com.oanda.v20.trade.TradeContext trade) {
            this.trade = trade;
        }

        @Override
        public TradeCloseResponse close(TradeCloseRequest request) throws broker.RequestException {
            com.oanda.v20.trade.TradeCloseRequest oandaRequest = new com.oanda.v20.trade.TradeCloseRequest(
                    new com.oanda.v20.account.AccountID(request.getAccountID().getId()),
                    new TradeSpecifier(request.getTradeSpecifier().getId()));

            try {
                com.oanda.v20.trade.TradeCloseResponse oandaResponse = trade.close(oandaRequest);

                return new TradeCloseResponse();
            } catch (RequestException e) {
                throw new broker.RequestException(e.getErrorMessage(), e);
            } catch (ExecuteException e) {
                throw new broker.RequestException(e.getMessage(), e);
            }
        }

        @Override
        public TradeListResponse list(TradeListRequest request) throws broker.RequestException {
            com.oanda.v20.trade.TradeListRequest oandaRequest = new com.oanda.v20.trade.TradeListRequest(
                    new com.oanda.v20.account.AccountID(request.getAccountID().getId()));
            // TODO: Needs to be passed in
            oandaRequest.setState(TradeStateFilter.CLOSED);
            oandaRequest.setCount(request.getCount());

            try {
                com.oanda.v20.trade.TradeListResponse oandaResponse = trade.list(oandaRequest);

                return new TradeListResponse(
                        oandaResponse.getTrades().stream().map(OandaContext::convert).collect(toList()),
                        convert(oandaResponse.getLastTransactionID()));
            } catch (RequestException e) {
                throw new broker.RequestException(e.getErrorMessage(), e);
            } catch (ExecuteException e) {
                throw new broker.RequestException(e.getMessage(), e);
            }
        }
    }

    static TradeSummary convert(com.oanda.v20.trade.Trade trade) {

        //TODO: Combine this parsing/logic with converting TradeSummary
        Instrument instrument = Instrument.bySymbol.get(trade.getInstrument().toString());
        String openTime = trade.getOpenTime().toString();
        String closeTime = trade.getCloseTime() == null ? null : trade.getCloseTime().toString();
        long price = pippetesFromDouble(trade.getPrice().doubleValue());
        long realizedProfitLoss = pippetesFromDouble(trade.getRealizedPL().doubleValue());
        long unrealizedProfitLoss = pippetesFromDouble(trade.getUnrealizedPL() == null ? 0L : trade.getUnrealizedPL().doubleValue());

        if (trade.getInitialUnits().doubleValue() < 0) {
            instrument = instrument.getOpposite();
            price = invert(price);
        }

        return new TradeSummary(instrument,
                abs((int) trade.getInitialUnits().doubleValue()),
                price,
                realizedProfitLoss,
                unrealizedProfitLoss,
                parseTimestamp(openTime), parseTimestamp(closeTime), trade.getId().toString());
    }

    private class OandaAccount implements AccountContext {
        private final com.oanda.v20.account.AccountContext account;

        OandaAccount(com.oanda.v20.account.AccountContext account) {
            this.account = account;
        }

        @Override
        public AccountChangesResponse changes(AccountChangesRequest request) throws broker.RequestException {
            com.oanda.v20.account.AccountID accountID = new com.oanda.v20.account.AccountID(request.getAccountID().getId());

            com.oanda.v20.account.AccountChangesRequest oandaRequest = new com.oanda.v20.account.AccountChangesRequest(accountID);
            oandaRequest.setSinceTransactionID(new com.oanda.v20.transaction.TransactionID(request.getSinceTransactionID().getId()));

            try {
                com.oanda.v20.account.AccountChangesResponse oandaResponse = account.changes(oandaRequest);

                TransactionID lastTransactionID = convert(oandaResponse.getLastTransactionID());
                return new AccountChangesResponse(lastTransactionID);
            } catch (RequestException e) {
                throw new broker.RequestException(e.getErrorMessage(), e);
            } catch (ExecuteException e) {
                throw new broker.RequestException(e.getMessage(), e);
            }
        }

        @Override
        public AccountGetResponse get(AccountID accountID) throws broker.RequestException {
            try {
                com.oanda.v20.account.AccountGetResponse oandaResponse = account.get(new com.oanda.v20.account.AccountID(accountID.getId()));
                Account account = convert(oandaResponse.getAccount());

                return new AccountGetResponse(account);
            } catch (RequestException e) {
                throw new broker.RequestException(e.getErrorMessage(), e);
            } catch (ExecuteException e) {
                throw new broker.RequestException(e.getMessage(), e);
            }
        }
    }

    private class OandaInstrument implements InstrumentContext {
        private final com.oanda.v20.instrument.InstrumentContext instrument;

        public OandaInstrument(com.oanda.v20.instrument.InstrumentContext instrument) {
            this.instrument = instrument;
        }

        @Override
        public InstrumentCandlesResponse candles(InstrumentCandlesRequest request) throws broker.RequestException {

            ZonedDateTime start = ZonedDateTime.of(request.getFrom(), ZONE);
            ZonedDateTime end = ZonedDateTime.of(request.getTo(), ZONE);
            String price = request.getPrice().stream().map(CandlePrice::getSymbol).collect(joining(""));

            com.oanda.v20.instrument.InstrumentCandlesRequest oandaRequest = new com.oanda.v20.instrument.InstrumentCandlesRequest(
                    new InstrumentName(request.getInstrument().getBrokerInstrument().getSymbol()));
            oandaRequest.setPrice(price);
            oandaRequest.setGranularity(convert(request.getGranularity()));
            // These dates get translated to UTC time via the formatter, which is what Oanda expects
            oandaRequest.setFrom(start.format(ISO_INSTANT_FORMATTER));
            oandaRequest.setTo(end.format(ISO_INSTANT_FORMATTER));
            oandaRequest.setIncludeFirst(request.isIncludeFirst());

            if (request.getAlignmentTimezone() != null) {
                String alignmentTimezone = request.getAlignmentTimezone().getDisplayName(NARROW, Locale.US);

                oandaRequest.setDailyAlignment(request.getDailyAlignment());
                oandaRequest.setAlignmentTimezone(alignmentTimezone);
            }

            if (request.getWeeklyAlignment() != null) {
                oandaRequest.setWeeklyAlignment(convert(request.getWeeklyAlignment()));
            }

            try {
                com.oanda.v20.instrument.InstrumentCandlesResponse oandaResponse = instrument.candles(oandaRequest);

                return convert(request.getInstrument(), oandaResponse);
            } catch (RequestException e) {
                throw new broker.RequestException(e.getErrorMessage(), e);
            } catch (ExecuteException e) {
                throw new broker.RequestException(e.getMessage(), e);
            }
        }
    }

    private static BiMap<CandlestickGranularity, com.oanda.v20.instrument.CandlestickGranularity> granularities =
            HashBiMap.create(stream(CandlestickGranularity.values())
                    .collect(toMap(Function.identity(), it -> com.oanda.v20.instrument.CandlestickGranularity.valueOf(it.name()))));

    private static com.oanda.v20.instrument.CandlestickGranularity convert(CandlestickGranularity granularity) {
        return granularities.get(granularity);
    }

    private static CandlestickGranularity convert(com.oanda.v20.instrument.CandlestickGranularity oandaVersion) {
        return granularities.inverse().get(oandaVersion);
    }

    private static Map<DayOfWeek, WeeklyAlignment> alignments = stream(DayOfWeek.values())
            .collect(toMap(Function.identity(), it -> WeeklyAlignment.valueOf(it.getDisplayName(TextStyle.FULL, Locale.US))));

    private static WeeklyAlignment convert(DayOfWeek day) {
        return alignments.get(day);
    }

    private static InstrumentCandlesResponse convert(Instrument requestedInstrument, com.oanda.v20.instrument.InstrumentCandlesResponse oandaResponse) {
        Instrument responseInstrument = Instrument.bySymbol.get(oandaResponse.getInstrument().toString());
        verifyResponseInstrument(requestedInstrument, responseInstrument);

        return new InstrumentCandlesResponse(
                responseInstrument,
                convert(oandaResponse.getGranularity()),
                oandaResponse.getCandles().stream().map(it ->
                        convert(requestedInstrument.isInverse(), it)).collect(toList()));
    }

    private static void verifyResponseInstrument(Instrument requestedInstrument, Instrument responseInstrument) {
        if (requestedInstrument != responseInstrument) {
            Preconditions.checkArgument(requestedInstrument == responseInstrument.getOpposite(),
                    "Received response instrument %s but requested was %s and not inverse %s",
                    responseInstrument, requestedInstrument, responseInstrument.getOpposite());
        }
    }

    private static Candlestick convert(boolean inverse, com.oanda.v20.instrument.Candlestick data) {
        ZonedDateTime zonedDateTime = parseToZone(data.getTime().toString(), ZONE);
        LocalDateTime timestamp = zonedDateTime.toLocalDateTime();

        return new Candlestick(timestamp, convert(inverse, data.getAsk()),
                convert(inverse, data.getBid()), convert(inverse, data.getMid()));
    }

    private static CandlestickData convert(boolean inverse, com.oanda.v20.instrument.CandlestickData data) {
        if (data == null) {
            return null;
        }

        double open = data.getO().doubleValue();
        double high = data.getH().doubleValue();
        double low = data.getL().doubleValue();
        double close = data.getC().doubleValue();

        if (inverse) {
            double actualHigh = low;
            low = high;
            high = actualHigh;
        }

        return new CandlestickData(
                pippetesFromDouble(inverse, open),
                pippetesFromDouble(inverse, high),
                pippetesFromDouble(inverse, low),
                pippetesFromDouble(inverse, close));
    }

    private static Account convert(com.oanda.v20.account.Account oandaAccount) {
        return new Account(convert(oandaAccount.getId()), convert(oandaAccount.getLastTransactionID()),
                oandaAccount.getTrades().stream().map(OandaContext::convert).collect(toList()),
                pippetesFromDouble(oandaAccount.getPl().doubleValue()));
    }

    private static TradeSummary convert(com.oanda.v20.trade.TradeSummary tradeSummary) {
        Instrument instrument = Instrument.bySymbol.get(tradeSummary.getInstrument().toString());
        String openTime = tradeSummary.getOpenTime().toString();
        String closeTime = tradeSummary.getCloseTime() == null ? null : tradeSummary.getCloseTime().toString();

        return new TradeSummary(instrument,
                (int) tradeSummary.getCurrentUnits().doubleValue(),
                pippetesFromDouble(tradeSummary.getPrice().doubleValue()),
                pippetesFromDouble(tradeSummary.getRealizedPL().doubleValue()),
                pippetesFromDouble(tradeSummary.getUnrealizedPL().doubleValue()),
                parseTimestamp(openTime), parseTimestamp(closeTime), tradeSummary.getId().toString());
    }

    static PricingGetResponse convert(Set<Instrument> requestInstruments,
                                      com.oanda.v20.pricing.PricingGetResponse oandaResponse) {
        List<Price> prices = new ArrayList<>();

        Iterator<Instrument> requestedInstrumentIter = requestInstruments.iterator();
        for (com.oanda.v20.pricing.Price oandaPrice : oandaResponse.getPrices()) {
            Instrument requestedInstrument = requestedInstrumentIter.next();
            prices.add(convert(requestedInstrument, oandaPrice));
        }
        return new PricingGetResponse(prices);
    }

    static Price convert(Instrument requestedInstrument, com.oanda.v20.pricing.Price oandaPrice) {
        Instrument responseInstrument = Instrument.bySymbol.get(oandaPrice.getInstrument().toString());
        verifyResponseInstrument(requestedInstrument, responseInstrument);

        boolean inverse = requestedInstrument.isInverse();

        long bid = pippetesFromDouble(inverse, oandaPrice.getBids().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No bid prices found!"))
                .getPrice().doubleValue());
        long ask = pippetesFromDouble(inverse, oandaPrice.getAsks().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No ask prices found!"))
                .getPrice().doubleValue());

        if (inverse) {
            long actualAsk = bid;
            bid = ask;
            ask = actualAsk;
        }

        return new Price(requestedInstrument, bid, ask);
    }

    private static AccountID convert(com.oanda.v20.account.AccountID id) {
        return new AccountID(id.toString());
    }

    private static TransactionID convert(com.oanda.v20.transaction.TransactionID oandaVersion) {
        return new TransactionID(oandaVersion.toString());
    }

    private static LocalDateTime parseTimestamp(String timestamp) {
        return timestamp == null ? null : parse(timestamp, ISO_INSTANT_FORMATTER.withZone(MarketTime.ZONE));
    }

    private static ZonedDateTime parseToZone(String time, ZoneId zone) {
        return ZonedDateTime.parse(time.substring(0, 19) + "Z", ISO_INSTANT_FORMATTER.withZone(zone));
    }

    private final com.oanda.v20.Context ctx;

    private OandaContext(String endpoint, String token) {
        this.ctx = new com.oanda.v20.Context(endpoint, token);
    }

    @Override
    protected PricingContext pricing() {
        return new OandaPricing(ctx.pricing);
    }

    @Override
    protected OrderContext order() {
        return new OandaOrder(ctx.order);
    }

    @Override
    protected TradeContext trade() {
        return new OandaTrade(ctx.trade);
    }

    @Override
    protected AccountContext account() {
        return new OandaAccount(ctx.account);
    }

    @Override
    protected InstrumentContext instrument() {
        return new OandaInstrument(ctx.instrument);
    }
}
