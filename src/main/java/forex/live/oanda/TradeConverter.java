package forex.live.oanda;

import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeSpecifier;
import forex.broker.CalculatedTradeState;
import forex.broker.MarketOrderTransaction;
import forex.broker.Trade;
import forex.broker.TradeCloseResponse;
import forex.broker.TradeListRequest;
import forex.broker.TradeListResponse;
import forex.broker.TradeState;
import forex.broker.TradeStateFilter;
import forex.broker.TradeSummary;
import forex.market.Instrument;

import java.time.LocalDateTime;

import static forex.broker.Quote.invert;
import static forex.live.oanda.CommonConverter.parseTimestamp;
import static forex.live.oanda.CommonConverter.pippetes;
import static forex.live.oanda.CommonConverter.toInt;
import static java.lang.Math.abs;
import static java.util.stream.Collectors.toList;

class TradeConverter {

    static TradeCloseRequest convert(forex.broker.TradeCloseRequest request) {
        return new com.oanda.v20.trade.TradeCloseRequest(
                new com.oanda.v20.account.AccountID(request.getAccountID()),
                new TradeSpecifier(request.getTradeSpecifier().getId()));
    }

    static TradeCloseResponse convert(com.oanda.v20.trade.TradeCloseResponse oandaResponse) {
        com.oanda.v20.transaction.MarketOrderTransaction orderCreateTransaction = oandaResponse.getOrderCreateTransaction();

        Instrument instrument = CommonConverter.convert(oandaResponse.getOrderCreateTransaction().getInstrument());
        if (toInt(orderCreateTransaction.getUnits()) < 0) {
            instrument = instrument.getOpposite();
        }

        MarketOrderTransaction orderCreated = OrderConverter.convert(instrument, orderCreateTransaction);

        return new TradeCloseResponse(orderCreated);
    }

    static com.oanda.v20.trade.TradeListRequest convert(TradeListRequest request) {
        com.oanda.v20.trade.TradeListRequest oandaRequest = new com.oanda.v20.trade.TradeListRequest(
                new com.oanda.v20.account.AccountID(request.getAccountID()));
        oandaRequest.setState(convert(request.getFilter()));
        oandaRequest.setCount(request.getCount());

        if (!request.getTradeIds().isEmpty()) {
            oandaRequest.setIds(request.getTradeIds());
        }

        return oandaRequest;
    }

    static com.oanda.v20.trade.TradeStateFilter convert(TradeStateFilter filter) {
        return com.oanda.v20.trade.TradeStateFilter.valueOf(filter.name());
    }

    static TradeListResponse convert(com.oanda.v20.trade.TradeListResponse oandaResponse, String accountId) {
        return new TradeListResponse(
                oandaResponse.getTrades().stream().map(it -> convert(it, accountId)).collect(toList()),
                OrderConverter.id(oandaResponse.getLastTransactionID()));
    }

    static Trade convert(com.oanda.v20.trade.Trade trade, String accountId) {

        Instrument instrument = InstrumentConverter.convert(trade.getInstrument());
        long price = pippetes(trade.getPrice());
        long averageClosePrice = trade.getAverageClosePrice() == null ? 0L : pippetes(trade.getAverageClosePrice());
        int initialUnits = toInt(trade.getInitialUnits());

        if (initialUnits < 0) {
            instrument = instrument.getOpposite();
            price = invert(price);
            if (averageClosePrice != 0) {
                averageClosePrice = invert(averageClosePrice);
            }
        }

        return new Trade(trade.getId().toString(), accountId, instrument,
                price,
                parseTimestamp(trade.getOpenTime()),
                TradeState.valueOf(trade.getState().name()),
                abs(initialUnits),
                abs(toInt(trade.getCurrentUnits())),
                trade.getRealizedPL() == null ? 0L : pippetes(trade.getRealizedPL()),
                trade.getUnrealizedPL() == null ? 0L : pippetes(trade.getUnrealizedPL()),
                trade.getMarginUsed() == null ? 0L : pippetes(trade.getMarginUsed()),
                averageClosePrice,
                trade.getClosingTransactionIDs().stream().map(OrderConverter::id).collect(toList()),
                trade.getFinancing() == null ? 0L : pippetes(trade.getFinancing()),
                parseTimestamp(trade.getCloseTime()));
    }

    static TradeSummary convert(com.oanda.v20.trade.TradeSummary tradeSummary, String accountId) {
        Instrument instrument = InstrumentConverter.convert(tradeSummary.getInstrument());
        LocalDateTime openTime = parseTimestamp(tradeSummary.getOpenTime());
        LocalDateTime closeTime = parseTimestamp(tradeSummary.getCloseTime());
        long price = pippetes(tradeSummary.getPrice());
        long realizedProfitLoss = tradeSummary.getRealizedPL() == null ? 0L : pippetes(tradeSummary.getRealizedPL());
        long unrealizedProfitLoss = tradeSummary.getUnrealizedPL() == null ? 0L : pippetes(tradeSummary.getUnrealizedPL());
        int initialUnits = toInt(tradeSummary.getInitialUnits());

        if (initialUnits < 0) {
            instrument = instrument.getOpposite();
            price = invert(price);
        }

        return new TradeSummary(tradeSummary.getId().toString(), accountId, instrument, price, openTime, abs((int) initialUnits),
                abs(toInt(tradeSummary.getCurrentUnits())), realizedProfitLoss, unrealizedProfitLoss, closeTime);
    }

    static CalculatedTradeState convert(com.oanda.v20.trade.CalculatedTradeState state) {
        return new CalculatedTradeState(state.getId().toString(), pippetes(state.getUnrealizedPL()));
    }
}
