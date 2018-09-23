package forex.simulator;

import forex.broker.TransactionID;

interface SequenceService {
    Integer nextTransactionId();

    Integer nextAccountTransactionID(String accountID);

    TransactionID getLatestTransactionId(String accountID);
}
