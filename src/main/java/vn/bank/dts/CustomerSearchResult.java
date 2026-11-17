package vn.bank.dts;

import vn.bank.digitalbank.models.Customer;

public class CustomerSearchResult {

    private final boolean success;

    private final String message;

    private final String customerId;

    public CustomerSearchResult(boolean success, String message, String customerId) {
        this.success = success;
        this.message = message;
        this.customerId = customerId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getCustomerId() {
        return customerId;
    }

}
