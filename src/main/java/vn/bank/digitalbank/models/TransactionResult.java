package vn.bank.digitalbank.models;

import vn.bank.digitalbank.commons.TransactionStatus;

public class TransactionResult {
    private TransactionStatus status;
    private String message;
    private String transactionId;

    public TransactionResult(TransactionStatus status,
                             String message,
                             String transactionId) {
        this.status = status;
        this.message = message;
        this.transactionId = transactionId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
