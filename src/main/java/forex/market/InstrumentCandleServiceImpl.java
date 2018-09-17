package forex.market;

import forex.broker.CandlePrice;
import forex.broker.Candlestick;
import forex.broker.Context;
import forex.broker.InstrumentCandlesRequest;
import forex.broker.RequestException;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static forex.broker.CandlePrice.ASK;
import static forex.broker.CandlePrice.MID;
import static forex.broker.CandlestickGranularity.M1;
import static java.time.Month.JANUARY;
import static java.util.stream.Collectors.toList;

@Service
public class InstrumentCandleServiceImpl implements InstrumentCandleService {

    private final Context context;
    private final InstrumentCandleRepository instrumentCandleRepo;

    public InstrumentCandleServiceImpl(Context context, InstrumentCandleRepository instrumentCandleRepo) {
        this.context = context;
        this.instrumentCandleRepo = instrumentCandleRepo;
    }

    @Transactional
    @Override
    public List<InstrumentCandle> retrieveAndStoreOneMinuteCandles(Range<LocalDateTime> inclusiveRange) throws RequestException {

        List<InstrumentCandle> retrievedAndStored = new ArrayList<>();

        for (Instrument instrument : Instrument.values()) {

            if (instrument.isInverse()) {
                continue;
            }

            InstrumentCandlesRequest request = new InstrumentCandlesRequest(Instrument.EURUSD);
            request.setPrice(ImmutableSet.of(CandlePrice.BID, MID, ASK));
            request.setGranularity(M1);
            request.setFrom(inclusiveRange.lowerEndpoint());
            request.setTo(inclusiveRange.upperEndpoint());
            request.setIncludeFirst(true);

            List<Candlestick> candles = context.instrumentCandles(request).getCandles();

            List<InstrumentCandle> entities = candles.stream().map(it -> {
                InstrumentCandleType id = new InstrumentCandleType();
                id.setGranularity(request.getGranularity());
                id.setInstrument(request.getInstrument());
                id.setTime(it.getTime());

                InstrumentCandle candle = new InstrumentCandle();
                candle.setId(id);
                candle.setMidOpen(it.getMid().getO());
                candle.setMidHigh(it.getMid().getH());
                candle.setMidLow(it.getMid().getL());
                candle.setMidClose(it.getMid().getC());
                candle.setOpenSpread(it.getAsk().getO() - it.getBid().getO());
                candle.setHighSpread(it.getAsk().getH() - it.getBid().getH());
                candle.setLowSpread(it.getAsk().getL() - it.getBid().getL());
                candle.setCloseSpread(it.getAsk().getC() - it.getBid().getC());

                return candle;
            }).collect(toList());

            retrievedAndStored.addAll(instrumentCandleRepo.saveAll(entities));
        }

        return retrievedAndStored;
    }

    @Override
    public LocalDateTime findLatestStoredMinute() {
        LocalDateTime maxTimestamp = instrumentCandleRepo.findMaxTimestamp();
        return maxTimestamp == null ? LocalDateTime.of(2005, JANUARY, 2, 12, 29) : maxTimestamp;
    }
}
