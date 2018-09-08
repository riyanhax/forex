package live.oanda;

import broker.Price;
import broker.PricingGetRequest;
import broker.PricingGetResponse;
import market.Instrument;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static broker.Quote.pippetesFromDouble;
import static java.util.stream.Collectors.toSet;
import static live.oanda.CommonConverter.verifyResponseInstrument;

class PricingConverter {

    static com.oanda.v20.pricing.PricingGetRequest convert(PricingGetRequest request) {
        com.oanda.v20.account.AccountID accountID = AccountConverter.convert(request.getAccountID());

        Set<String> instruments = request.getInstruments().stream()
                .map(Instrument::getBrokerInstrument)
                .map(Instrument::getSymbol).collect(toSet());

        return new com.oanda.v20.pricing.PricingGetRequest(accountID, instruments);
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

    private static Price convert(Instrument requestedInstrument, com.oanda.v20.pricing.Price oandaPrice) {
        Instrument responseInstrument = CommonConverter.convert(oandaPrice.getInstrument());
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

}
