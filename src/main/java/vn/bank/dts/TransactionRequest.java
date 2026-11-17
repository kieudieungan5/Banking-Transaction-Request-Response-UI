package vn.bank.dts;

import java.math.BigDecimal;

public class TransactionRequest {
    private String customerId;
    private String accountSource;
    private String accountDestination;
    private BigDecimal amount;

    public TransactionRequest(String customerId, String sourceAcc,
                              String destAcc, BigDecimal amount) {
        this.customerId = customerId;
        this.accountSource = sourceAcc;
        this.accountDestination = destAcc;
        this.amount = amount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getAccountSource() {
        return accountSource;
    }

    public String getAccountDestination() {
        return accountDestination;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
