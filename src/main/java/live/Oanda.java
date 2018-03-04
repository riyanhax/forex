package live;

import broker.ForexBroker;
import broker.OpenPositionRequest;
import broker.Quote;
import broker.Stance;
import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.Price;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.transaction.StopLossDetails;
import com.oanda.v20.transaction.TakeProfitDetails;
import market.ForexPortfolio;
import market.ForexPortfolioValue;
import market.ForexPosition;
import market.ForexPositionValue;
import market.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import trader.ForexTrader;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Collections.emptySet;
import static java.util.Collections.emptySortedSet;

@Service
class Oanda implements ForexBroker {

    private static final Logger LOG = LoggerFactory.getLogger(Oanda.class);
    private final ForexTrader trader;
    private final Context ctx;
    private final AccountID accountId;

    public Oanda(OandaProperties properties, ForexTrader trader) {
        this.ctx = new Context(properties.getApi().getEndpoint(), properties.getApi().getToken());
        this.accountId = new AccountID(properties.getApi().getAccount());
        this.trader = trader;
    }

    @Override
    public ForexPortfolioValue getPortfolioValue(ForexTrader trader) throws Exception {
        try {
            AccountSummary summary = ctx.account.summary(accountId).getAccount();
            LOG.info(summary.toString());

            Set<ForexPosition> positions = emptySet();
            SortedSet<ForexPositionValue> closedTrades = emptySortedSet();
            ForexPortfolio portfolio = new ForexPortfolio(0, positions, closedTrades);

            // TODO: Make all of this stuff use actual position data
            if (summary.getOpenTradeCount() != 0) {
                positions = Collections.singleton(new ForexPosition(LocalDateTime.now(), Instrument.EURUSD, Stance.LONG, 0));
                closedTrades = new TreeSet<>();
                closedTrades.add(new ForexPositionValue(positions.iterator().next(), LocalDateTime.now(), 0));
            }
            return new ForexPortfolioValue(portfolio, LocalDateTime.now(), closedTrades);
        } catch (Exception e) {
           throw e;
        }
    }

    @Override
    public Quote getQuote(Instrument pair) throws Exception {
        String symbol = pair.getSymbol();

        PricingGetRequest request = new PricingGetRequest(accountId, Collections.singletonList(symbol));
        PricingGetResponse resp = ctx.pricing.get(request);
        List<Price> prices = resp.getPrices();

        if (prices.isEmpty()) {
            throw new IllegalStateException("Prices were empty!");
        }

        Price price = prices.iterator().next();
        LOG.info("Current price for {}: {}", symbol, price.toString());

        return new OandaQuote(price);
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isClosed(LocalDate time) {
        return true;
    }

    @Override
    public void openPosition(ForexTrader trader, OpenPositionRequest request) throws Exception {
        Instrument pair = request.getPair();
        Quote quote = getQuote(pair);
        String symbol = pair.getSymbol();

        MarketOrderRequest marketOrderRequest = new MarketOrderRequest();
        marketOrderRequest.setInstrument(symbol);
        marketOrderRequest.setUnits(1);

        request.getStopLoss().ifPresent(stop -> {
            StopLossDetails stopLoss = new StopLossDetails();
            stopLoss.setPrice(quote.getBid() - stop);
            marketOrderRequest.setStopLossOnFill(stopLoss);
        });

        request.getTakeProfit().ifPresent(profit -> {
            TakeProfitDetails takeProfit = new TakeProfitDetails();
            takeProfit.setPrice(quote.getBid() + profit);
            marketOrderRequest.setTakeProfitOnFill(takeProfit);
        });

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(accountId);
        orderCreateRequest.setOrder(marketOrderRequest);

        OrderCreateResponse orderCreateResponse = ctx.order.create(orderCreateRequest);
        LOG.info(orderCreateResponse.toString());
    }

    @Override
    public void closePosition(ForexTrader trader, ForexPosition position, @Nullable Double limit) {

    }

    @Override
    public void processUpdates() throws Exception {
        if (isClosed()) {
            LOG.info("Market is closed.");
            return;
        }

        trader.processUpdates(this);
    }
}
