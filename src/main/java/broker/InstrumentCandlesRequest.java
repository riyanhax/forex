package broker;

import market.Instrument;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

public class InstrumentCandlesRequest {
    private final Instrument instrument;
    private Set<CandlePrice> price;
    private CandlestickGranularity granularity;
    private LocalDateTime from;
    private LocalDateTime to;
    private boolean includeFirst;
    private ZoneId alignmentTimezone;
    private int dailyAlignment;
    private DayOfWeek weeklyAlignment;

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

    public void setAlignmentTimezone(ZoneId alignmentTimezone) {
        this.alignmentTimezone = alignmentTimezone;
    }

    public int getDailyAlignment() {
        return dailyAlignment;
    }

    public void setDailyAlignment(int dailyAlignment) {
        this.dailyAlignment = dailyAlignment;
    }

    public DayOfWeek getWeeklyAlignment() {
        return weeklyAlignment;
    }

    public void setWeeklyAlignment(DayOfWeek weeklyAlignment) {
        this.weeklyAlignment = weeklyAlignment;
    }
}
