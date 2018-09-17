package forex.broker;

public interface InstrumentContext {

    InstrumentCandlesResponse candles(InstrumentCandlesRequest request) throws RequestException;

}
