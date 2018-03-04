package trader;

import broker.OpenPositionRequest;
import market.Instrument;
import market.InstrumentHistoryService;
import market.MarketTime;

import java.util.Optional;
import java.util.Random;

class OpenRandomPosition extends BaseTrader {

    private final Random random = new Random();

    OpenRandomPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        super(clock, instrumentHistoryService);
    }

    @Override
    Optional<OpenPositionRequest> shouldOpenPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        Instrument[] instruments = Instrument.values();
        Instrument pair = instruments[random.nextInt(instruments.length)];

        return Optional.of(new OpenPositionRequest(pair, null, 30d, 60d));
    }
}
