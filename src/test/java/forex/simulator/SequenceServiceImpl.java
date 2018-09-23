package forex.simulator;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
class SequenceServiceImpl implements SequenceService {

    private final AtomicInteger transactionSequence = new AtomicInteger(1);
    private final Map<String, Integer> latestTransactionById = new HashMap<>();

    @Override
    public Integer nextTransactionId() {
        return transactionSequence.getAndIncrement();
    }

    @Override
    public Integer nextAccountTransactionID(String accountID) {
        Integer transactionId = nextTransactionId();
        latestTransactionById.put(accountID, transactionId);

        return transactionId;
    }

    @Override
    public Integer getLatestTransactionId(String accountID) {
        latestTransactionById.computeIfAbsent(accountID, it -> nextTransactionId());

        return latestTransactionById.get(accountID);
    }
}
