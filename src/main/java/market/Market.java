package market;

import broker.Instrument;
import simulator.TimeAware;

public interface Market<I extends Instrument> extends TimeAware {

    double getPrice(I instrument);

    boolean isAvailable();
}
