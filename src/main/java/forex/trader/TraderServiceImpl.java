package forex.trader;

import forex.broker.Account;
import forex.broker.AccountAndTrades;
import forex.broker.AccountChanges;
import forex.broker.AccountChangesRequest;
import forex.broker.AccountChangesResponse;
import forex.broker.AccountSummary;
import forex.broker.Context;
import forex.broker.Orders;
import forex.broker.RequestException;
import forex.broker.Trade;
import forex.broker.TradeListRequest;
import forex.broker.TradeListResponse;
import forex.broker.TradeSummary;
import forex.market.AccountOrderService;
import forex.market.AccountRepository;
import forex.market.MarketTime;
import forex.market.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static forex.broker.TradeStateFilter.CLOSED;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional
class TraderServiceImpl implements TraderService {

    private static final Logger LOG = LoggerFactory.getLogger(TraderServiceImpl.class);
    private final Context context;
    private final MarketTime clock;
    private final AccountRepository accountRepository;
    private final TradeRepository tradeRepository;
    private final AccountOrderService orderService;

    @Autowired
    TraderServiceImpl(Context context,
                      MarketTime clock,
                      AccountRepository accountRepository,
                      TradeRepository tradeRepository,
                      AccountOrderService orderService) {
        this.context = context;
        this.clock = clock;
        this.accountRepository = accountRepository;
        this.tradeRepository = tradeRepository;
        this.orderService = orderService;
    }

    private void refreshAccount(AccountChangesRequest request) throws RequestException {

        AccountChangesResponse response = context.accountChanges(request);

        String accountID = request.getAccountID();
        AccountSummary currentState = getAccountSummary(accountID);
        AccountSummary newState = currentState.processChanges(response);

        if (!currentState.equals(newState)) {
            accountRepository.save(newState.getAccount());
        }

        AccountChanges accountChanges = response.getAccountChanges();

        orderService.saveIfNotExists(accountChanges.getCreatedOrders());
        orderService.upsert(accountChanges.getFilledOrders());
        orderService.upsert(accountChanges.getCanceledOrders());

        List<Trade> tradesToMerge = new ArrayList<>();
        accountChanges.getTradesOpened().forEach(it ->
                tradesToMerge.add(new Trade(it)));

        List<TradeSummary> tradesClosed = accountChanges.getTradesClosed();
        if (!tradesClosed.isEmpty()) {
            Set<String> tradeIds = tradesClosed.stream().map(TradeSummary::getTradeId).collect(toSet());
            TradeListRequest tradeListRequest = new TradeListRequest(currentState.getId(), CLOSED, tradeIds);
            TradeListResponse tradeListResponse = context.listTrade(tradeListRequest);

            tradeListResponse.getTrades().stream()
                    .filter(it -> tradeIds.contains(it.getTradeId()))
                    .forEach(tradesToMerge::add);
        }

        for (Trade tradeToMerge : tradesToMerge) {
            Trade existingTrade = tradeRepository.findByAccountIdAndTradeId(tradeToMerge.getAccountId(), tradeToMerge.getTradeId());

            if (existingTrade != null) {
                tradeToMerge.setId(existingTrade.getId());
            }

            tradeRepository.save(tradeToMerge);
        }
    }

    private void initializeAccount(String accountId, int numberClosedTrades) throws RequestException {
        AccountAndTrades accountAndTrades = context.initializeAccount(accountId, numberClosedTrades);
        AccountSummary accountSummary = accountAndTrades.getAccount();

        accountRepository.save(accountSummary.getAccount());
        orderService.saveIfNotExists(accountSummary.getPendingOrders());

        for (TradeSummary openTrade : accountSummary.getTrades()) {
            Trade trade = tradeRepository.findByAccountIdAndTradeId(openTrade.getAccountId(), openTrade.getTradeId());

            if (trade == null) {
                trade = new Trade(openTrade);
                tradeRepository.save(trade);
            }
        }

        for (Trade closedTrade : accountAndTrades.getTrades()) {
            tradeRepository.save(closedTrade);
        }
    }

    @Override // TODO: Change to optional
    public AccountAndTrades accountAndTrades(String accountId, int numberClosedTrades) throws RequestException {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isPresent()) {
            refreshAccount(new AccountChangesRequest(accountOpt.get().getId(), accountOpt.get().getLastTransactionID()));
        } else {
            initializeAccount(accountId, numberClosedTrades);
        }

        accountOpt = accountRepository.findById(accountId);
        if (!accountOpt.isPresent()) {
            return null;
        }

        List<Trade> closedTrades = tradeRepository.findByAccountIdAndCloseTimeIsNotNullOrderByCloseTimeDesc(accountId, PageRequest.of(0, 10));

        return new AccountAndTrades(getAccountSummary(accountId), closedTrades);
    }

    private AccountSummary getAccountSummary(String accountID) {
        List<TradeSummary> openTrades = tradeRepository.findByAccountIdAndCloseTimeIsNull(accountID)
                .stream().map(TradeSummary::new).collect(Collectors.toList());
        Orders pendingOrders = orderService.findPendingOrders(accountID);

        return new AccountSummary(accountRepository.getOne(accountID), openTrades, pendingOrders);
    }
}
