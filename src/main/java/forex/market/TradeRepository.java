package forex.market;

import forex.broker.Trade;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Integer> {
    Trade findByAccountIdAndTradeId(String accountId, String tradeId);

    List<Trade> findByAccountIdAndCloseTimeIsNull(String accountId);

    List<Trade> findByAccountIdAndCloseTimeIsNotNullOrderByCloseTimeDesc(String id, Pageable pageRequest);
}
