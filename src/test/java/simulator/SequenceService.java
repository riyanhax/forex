package simulator;

import broker.AccountID;
import broker.TransactionID;

interface SequenceService {
    Integer nextTransactionId();

    Integer nextAccountTransactionID(AccountID accountID);

    TransactionID getLatestTransactionId(AccountID accountID);
}
