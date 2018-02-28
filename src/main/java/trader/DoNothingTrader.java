package trader;

import broker.PositionValue;
import broker.Quote;
import broker.forex.ForexBroker;
import broker.forex.ForexPortfolio;
import broker.forex.ForexPortfolioValue;
import market.forex.Instrument;
import market.order.OrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import simulator.Simulation;
import trader.forex.ForexTrader;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;

@Service
class DoNothingTrader implements ForexTrader {

    private static final Logger LOG = LoggerFactory.getLogger(DoNothingTrader.class);

    private String accountNo = UUID.randomUUID().toString();
    private ForexPortfolio portfolio;
    private double openedPositionValue;

    @Override
    public String getAccountNumber() {
        return accountNo;
    }

    @Override
    public void processUpdates(ForexBroker broker) {
        LOG.info("\tChecking portfolio");

        ForexPortfolioValue portfolio = broker.getPortfolioValue(this);
        double cash = portfolio.getCash();
        Set<PositionValue> positions = portfolio.getPositionValues();

        LOG.info("\tCash: {}", cash);
        LOG.info("\tPositions: {}", positions);

        if (positions.isEmpty()) {
            Instrument pair = Instrument.EURUSD;
            Quote quote = broker.getQuote(pair);
            LOG.info("\t{} bid: {}, ask: {}", pair.getName(), quote.getBid(), quote.getAsk());

            this.openedPositionValue = portfolio.marketValue();

            LOG.info("\tMaking orders");
            broker.openPosition(this, Instrument.EURUSD, 100000, null);
        } else {
            double lostValue = portfolio.marketValue() - this.openedPositionValue;
            if (lostValue < -5) {
                // Close once we've lost five dollars
                broker.closePosition(this, positions.iterator().next(), null);
            }
        }

        LOG.info("\tClosing orders");
    }

    @Override
    public void cancelled(OrderRequest cancelled) {

    }

    @Override
    public void init(Simulation simulation) {
        this.portfolio = new ForexPortfolio(simulation.portfolioDollars, emptySet());
    }

    @Override
    public ForexPortfolio getPortfolio() {
        return portfolio;
    }

    @Override
    public void setPortfolio(ForexPortfolio portfolio) {
        this.portfolio = portfolio;
    }

}
