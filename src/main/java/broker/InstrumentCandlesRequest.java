package broker;

import java.time.DayOfWeek;

public class InstrumentCandlesRequest {
    private final String instrument;
    private String price;
    private CandlestickGranularity granularity;
    private String from;
    private String to;
    private boolean includeFirst;
    private String alignmentTimezone;
    private int dailyAlignment;
    private DayOfWeek weeklyAlignment;

    public InstrumentCandlesRequest(String instrument) {
        this.instrument = instrument;
    }

    public String getInstrument() {
        return instrument;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public CandlestickGranularity getGranularity() {
        return granularity;
    }

    public void setGranularity(CandlestickGranularity granularity) {
        this.granularity = granularity;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public boolean isIncludeFirst() {
        return includeFirst;
    }

    public void setIncludeFirst(boolean includeFirst) {
        this.includeFirst = includeFirst;
    }

    public String getAlignmentTimezone() {
        return alignmentTimezone;
    }

    public void setAlignmentTimezone(String alignmentTimezone) {
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
