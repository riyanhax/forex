package trader;

import market.InstrumentHistoryService;
import market.forex.Instrument;
import simulator.AppClock;

import java.util.Optional;
import java.util.Random;

class OpenRandomPosition extends BaseTrader {

    private final Random random = new Random();

    OpenRandomPosition(AppClock clock, InstrumentHistoryService instrumentHistoryService) {
        super(clock, instrumentHistoryService);
    }

    @Override
    Optional<Instrument> shouldOpenPosition(AppClock clock, InstrumentHistoryService instrumentHistoryService) {
        Instrument[] instruments = Instrument.values();
        Instrument pair = instruments[random.nextInt(instruments.length)];

        return Optional.of(pair);
    }
}
