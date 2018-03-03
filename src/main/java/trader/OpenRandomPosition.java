package trader;

import market.InstrumentHistoryService;
import market.forex.Instrument;
import market.MarketTime;

import java.util.Optional;
import java.util.Random;

class OpenRandomPosition extends BaseTrader {

    private final Random random = new Random();

    OpenRandomPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        super(clock, instrumentHistoryService);
    }

    @Override
    Optional<Instrument> shouldOpenPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        Instrument[] instruments = Instrument.values();
        Instrument pair = instruments[random.nextInt(instruments.length)];

        return Optional.of(pair);
    }
}
