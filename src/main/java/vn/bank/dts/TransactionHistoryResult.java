package vn.bank.dts;

import vn.bank.digitalbank.models.Transaction;

import java.util.List;

public class TransactionHistoryResult {

    private final boolean success;

    private final String message;

    private final List<Transaction> transactions;

    public TransactionHistoryResult(boolean success, String message, List<Transaction> transactions) {
        this.success = success;
        this.message = message;
        this.transactions = transactions;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
