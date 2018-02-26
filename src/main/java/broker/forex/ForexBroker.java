package broker.forex;

import broker.Broker;
import market.forex.CurrencyPair;
import market.forex.ForexMarket;

public interface ForexBroker extends Broker<CurrencyPair, ForexMarket, ForexPosition, ForexPositionValue, ForexPortfolioValue> {
}
