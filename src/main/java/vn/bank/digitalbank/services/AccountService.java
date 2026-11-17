package vn.bank.digitalbank.services;

import vn.bank.connection.Db;
import vn.bank.digitalbank.daos.AccountDAO;
import vn.bank.digitalbank.daos.CustomerDAO;
import vn.bank.digitalbank.exceptions.BusinessException;
import vn.bank.digitalbank.exceptions.DatabaseConnectionException;
import vn.bank.digitalbank.models.Account;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AccountService {

    /**
     * Return account information by customer id
     * @param customerId Id of customer
     * @return Accounts of customer
     *
     */
    public static List<Account> getAccountById(
            String customerId) {

        // When customer id is blank
        // throw BusinessException
        if (customerId == null || customerId.isBlank()) {
            throw new BusinessException(
                    "Customer ID must not be blank"
            );
        }

        // Create Connection to retrieve account
        try (Connection conn = Db.getConnection()) {

            // When customer id is not in databse
            // throw BusinessException
            if (!CustomerDAO.existById(conn, customerId)) {
                throw new BusinessException(
                        "This customer does not exit"
                );
            }

            return AccountDAO.getAccount(conn, customerId);

            // When closing the database connection fails,
            // catch SQLException and convert it into DatabaseConnectionException
        } catch (SQLException exc) {
            throw new DatabaseConnectionException("Can not close database connection");
        }

    }

    /**
     * Create new account by number of account & customer ID,
     * add to database
     * @param accountNumber Number of account
     * @param customerId Id of customer
     * @return true if create success, fall if not
     *
     */
    public static boolean createAccount(String accountNumber, String customerId) {
        // Id must have 12 digit
        if (customerId == null || !customerId.matches("\\d{12}")) {
            System.out.println("Customer Id must have 12 digits");
            return false;
        }

        // Account number must have 6 digit
        if (accountNumber == null || !accountNumber.matches("\\d{6}")) {
            System.out.println("Account number must have 6 digits");
            return false;
        }

        // Stores account into db
        boolean success = AccountDAO.insert(accountNumber, customerId);

        if (success) {
            System.out.println("Created new account");
        }

        return success;
    }
}
