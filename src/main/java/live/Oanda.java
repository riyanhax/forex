package live;

import broker.ForexBroker;
import broker.OpenPositionRequest;
import broker.Quote;
import broker.Stance;
import com.oanda.v20.Context;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.Account;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.position.PositionSide;
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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Collections.emptySortedSet;

@Service
class Oanda implements ForexBroker {

    private static final Logger LOG = LoggerFactory.getLogger(Oanda.class);
    private final ForexTrader trader;
    private final Context ctx;
    private final AccountID accountId;
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.#####");

    static {
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
    }

    public Oanda(OandaProperties properties, ForexTrader trader) throws ExecuteException, RequestException {
        this.ctx = new Context(properties.getApi().getEndpoint(), properties.getApi().getToken());
        this.accountId = new AccountID(properties.getApi().getAccount());
        this.trader = trader;
    }

    @Override
    public ForexPortfolioValue getPortfolioValue(ForexTrader trader) throws Exception {
        try {
            Account account = ctx.account.get(this.accountId).getAccount();

            Set<ForexPosition> positions = account.getPositions().stream()
                    .filter(it -> it.getLong().getUnits().doubleValue() != 0d ||
                            it.getShort().getUnits().doubleValue() != 0d)
                    .map(it -> {
                        Instrument pair = Instrument.bySymbol.get(it.getInstrument().toString());
                        PositionSide positionSide = it.getLong();
                        boolean inverse = false;

                        if (positionSide.getUnits().doubleValue() == 0d) {
                            positionSide = it.getShort();
                            pair = pair.getOpposite();
                            inverse = true;
                        }

                        double price = positionSide.getAveragePrice().doubleValue();
                        if (inverse) {
                            price = 1 / price;
                        }

                        // TODO: Parse real opened date
                        LocalDateTime opened = LocalDateTime.now();
                        return new ForexPosition(opened, pair, Stance.LONG, price);
                    }).collect(Collectors.toSet());

            SortedSet<ForexPositionValue> closedTrades = emptySortedSet();
            ForexPortfolio portfolio = new ForexPortfolio(account.getPl().doubleValue(), positions, closedTrades);

            if (positions.size() != 0) {
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
        return false;
    }

    @Override
    public void openPosition(ForexTrader trader, OpenPositionRequest request) throws Exception {
        Instrument pair = request.getPair();

        boolean shorting = pair.isInverse();
        if (shorting) {
            pair = pair.getOpposite();
        }

        Quote quote = getQuote(pair);
        String symbol = pair.getSymbol();
        double basePrice = shorting ? quote.getAsk() : quote.getBid();
        double pip = pair.getPip();

        MarketOrderRequest marketOrderRequest = new MarketOrderRequest();
        marketOrderRequest.setInstrument(symbol);
        marketOrderRequest.setUnits(shorting ? -1 : 1);

        request.getStopLoss().ifPresent(stop -> {
            StopLossDetails stopLoss = new StopLossDetails();
            stopLoss.setPrice(roundToFiveDecimalPlaces(basePrice - stop * pip * (shorting ? -1 : 1)));
            marketOrderRequest.setStopLossOnFill(stopLoss);
        });

        request.getTakeProfit().ifPresent(profit -> {
            TakeProfitDetails takeProfit = new TakeProfitDetails();
            takeProfit.setPrice(roundToFiveDecimalPlaces(basePrice + profit * pip * (shorting ? -1 : 1)));
            marketOrderRequest.setTakeProfitOnFill(takeProfit);
        });

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(accountId);
        orderCreateRequest.setOrder(marketOrderRequest);

        try {
            OrderCreateResponse orderCreateResponse = ctx.order.create(orderCreateRequest);
            LOG.info(orderCreateResponse.toString());
        } catch (RequestException e) {
            throw new Exception(e.getErrorMessage(), e);
        }
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

    private static String roundToFiveDecimalPlaces(double value) {
        return decimalFormat.format(value);
    }
}
