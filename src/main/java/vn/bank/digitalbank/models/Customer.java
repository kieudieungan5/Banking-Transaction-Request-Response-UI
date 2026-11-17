package vn.bank.digitalbank.models;

public class Customer{

    private final String name;

    private final String customerId;

    public String getName() {
        return name;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Customer(String customerId) {
        this.customerId = customerId;
        this.name = null;
    }

    public Customer(String customerId, String name) {
        this.name = name;
        this.customerId = customerId;
    }

}
