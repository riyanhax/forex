package forex.simulator;

import forex.broker.AccountID;
import forex.broker.TransactionID;

interface SequenceService {
    Integer nextTransactionId();

    Integer nextAccountTransactionID(AccountID accountID);

    TransactionID getLatestTransactionId(AccountID accountID);
}
