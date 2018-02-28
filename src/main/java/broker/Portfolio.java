package broker;

import broker.forex.ForexPosition;

import java.util.Set;

public interface Portfolio {

    double getPipsProfit();

    Set<ForexPosition> getPositions();
}
