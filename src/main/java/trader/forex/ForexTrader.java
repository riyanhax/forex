package trader.forex;

import broker.forex.ForexBroker;
import broker.forex.ForexPortfolioValue;
import broker.forex.ForexPosition;
import broker.forex.ForexPositionValue;
import market.forex.CurrencyPair;
import market.forex.ForexMarket;
import trader.Trader;

public interface ForexTrader extends Trader<CurrencyPair, ForexMarket, ForexPosition, ForexPositionValue, ForexPortfolioValue, ForexBroker> {
}
