package trader;

import broker.Quote;
import broker.forex.ForexBroker;
import broker.forex.ForexPortfolio;
import broker.forex.ForexPortfolioValue;
import broker.forex.ForexPositionValue;
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
        double profit = portfolio.getPipsProfit();
        Set<ForexPositionValue> positions = portfolio.getPositionValues();

        LOG.info("\tPips: {}", profit);
        LOG.info("\tPositions: {}", positions);

        Instrument pair = Instrument.EURUSD;
        Quote quote = broker.getQuote(pair);
        LOG.info("\t{} bid: {}, ask: {}", pair.getName(), quote.getBid(), quote.getAsk());

        if (positions.isEmpty()) {

            this.openedPositionValue = quote.getAsk();

            LOG.info("\tMaking orders");
            broker.openPosition(this, Instrument.EURUSD, null);
        } else {
            ForexPositionValue positionValue = positions.iterator().next();
            double pipsProfit = positionValue.pips();

            // Close once we've lost or gained enough pips
            if (pipsProfit < -30 || pipsProfit > 60) {
                broker.closePosition(this, positionValue.getPosition(), null);
            }
        }

        LOG.info("\tClosing orders");
    }

    @Override
    public void cancelled(OrderRequest cancelled) {

    }

    @Override
    public void init(Simulation simulation) {
        this.portfolio = new ForexPortfolio(0, emptySet());
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
