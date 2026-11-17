package vn.bank.digitalbank.models;

import java.math.BigDecimal;
import java.util.Objects;

public class Account {

    private final String customerId;
    private final String accountNumber;
    private BigDecimal balance;

    public Account(String customerId, String accountNumber, BigDecimal balance) {

        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.balance = balance;

    }

    public String getCustomerId() {
        return customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return customerId + " | "
                + accountNumber + " | "
                + balance + "đ";
    }

    @Override
    public boolean equals(Object obj) { // khai báo mặc định của hàm equals()

        if (this == obj) return true;

        if (!(obj instanceof Account other)) return false;

        return Objects.equals(customerId, other.customerId)
                && Objects.equals(accountNumber, other.accountNumber)
                && Objects.equals(balance, other.balance);
    }

}
