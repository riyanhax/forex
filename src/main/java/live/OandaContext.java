package live;

import broker.Account;
import broker.AccountChangesRequest;
import broker.AccountChangesResponse;
import broker.AccountContext;
import broker.AccountGetResponse;
import broker.AccountID;
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
import broker.TradeSummary;
import broker.TransactionID;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.instrument.WeeklyAlignment;
import com.oanda.v20.order.OrderRequest;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.trade.TradeSpecifier;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class OandaContext implements Context {

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


            com.oanda.v20.pricing.PricingGetRequest oandaRequest = new com.oanda.v20.pricing.PricingGetRequest(
                    new com.oanda.v20.account.AccountID(request.getAccountID().getId()), request.getInstruments());

            try {
                com.oanda.v20.pricing.PricingGetResponse oandaResponse = ctx.pricing.get(oandaRequest);

                return new PricingGetResponse(oandaResponse.getPrices().stream()
                        .map(it -> new Price(it.getCloseoutBid().doubleValue(), it.getCloseoutAsk().doubleValue()))
                        .collect(toList()));
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
        com.oanda.v20.order.MarketOrderRequest oandaOrder = new com.oanda.v20.order.MarketOrderRequest();
        oandaOrder.setInstrument(order.getInstrument());
        oandaOrder.setUnits(order.getUnits());
        oandaOrder.setStopLossOnFill(convert(order.getStopLossOnFill()));
        oandaOrder.setTakeProfitOnFill(convert(order.getTakeProfitOnFill()));

        return oandaOrder;
    }

    private com.oanda.v20.transaction.StopLossDetails convert(StopLossDetails stopLossOnFill) {
        com.oanda.v20.transaction.StopLossDetails oandaVersion = new com.oanda.v20.transaction.StopLossDetails();
        oandaVersion.setPrice(stopLossOnFill.getPrice());

        return oandaVersion;
    }

    private com.oanda.v20.transaction.TakeProfitDetails convert(TakeProfitDetails takeProfit) {
        com.oanda.v20.transaction.TakeProfitDetails oandaVersion = new com.oanda.v20.transaction.TakeProfitDetails();
        oandaVersion.setPrice(takeProfit.getPrice());

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

            com.oanda.v20.instrument.InstrumentCandlesRequest oandaRequest = new com.oanda.v20.instrument.InstrumentCandlesRequest(
                    new InstrumentName(request.getInstrument()));
            oandaRequest.setPrice(request.getPrice());
            oandaRequest.setGranularity(convert(request.getGranularity()));
            oandaRequest.setFrom(request.getFrom());
            oandaRequest.setTo(request.getTo());
            oandaRequest.setIncludeFirst(request.isIncludeFirst());
            oandaRequest.setAlignmentTimezone(request.getAlignmentTimezone());
            oandaRequest.setDailyAlignment(request.getDailyAlignment());
            oandaRequest.setWeeklyAlignment(convert(request.getWeeklyAlignment()));

            try {
                com.oanda.v20.instrument.InstrumentCandlesResponse oandaResponse = instrument.candles(oandaRequest);

                return convert(oandaResponse);
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

    private static InstrumentCandlesResponse convert(com.oanda.v20.instrument.InstrumentCandlesResponse oandaResponse) {
        return new InstrumentCandlesResponse(oandaResponse.getInstrument().toString(),
                convert(oandaResponse.getGranularity()),
                oandaResponse.getCandles().stream().map(OandaContext::convert).collect(toList()));
    }

    private static Candlestick convert(com.oanda.v20.instrument.Candlestick data) {
        return new Candlestick(data.getTime().toString(),
                convert(data.getAsk()), convert(data.getBid()), convert(data.getMid()));
    }

    private static CandlestickData convert(com.oanda.v20.instrument.CandlestickData data) {
        return new CandlestickData(data.getO().doubleValue(), data.getH().doubleValue(),
                data.getL().doubleValue(), data.getC().doubleValue());
    }

    private static Account convert(com.oanda.v20.account.Account oandaAccount) {
        return new Account(convert(oandaAccount.getId()), convert(oandaAccount.getLastTransactionID()),
                oandaAccount.getTrades().stream().map(OandaContext::convert).collect(toList()),
                oandaAccount.getPl().doubleValue());
    }

    private static TradeSummary convert(com.oanda.v20.trade.TradeSummary tradeSummary) {
        return new TradeSummary(tradeSummary.getInstrument().toString(),
                (int) tradeSummary.getCurrentUnits().doubleValue(),
                tradeSummary.getPrice().doubleValue(),
                tradeSummary.getUnrealizedPL().doubleValue(),
                tradeSummary.getOpenTime().toString(),
                tradeSummary.getId().toString());
    }

    private static AccountID convert(com.oanda.v20.account.AccountID id) {
        return new AccountID(id.toString());
    }

    private static TransactionID convert(com.oanda.v20.transaction.TransactionID oandaVersion) {
        return new TransactionID(oandaVersion.toString());
    }

    private final com.oanda.v20.Context ctx;

    private OandaContext(String endpoint, String token) {
        this.ctx = new com.oanda.v20.Context(endpoint, token);
    }

    @Override
    public PricingContext pricing() {
        return new OandaPricing(ctx.pricing);
    }

    @Override
    public OrderContext order() {
        return new OandaOrder(ctx.order);
    }

    @Override
    public TradeContext trade() {
        return new OandaTrade(ctx.trade);
    }

    @Override
    public AccountContext account() {
        return new OandaAccount(ctx.account);
    }

    @Override
    public InstrumentContext instrument() {
        return new OandaInstrument(ctx.instrument);
    }
}
