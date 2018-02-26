package trader;

import broker.Quote;
import broker.forex.ForexBroker;
import broker.forex.ForexPortfolioValue;
import broker.forex.ForexPosition;
import market.forex.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import trader.forex.ForexTrader;

import java.util.Set;
import java.util.UUID;

@Service
class DoNothingTrader implements ForexTrader {

    private static final Logger LOG = LoggerFactory.getLogger(DoNothingTrader.class);

    private String accountNo = UUID.randomUUID().toString();

    @Override
    public String getAccountNumber() {
        return accountNo;
    }

    @Override
    public void processUpdates(ForexBroker broker) {
        LOG.info("\tChecking portfolio");

        ForexPortfolioValue portfolio = broker.getPortfolio(this);
        double cash = portfolio.getCash();
        Set<ForexPosition> positions = portfolio.getPositions();

        LOG.info("\tCash: {}", cash);
        LOG.info("\tPositions: {}", positions);

        CurrencyPair pair = CurrencyPair.EURUSD;
        Quote quote = broker.getQuote(pair);
        LOG.info("\t{} bid: {}, ask: {}", pair.getName(), quote.getBid(), quote.getAsk());

        LOG.info("\tMaking orders");
        LOG.info("\tClosing orders");
    }

}
