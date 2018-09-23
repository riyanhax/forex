package forex.live;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import forex.broker.RequestException;
import forex.market.DataRetriever;
import forex.market.InstrumentCandle;
import forex.market.InstrumentCandleService;
import forex.market.InstrumentDataRetriever;
import forex.market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static forex.broker.Context.MAXIMUM_CANDLES_PER_RETRIEVAL;
import static forex.market.CandleTimeFrame.ONE_MINUTE;
import static java.util.Collections.emptySortedSet;
import static java.util.stream.Collectors.toList;

@Service
public class LiveInstrumentDataRetriever implements InstrumentDataRetriever {

    public static final Logger LOG = LoggerFactory.getLogger(LiveInstrumentDataRetriever.class);
    private final MarketTime clock;
    private final InstrumentCandleService service;
    private final DataRetriever<Range<LocalDateTime>, List<InstrumentCandle>> dataRetriever;

    public LiveInstrumentDataRetriever(MarketTime clock, InstrumentCandleService service) {
        this.clock = clock;
        this.service = service;
        this.dataRetriever =  new DataRetriever<>(clock, service::retrieveAndStoreOneMinuteCandles);
    }

    @Override
    public void retrieveClosedCandles() throws RequestException {

        Stopwatch timer = Stopwatch.createStarted();

        SortedSet<Range<LocalDateTime>> ranges = determineRetrievalRanges();

        List<InstrumentCandle> stored = dataRetriever.retrieve(ranges)
                .stream().flatMap(Collection::stream).collect(toList());

        LOG.info("Retrieved and stored {} one minute instrument candles for {} ranges in {}", stored.size(), ranges.size(), timer);
    }

    SortedSet<Range<LocalDateTime>> determineRetrievalRanges() {
        LocalDateTime latestStored = findLatestStoredMinute();
        LocalDateTime mostRecentCompletedCandle = ONE_MINUTE.previousCandle(clock.now());
        LocalDateTime startOfRetrievals = ONE_MINUTE.nextCandle(latestStored);

        if (latestStored.equals(mostRecentCompletedCandle)) {
            return emptySortedSet();
        }

        TreeSet<Range<LocalDateTime>> ranges = new TreeSet<>(Comparator.comparing(Range::lowerEndpoint));

        for (LocalDateTime startOfThisRange = startOfRetrievals; !startOfThisRange.isAfter(mostRecentCompletedCandle); ) {
            LocalDateTime endOfThisRange = startOfThisRange.plusMinutes(MAXIMUM_CANDLES_PER_RETRIEVAL);

            if (endOfThisRange.isAfter(mostRecentCompletedCandle)) {
                endOfThisRange = mostRecentCompletedCandle;
            }

            ranges.add(Range.closed(startOfThisRange, endOfThisRange));
            startOfThisRange = endOfThisRange.plusMinutes(1);
        }

        return ranges;
    }

    LocalDateTime findLatestStoredMinute() {
        return service.findLatestStoredMinute();
    }
}
