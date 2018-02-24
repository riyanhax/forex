package market;

import broker.Instrument;
import simulator.TimeAware;

public interface Market extends TimeAware {

    double getPrice(Instrument instrument);

}
