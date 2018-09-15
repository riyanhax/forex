package market;

import com.google.common.base.MoreObjects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Table(name = "instrument_candle")
@Entity
public class InstrumentCandle {

    @EmbeddedId
    private InstrumentCandleType id;

    private long midOpen;
    private long midHigh;
    private long midLow;
    private long midClose;
    private long openSpread;
    private long highSpread;
    private long lowSpread;
    private long closeSpread;

    public InstrumentCandleType getId() {
        return id;
    }

    public void setId(InstrumentCandleType id) {
        this.id = id;
    }

    public long getMidOpen() {
        return midOpen;
    }

    public void setMidOpen(long midOpen) {
        this.midOpen = midOpen;
    }

    public long getMidHigh() {
        return midHigh;
    }

    public void setMidHigh(long midHigh) {
        this.midHigh = midHigh;
    }

    public long getMidLow() {
        return midLow;
    }

    public void setMidLow(long midLow) {
        this.midLow = midLow;
    }

    public long getMidClose() {
        return midClose;
    }

    public void setMidClose(long midClose) {
        this.midClose = midClose;
    }

    public long getOpenSpread() {
        return openSpread;
    }

    public void setOpenSpread(long openSpread) {
        this.openSpread = openSpread;
    }

    public long getHighSpread() {
        return highSpread;
    }

    public void setHighSpread(long highSpread) {
        this.highSpread = highSpread;
    }

    public long getLowSpread() {
        return lowSpread;
    }

    public void setLowSpread(long lowSpread) {
        this.lowSpread = lowSpread;
    }

    public long getCloseSpread() {
        return closeSpread;
    }

    public void setCloseSpread(long closeSpread) {
        this.closeSpread = closeSpread;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstrumentCandle candle = (InstrumentCandle) o;
        return midOpen == candle.midOpen &&
                midHigh == candle.midHigh &&
                midLow == candle.midLow &&
                midClose == candle.midClose &&
                openSpread == candle.openSpread &&
                highSpread == candle.highSpread &&
                lowSpread == candle.lowSpread &&
                closeSpread == candle.closeSpread &&
                Objects.equals(id, candle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, midOpen, midHigh, midLow, midClose, openSpread, highSpread, lowSpread, closeSpread);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("midOpen", midOpen)
                .add("midHigh", midHigh)
                .add("midLow", midLow)
                .add("midClose", midClose)
                .add("openSpread", openSpread)
                .add("highSpread", highSpread)
                .add("lowSpread", lowSpread)
                .add("closeSpread", closeSpread)
                .toString();
    }
}
