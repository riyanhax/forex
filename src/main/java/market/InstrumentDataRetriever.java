package market;

import broker.RequestException;

public interface InstrumentDataRetriever {

    void retrieveClosedCandles() throws RequestException;

}
