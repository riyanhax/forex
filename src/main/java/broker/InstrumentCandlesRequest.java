package broker;

import market.Instrument;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

public class InstrumentCandlesRequest {
    private Instrument instrument;
    private Set<CandlePrice> price;
    private CandlestickGranularity granularity;
    private LocalDateTime from;
    private LocalDateTime to;
    private boolean includeFirst;
    private ZoneId alignmentTimezone;
    private int dailyAlignment;
    private DayOfWeek weeklyAlignment;
    private int count;

    public InstrumentCandlesRequest() {
    }

    public InstrumentCandlesRequest(Instrument instrument) {
        this.instrument = instrument;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public Set<CandlePrice> getPrice() {
        return price;
    }

    public void setPrice(Set<CandlePrice> price) {
        this.price = price;
    }

    public CandlestickGranularity getGranularity() {
        return granularity;
    }

    public void setGranularity(CandlestickGranularity granularity) {
        this.granularity = granularity;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public void setTo(LocalDateTime to) {
        this.to = to;
    }

    public boolean isIncludeFirst() {
        return includeFirst;
    }

    public void setIncludeFirst(boolean includeFirst) {
        this.includeFirst = includeFirst;
    }

    public ZoneId getAlignmentTimezone() {
        return alignmentTimezone;
    }

    /**
     * The timezone to use for the dailyAlignment parameter. Candlesticks with
     * daily alignment will be aligned to the dailyAlignment hour within the
     * alignmentTimezone.
     */
    public void setAlignmentTimezone(ZoneId alignmentTimezone) {
        this.alignmentTimezone = alignmentTimezone;
    }

    public int getDailyAlignment() {
        return dailyAlignment;
    }

    /**
     * The hour of the day (in the specified timezone) to use for granularities
     * that have daily alignments.
     */
    public void setDailyAlignment(int dailyAlignment) {
        this.dailyAlignment = dailyAlignment;
    }

    public DayOfWeek getWeeklyAlignment() {
        return weeklyAlignment;
    }

    /**
     * The day of the week used for granularities that have weekly alignment.
     */
    public void setWeeklyAlignment(DayOfWeek weeklyAlignment) {
        this.weeklyAlignment = weeklyAlignment;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
