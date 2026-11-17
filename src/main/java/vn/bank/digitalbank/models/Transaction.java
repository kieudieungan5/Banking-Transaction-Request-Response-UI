package vn.bank.digitalbank.models;

import vn.bank.digitalbank.commons.TransactionStatus;
import vn.bank.digitalbank.commons.TransactionType;
import vn.bank.digitalbank.commons.Utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private final String id;
    private final TransactionType type;
    private final String accountDestination;
    private final String accountSource;
    private final BigDecimal amount;
    private final String time;
    private final TransactionStatus status;

    // Create Transaction read from database
    public Transaction(
            String id,
            TransactionType type,
            String accountSource,
            String accountDestination,
            BigDecimal amount,
            TransactionStatus status,
            String time) {

        this.id = id;
        this.type = type;
        this.accountSource = accountSource;
        this.accountDestination = accountDestination;
        this.amount = amount;
        this.status = status;
        this.time = time;
    }

    // Create Transaction
    public Transaction(TransactionType type,
                       String accountSource,
                       String accountDestination,
                       BigDecimal amount,
                       TransactionStatus status) {
        this.time = Utils.getDateTime();
        this.id = Utils.generateTransactionId();
        this.type = type;
        this.accountDestination = accountDestination;
        this.accountSource = accountSource;
        this.amount = amount;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public String getAccountDestination() {
        return accountDestination;
    }

    public String getAccountSource() {
        return accountSource;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTime() {
        return time;
    }

    public TransactionStatus getStatus() {
        return status;
    }
}
