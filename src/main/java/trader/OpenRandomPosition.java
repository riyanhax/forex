package trader;

import broker.OpenPositionRequest;
import market.Instrument;
import market.InstrumentHistoryService;
import market.MarketTime;

import java.util.Optional;
import java.util.Random;

public class OpenRandomPosition implements TradingStrategy {

    private final Random random = new Random();

    @Override
    public Optional<OpenPositionRequest> shouldOpenPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        Instrument[] instruments = Instrument.values();
        Instrument pair = instruments[random.nextInt(instruments.length)];

        return Optional.of(new OpenPositionRequest(pair, null, 30d, 60d));
    }
}
