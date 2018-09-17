package forex.market;

import forex.broker.RequestException;

public interface InstrumentDataRetriever {

    void retrieveClosedCandles() throws RequestException;

}
