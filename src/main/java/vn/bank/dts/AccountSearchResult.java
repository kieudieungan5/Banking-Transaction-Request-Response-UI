package vn.bank.dts;

import vn.bank.digitalbank.models.Account;

import java.util.List;

public class AccountSearchResult {

    private final boolean success;

    private final String message;

    private final List<Account> accounts;

    public AccountSearchResult(boolean success, String message, List<Account> accounts) {
        this.success = success;
        this.message = message;
        this.accounts = accounts;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<Account> getAccounts() {
        return accounts;
    }
}
