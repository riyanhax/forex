package forex.trader;

import forex.broker.Account;
import forex.broker.AccountAndTrades;
import forex.broker.AccountChangesRequest;
import forex.broker.AccountChangesResponse;
import forex.broker.AccountSummary;
import forex.broker.Context;
import forex.broker.RequestException;
import forex.broker.Trade;
import forex.broker.TradeListRequest;
import forex.broker.TradeListResponse;
import forex.broker.TradeSummary;
import forex.market.AccountRepository;
import forex.market.TradeRepository;
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
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional
class TraderServiceImpl implements TraderService {

    private final Context context;
    private final AccountRepository accountRepository;
    private final TradeRepository tradeRepository;

    @Autowired
    TraderServiceImpl(Context context, AccountRepository accountRepository, TradeRepository tradeRepository) {
        this.context = context;
        this.accountRepository = accountRepository;
        this.tradeRepository = tradeRepository;
    }

    private void refreshAccount(AccountChangesRequest request) throws RequestException {

        AccountChangesResponse response = context.accountChanges(request);

        AccountSummary currentState = getAccountSummary(request.getAccountID());
        AccountSummary newState = currentState.processChanges(response);

        if (!currentState.equals(newState)) {
            accountRepository.save(newState.getAccount());
        }

        List<Trade> tradesToMerge = new ArrayList<>();
        response.getAccountChanges().getTradesOpened().forEach(it ->
                tradesToMerge.add(new Trade(it)));

        List<TradeSummary> tradesClosed = response.getAccountChanges().getTradesClosed();
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

        List<TradeSummary> openTrades = tradeRepository.findByAccountIdAndCloseTimeIsNull(accountId)
                .stream().map(TradeSummary::new).collect(toList());
        List<Trade> closedTrades = tradeRepository.findByAccountIdAndCloseTimeIsNotNullOrderByCloseTimeDesc(accountId, PageRequest.of(0, 10));

        return new AccountAndTrades(new AccountSummary(accountOpt.get(), openTrades), closedTrades);
    }

    private AccountSummary getAccountSummary(String accountID) {
        List<TradeSummary> openTrades = tradeRepository.findByAccountIdAndCloseTimeIsNull(accountID)
                .stream().map(TradeSummary::new).collect(Collectors.toList());
        return new AccountSummary(accountRepository.getOne(accountID), openTrades);
    }
}
