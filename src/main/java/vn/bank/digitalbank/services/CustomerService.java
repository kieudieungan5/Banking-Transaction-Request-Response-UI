package vn.bank.digitalbank.services;

import vn.bank.connection.Db;
import vn.bank.digitalbank.daos.AccountDAO;
import vn.bank.digitalbank.daos.CustomerDAO;
import vn.bank.digitalbank.exceptions.BusinessException;
import vn.bank.digitalbank.exceptions.DatabaseConnectionException;
import vn.bank.digitalbank.models.Customer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CustomerService {

    /**
     * Return customer using input account number
     *
     * @param accountNumber Number of account
     * @return Customer
     */
    public static Customer getCustomerByAccount(
            String accountNumber) {

        // When account number is blank
        // throw BusinessException
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new BusinessException(
                    "Account number must not be blank"
            );
        }

        // Create Connection to retrieve customer information
        try (Connection conn = Db.getConnection()) {

            // When account doesn't exit
            // throw BusinessException
            if (!AccountDAO.existByAccountNumber(conn, accountNumber)) {
                throw new BusinessException(
                        "This account does not exit"
                );
            }

            return CustomerDAO.getCustomerByAccount(conn, accountNumber);

            // When closing the database connection fails,
            // catch SQLException and convert it into DatabaseConnectionException
        } catch (SQLException exc) {
            throw new DatabaseConnectionException("Can not close database connection");
        }

    }

    /**
     *
     * @param customerId
     * @param name
     * @return
     */
    public static boolean createCustomer(String customerId, String name) {
        // ID must have 12 digit
        if (customerId == null || !customerId.matches("\\d{12}")) {
            System.out.println("Customer Id must have 12 digit");
            return false;
        }

        // Name must not be blank
        if (name == null || name.isBlank()) {
            System.out.println("Name must be filled");
            return false;
        }

        // Store customer into db
        boolean success = CustomerDAO.insert(customerId, name);

        if (success) {
            System.out.println("Congratulate new customer");
        }

        return success;
    }

    /**
     *
     * @return
     */
    public static List<Customer> showAllCustomers() {
        List<Customer> customers;
        try {
            customers = CustomerDAO.showAll();
        } catch (RuntimeException exc) {
            System.out.println(exc.getMessage());
            return null;
        }
        return customers;
    }

}
