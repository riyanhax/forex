package live.oanda;

import broker.CalculatedTradeState;
import broker.MarketOrderTransaction;
import broker.Trade;
import broker.TradeCloseResponse;
import broker.TradeListRequest;
import broker.TradeListResponse;
import broker.TradeState;
import broker.TradeStateFilter;
import broker.TradeSummary;
import com.oanda.v20.pricing.PriceValue;
import com.oanda.v20.primitives.AccountUnits;
import com.oanda.v20.primitives.DateTime;
import com.oanda.v20.primitives.DecimalNumber;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeID;
import com.oanda.v20.trade.TradeSpecifier;
import market.Instrument;

import java.time.LocalDateTime;

import static broker.Quote.invert;
import static broker.Quote.pippetesFromDouble;
import static java.lang.Math.abs;
import static java.util.stream.Collectors.toList;

class TradeConverter {

    static TradeCloseRequest convert(broker.TradeCloseRequest request) {
        return new com.oanda.v20.trade.TradeCloseRequest(
                new com.oanda.v20.account.AccountID(request.getAccountID().getId()),
                new TradeSpecifier(request.getTradeSpecifier().getId()));
    }

    static TradeCloseResponse convert(com.oanda.v20.trade.TradeCloseResponse oandaResponse) {
        com.oanda.v20.transaction.MarketOrderTransaction orderCreateTransaction = oandaResponse.getOrderCreateTransaction();

        Instrument instrument = CommonConverter.convert(oandaResponse.getOrderCreateTransaction().getInstrument());
        if ((int) orderCreateTransaction.getUnits().doubleValue() < 0) {
            instrument = instrument.getOpposite();
        }

        MarketOrderTransaction orderCreated = OrderConverter.convert(instrument, orderCreateTransaction);

        return new TradeCloseResponse(orderCreated);
    }

    static com.oanda.v20.trade.TradeListRequest convert(TradeListRequest request) {
        com.oanda.v20.trade.TradeListRequest oandaRequest = new com.oanda.v20.trade.TradeListRequest(
                new com.oanda.v20.account.AccountID(request.getAccountID().getId()));
        oandaRequest.setState(convert(request.getFilter()));
        oandaRequest.setCount(request.getCount());

        return oandaRequest;
    }

    static com.oanda.v20.trade.TradeStateFilter convert(TradeStateFilter filter) {
        return com.oanda.v20.trade.TradeStateFilter.valueOf(filter.name());
    }

    static TradeListResponse convert(com.oanda.v20.trade.TradeListResponse oandaResponse) {
        return new TradeListResponse(
                oandaResponse.getTrades().stream().map(TradeConverter::convert).collect(toList()),
                CommonConverter.convert(oandaResponse.getLastTransactionID()));
    }

    static Trade convert(com.oanda.v20.trade.Trade trade) {

        Instrument instrument = InstrumentConverter.convert(trade.getInstrument());
        long price = pippetesFromDouble(trade.getPrice().doubleValue());
        long averageClosePrice = trade.getAverageClosePrice() == null ? 0L : pippetesFromDouble(trade.getAverageClosePrice().doubleValue());
        int initialUnits = (int) trade.getInitialUnits().doubleValue();

        if (initialUnits < 0) {
            instrument = instrument.getOpposite();
            price = invert(price);
            if (averageClosePrice != 0) {
                averageClosePrice = invert(averageClosePrice);
            }
        }

        return new Trade(trade.getId().toString(), instrument,
                price,
                trade.getOpenTime() == null ? null : CommonConverter.parseTimestamp(trade.getOpenTime().toString()),
                TradeState.valueOf(trade.getState().name()),
                abs(initialUnits),
                abs((int) trade.getCurrentUnits().doubleValue()),
                trade.getRealizedPL() == null ? 0L : pippetesFromDouble(trade.getRealizedPL().doubleValue()),
                trade.getUnrealizedPL() == null ? 0L : pippetesFromDouble(trade.getUnrealizedPL().doubleValue()),
                trade.getMarginUsed() == null ? 0L : pippetesFromDouble(trade.getMarginUsed().doubleValue()),
                averageClosePrice,
                trade.getClosingTransactionIDs().stream().map(CommonConverter::convert).collect(toList()),
                trade.getFinancing() == null ? 0L : pippetesFromDouble(trade.getFinancing().doubleValue()),
                trade.getCloseTime() == null ? null : CommonConverter.parseTimestamp(trade.getCloseTime().toString()));
    }

    static TradeSummary convert(com.oanda.v20.trade.TradeSummary tradeSummary) {
        return createTradeSummary(tradeSummary.getInstrument(), tradeSummary.getOpenTime(), tradeSummary.getCloseTime(),
                tradeSummary.getPrice(), tradeSummary.getRealizedPL(), tradeSummary.getUnrealizedPL(), tradeSummary.getInitialUnits(),
                tradeSummary.getId());
    }

    static CalculatedTradeState convert(com.oanda.v20.trade.CalculatedTradeState state) {
        return new CalculatedTradeState(state.getId().toString(), pippetesFromDouble(state.getUnrealizedPL().doubleValue()));
    }

    private static TradeSummary createTradeSummary(InstrumentName instrumentName, DateTime openDateTime, DateTime closeDateTime,
                                                   PriceValue priceValue, AccountUnits realizedPl, AccountUnits unrealizedPl,
                                                   DecimalNumber initialUnits, TradeID id) {
        Instrument instrument = InstrumentConverter.convert(instrumentName);
        LocalDateTime openTime = openDateTime == null ? null : CommonConverter.parseTimestamp(openDateTime.toString());
        LocalDateTime closeTime = closeDateTime == null ? null : CommonConverter.parseTimestamp(closeDateTime.toString());
        long price = pippetesFromDouble(priceValue.doubleValue());
        long realizedProfitLoss = pippetesFromDouble(realizedPl == null ? 0L : realizedPl.doubleValue());
        long unrealizedProfitLoss = pippetesFromDouble(unrealizedPl == null ? 0L : unrealizedPl.doubleValue());

        if (initialUnits.doubleValue() < 0) {
            instrument = instrument.getOpposite();
            price = invert(price);
        }

        return new TradeSummary(instrument,
                abs((int) initialUnits.doubleValue()),
                price,
                realizedProfitLoss,
                unrealizedProfitLoss,
                openTime, closeTime, id.toString());
    }
}
