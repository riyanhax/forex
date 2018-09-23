package forex.simulator;

interface SequenceService {
    Integer nextTransactionId();

    Integer nextAccountTransactionID(String accountID);

    Integer getLatestTransactionId(String accountID);
}
