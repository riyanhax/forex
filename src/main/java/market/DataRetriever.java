package market;

import com.google.common.collect.Range;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import static broker.Context.MAXIMUM_CANDLES_PER_RETRIEVAL;
import static java.util.Collections.emptySortedSet;
import static market.CandleTimeFrame.ONE_MINUTE;

public class DataRetriever {

    private final MarketTime clock;

    public DataRetriever(MarketTime clock) {
        this.clock = clock;
    }

    public SortedSet<Range<LocalDateTime>> determineRetrievalRanges() {
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
        return null;
    }
}
