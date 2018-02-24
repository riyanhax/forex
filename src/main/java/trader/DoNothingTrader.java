package trader;

import broker.Broker;
import broker.Portfolio;
import broker.Position;
import broker.Quote;
import instrument.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
class DoNothingTrader implements Trader {

    private static final Logger LOG = LoggerFactory.getLogger(DoNothingTrader.class);

    @Override
    public void processUpdates(Broker broker) {
        LOG.info("\tChecking portfolio");

        Portfolio portfolio = broker.getPortfolio(this);
        double cash = portfolio.getCash();
        Set<Position> positions = portfolio.getPositions();

        LOG.info("\tCash: {}", cash);
        LOG.info("\tPositions: {}", positions);

        CurrencyPair pair = CurrencyPair.EURUSD;
        Quote quote = broker.getQuote(pair);
        LOG.info("\t{} bid: {}, ask: {}", pair.getName(), quote.getBid(), quote.getAsk());

        LOG.info("\tMaking orders");
        LOG.info("\tClosing orders");
    }
}
