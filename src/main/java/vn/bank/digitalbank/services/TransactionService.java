package vn.bank.digitalbank.services;

import vn.bank.connection.Db;
import vn.bank.digitalbank.commons.TransactionStatus;
import vn.bank.digitalbank.commons.TransactionType;
import vn.bank.digitalbank.daos.AccountDAO;
import vn.bank.digitalbank.daos.CustomerDAO;
import vn.bank.digitalbank.daos.TransactionDAO;
import vn.bank.digitalbank.exceptions.BusinessException;
import vn.bank.digitalbank.exceptions.DatabaseConnectionException;
import vn.bank.digitalbank.exceptions.DatabaseOperationException;
import vn.bank.digitalbank.models.Transaction;
import vn.bank.digitalbank.models.TransactionResult;
import vn.bank.logs.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TransactionService {

    /**
     * Return list transactions of an account
     *
     * @param accountNumber Number of account
     * @return List transactions of account
     */
    public static List<Transaction> getTransactionHistory(
            String accountNumber) {

        // When account number is blank
        // throw BusinessException
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new BusinessException(
                    "Account number must be filled"
            );
        }

        // Create Connection to retrieve transaction histories
        try (Connection conn = Db.getConnection()) {

            // When account doesn't exit
            // throw BusinessException
            if (!AccountDAO.existByAccountNumber(conn, accountNumber)) {
                throw new BusinessException(
                        "Account does not exit"
                );
            }

            return TransactionDAO.getTransactionsByAccount(conn, accountNumber);


        } catch (SQLException exc) {
            throw new DatabaseConnectionException("Can not close database connection");
        }

    }

    /**
     * Transfer form an account to another account, add transaction
     * Roll back balances and add FAILED transactions if transfer can not be executed
     * Handle roll back error, insert transaction error, commit error
     *
     * @param customerId cus_id
     * @param accountSource acc_source
     * @param accountDestination acc_receive
     * @return TransactionResult Object contain state of transfer, message, transfer id
     */
    public static TransactionResult transfer(String customerId,
                                             String accountSource,
                                             String accountDestination,
                                             BigDecimal amount) {

        // 1. Create transaction before execute transfer
        Transaction successTrans = new Transaction(
                TransactionType.TRANSFER,
                accountSource,
                accountDestination,
                amount,
                TransactionStatus.SUCCESS
        );

        // 2. Execute transfer in one database connection
        try (Connection conn = Db.getConnection()) {

            // 2.1. Validate customer & account
            // Check if customer is existed
            // Check if source account is existed
            // Check if destination account is existed
            // Check if customer executing transaction owns account
            try {
                if (!CustomerDAO.existById(conn, customerId)) {
                    return new TransactionResult(TransactionStatus.FAILED,
                            "Customer does not exist",
                            null);
                }

                if (!AccountDAO.existByCustomerAndAccount(conn, customerId, accountSource)) {
                    return new TransactionResult(TransactionStatus.FAILED,
                            "Customer does not own this source account",
                            null);
                }

                if (!AccountDAO.existByAccountNumber(conn, accountSource)) {
                    return new TransactionResult(TransactionStatus.FAILED,
                            "Source account is not existed",
                            null);
                }

                if (!AccountDAO.existByAccountNumber(conn, accountDestination)) {
                    return new TransactionResult(TransactionStatus.FAILED,
                            "Destination account is not existed",
                            null);
                }

                // Input validation
                // Customer transfers between different accounts
                if (accountSource.compareTo(accountDestination) == 0) {
                    return new TransactionResult(TransactionStatus.FAILED,
                            "Can not make transaction cause account is duplicated",
                            null);
                }

                // Customer transfers valid amount
                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                    return new TransactionResult(TransactionStatus.FAILED,
                            "Amount must be greater than 0",
                            null);
                }

                // Connection error or database error
                // Write to log
            } catch (RuntimeException exc) {
                Logger.logTransaction("System error: " + exc.getMessage());

                return new TransactionResult(TransactionStatus.FAILED,
                        "System error during transaction validation",
                        null);
            }

            // 2.2. Turn off auto commit
            conn.setAutoCommit(false);

            // 3. Update balance & add transaction history
            try {
                if (!AccountDAO.subtractBalance(conn, accountSource, amount)) {
                    throw new BusinessException("Account source has not enough balance");
                }

                if (!AccountDAO.addBalance(conn, accountDestination, amount)) {
                    throw new BusinessException("Can not add balance to this account");
                }

                if (!TransactionDAO.insert(conn, successTrans)) {
                    throw new SQLException("Can not save transaction");
                }

                // 4. If error occurs before commit
                // Roll back balance changes & transaction history
            } catch (BusinessException | SQLException processingError) {

                try {
                    conn.rollback();

                    // 4.1. If roll back is not success, add this roll back error with previous processing error
                    // Roll back error do not assume transaction was undone
                    // So write it to log
                } catch (SQLException rollbackError) {
                    processingError.addSuppressed(rollbackError);

                    Logger.logTransaction(
                            "ROLLBACK outcome uncertain"
                                    + " | transactionId=" + successTrans.getId()
                                    + " | source=" + accountSource
                                    + " | destination=" + accountDestination
                                    + " | amount=" + amount
                                    + " | processingError=" + processingError.getMessage()
                                    + " | rollbackError=" + rollbackError.getMessage()
                    );

                    return new TransactionResult(TransactionStatus.UNKNOWN,
                            "Transfer result is uncertain cause rollback failed",
                            successTrans.getId());
                }

                // 4.2. If roll back successes
                // Save FAILED transaction through a seperate connection
                Transaction failedTrans = new Transaction(TransactionType.TRANSFER,
                        accountSource,
                        accountDestination,
                        amount,
                        TransactionStatus.FAILED);

                try (Connection rollBackConn = Db.getConnection()) {
                    if (!TransactionDAO.insert(rollBackConn, failedTrans)) {
                        throw new SQLException(
                                "Cannot save FAILED transaction history");
                    }

                    return new TransactionResult(
                            TransactionStatus.FAILED,
                            processingError.getMessage(),
                            failedTrans.getId()
                    );

                    // Write insert error to log
                } catch (SQLException insertError) {
                    Logger.logTransaction(
                            "FAILED history could not be saved"
                                    + " | attemptedTransactionId=" + successTrans.getId()
                                    + " | source=" + accountSource
                                    + " | destination=" + accountDestination
                                    + " | amount=" + amount
                                    + " | processingError=" + processingError.getMessage()
                                    + " | historyError=" + insertError.getMessage()
                    );

                    return new TransactionResult(TransactionStatus.FAILED,
                            "Transfer failed. Insertion of failed transaction could not be saved",
                            failedTrans.getId());
                }

            }

            // 5. Commit changes
            try {
                // 5.1. Commit success & return to application
                conn.commit();

                return new TransactionResult(
                        TransactionStatus.SUCCESS,
                        "Transfer successful",
                        successTrans.getId()
                );

                // 5.2. Commit success but return error to application
                // Do not assume that transaction is not commited
                // Write commit error to log
            } catch (SQLException commitError) {
                Logger.logTransaction(
                        "COMMIT outcome uncertain"
                                + " | transactionId=" + successTrans.getId()
                                + " | source=" + accountSource
                                + " | destination=" + accountDestination
                                + " | amount=" + amount
                                + " | error=" + commitError.getMessage()
                );

                return new TransactionResult(
                        TransactionStatus.UNKNOWN,
                        "Transfer outcome is uncertain",
                        successTrans.getId()
                );

            }

            // Database error happens
        } catch (SQLException exc) {
            return new TransactionResult(TransactionStatus.FAILED,
                    "Can't access to database",
                    null);
        }
    }

    /**
     * Withdraw from an account, add transaction
     * Roll back balances and add FAILED transactions if withdrawal can not be executed
     * Handle roll back error, insert transaction error, commit error
     *
     * @param customerId cus_id
     * @param accountSource acc_source
     * @return TransactionResult Object contain state of withdraw, message, withdraw id
     */
    public static TransactionResult withdraw(
            String customerId,
            String accountSource,
            BigDecimal amount) {

        // 1. Create transaction before processing to retain its ID
        Transaction successTrans = new Transaction(
                TransactionType.WITHDRAW,
                accountSource,
                null,
                amount,
                TransactionStatus.SUCCESS
        );

        // 2. Execute withdrawal in one database connection
        try (Connection conn = Db.getConnection()) {

            // 2.1. Validate customer and account
            try {
                if (!CustomerDAO.existById(conn, customerId)) {
                    return new TransactionResult(
                            TransactionStatus.FAILED,
                            "Customer does not exist",
                            null
                    );
                }

                if (!AccountDAO.existByCustomerAndAccount(
                        conn, customerId, accountSource)) {
                    return new TransactionResult(
                            TransactionStatus.FAILED,
                            "Customer does not own this account",
                            null
                    );
                }

                // 2.2. Validate input
                if (accountSource == null) {
                    return new TransactionResult(
                            TransactionStatus.FAILED,
                            "Account number must not be null",
                            null
                    );
                }

                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                    return new TransactionResult(
                            TransactionStatus.FAILED,
                            "Amount must be greater than 0",
                            null
                    );
                }

                // Connection or database error
                // Write to log
            } catch (RuntimeException exc) {
                Logger.logTransaction("System error: " + exc.getMessage());

                return new TransactionResult(TransactionStatus.FAILED,
                        "System error during transaction validation",
                        null);
            }

            if (!AccountDAO.existByAccountNumber(conn, accountSource)) {
                return new TransactionResult(
                        TransactionStatus.FAILED,
                        "Account does not exist",
                        null
                );
            }

            // 3. Turn off auto commit
            conn.setAutoCommit(false);

            // 4. Update balance and insert transaction history
            try {
                if (!AccountDAO.subtractBalance(
                        conn, accountSource, amount)) {
                    throw new SQLException(
                            "Account does not exist or has insufficient balance");
                }

                if (!TransactionDAO.insert(conn, successTrans)) {
                    throw new SQLException(
                            "Cannot save transaction history");
                }

                // 5. If error occurs before commit
                // Roll back balance changes & transaction history
            } catch (SQLException processingError) {

                try {
                    conn.rollback();

                    // 5.1 If roll back is not success, add this roll back error with previous processing error
                    // Roll back error do not assume transaction was undone
                    // So write it to log
                } catch (SQLException rollbackError) {
                    processingError.addSuppressed(rollbackError);

                    Logger.logTransaction(
                            "ROLLBACK outcome uncertain"
                                    + " | transactionId=" + successTrans.getId()
                                    + " | source=" + accountSource
                                    + " | amount=" + amount
                                    + " | processingError=" + processingError.getMessage()
                                    + " | rollbackError=" + rollbackError.getMessage()
                    );

                    return new TransactionResult(TransactionStatus.UNKNOWN,
                            "Withdrawal result is uncertain cause rollback failed",
                            successTrans.getId());
                }

                // 5.2. If roll back successes
                // Save FAILED transaction through a seperate connection
                Transaction failedTrans = new Transaction(TransactionType.WITHDRAW,
                        accountSource,
                        null,
                        amount,
                        TransactionStatus.FAILED);

                try (Connection rollBackConn = Db.getConnection()) {
                    if (!TransactionDAO.insert(rollBackConn, failedTrans)) {
                        throw new SQLException(
                                "Cannot save FAILED transaction history");
                    }

                    return new TransactionResult(
                            TransactionStatus.FAILED,
                            processingError.getMessage(),
                            failedTrans.getId()
                    );

                    // Write insert error to log
                } catch (SQLException insertError) {
                    Logger.logTransaction(
                            "FAILED history could not be saved"
                                    + " | attemptedTransactionId=" + successTrans.getId()
                                    + " | source=" + accountSource
                                    + " | amount=" + amount
                                    + " | processingError=" + processingError.getMessage()
                                    + " | historyError=" + insertError.getMessage()
                    );

                    return new TransactionResult(TransactionStatus.FAILED,
                            "Transfer failed. Insertion of failed transaction could not be saved",
                            failedTrans.getId());
                }

            }

            // 6. Commit separately so commit errors are not treated as ordinary processing errors.
            try {
                conn.commit();

                return new TransactionResult(
                        TransactionStatus.SUCCESS,
                        "Withdraw successful",
                        successTrans.getId()
                );

                // Commit successes but return error to application
                // Do not assume that transaction is not commited
                // Write error to log
            } catch (SQLException commitError) {
                Logger.logTransaction(
                        "COMMIT outcome uncertain"
                                + " | transactionId=" + successTrans.getId()
                                + " | source=" + accountSource
                                + " | amount=" + amount
                                + " | error=" + commitError.getMessage()
                );

                return new TransactionResult(
                        TransactionStatus.UNKNOWN,
                        "Withdraw outcome is uncertain",
                        successTrans.getId()
                );

            }

            // Database error happens
        } catch (SQLException exc) {
            return new TransactionResult(TransactionStatus.FAILED,
                    "Can't access to database",
                    null);
        }

    }

    /**
     * Deposit to account, add transaction
     * Roll back balances and add FAILED transactions if deposit can not be executed
     * Handle roll back error, insert transaction error, commit error
     *
     * @param customerId cus_id
     * @param accountSource acc_source
     * @return TransactionResult Object contain state of deposit, message, deposit id
     */
    public static TransactionResult deposit(
            String customerId,
            String accountSource,
            BigDecimal amount) {

        // 1. Create transaction before processing
        Transaction successTrans = new Transaction(
                TransactionType.DEPOSIT,
                accountSource,
                null,
                amount,
                TransactionStatus.SUCCESS
        );

        // 2. Execute deposit in one database transaction
        try (Connection conn = Db.getConnection()) {

            // Turn off auto commit
            conn.setAutoCommit(false);

            // 2.1. Validate customer and account using the same Connection
            try {
                if (!CustomerDAO.existById(conn, customerId)) {
                    return new TransactionResult(
                            TransactionStatus.FAILED,
                            "Customer does not exist",
                            null
                    );
                }

                if (!AccountDAO.existByCustomerAndAccount(
                        conn, customerId, accountSource)) {
                    return new TransactionResult(
                            TransactionStatus.FAILED,
                            "Customer does not own this account",
                            null
                    );
                }

                // 2.2. Validate input
                if (accountSource == null || accountSource.isBlank()) {
                    return new TransactionResult(
                            TransactionStatus.FAILED,
                            "Account number must not be null or blank",
                            null
                    );
                }

                if (customerId == null || customerId.isBlank()) {
                    return new TransactionResult(
                            TransactionStatus.FAILED,
                            "Customer ID must not be null or blank",
                            null
                    );
                }

                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                    return new TransactionResult(
                            TransactionStatus.FAILED,
                            "Amount must be greater than 0",
                            null
                    );
                }

                // Connection or database error happens
                // Write error information to file log
            } catch (DatabaseOperationException exc) {

                Logger.logTransaction("System error: " + exc.getMessage());

                return new TransactionResult(TransactionStatus.FAILED,
                        "System error during transaction validation",
                        null);
            }

            // 3. Update balance and insert transaction history
            // Transaction executes successfully
            try {
                if (!AccountDAO.addBalance(
                        conn, accountSource, amount)) {
                    throw new SQLException(
                            "Can not update account balance");
                }

                if (!TransactionDAO.insert(conn, successTrans)) {
                    throw new SQLException(
                            "Can not insert transaction history");
                }

                // 4. Connection / database / SQL statement error happens
                // Roll back balance changes & transaction history
            } catch (SQLException processingError) {

                try {
                    conn.rollback();

                    // 4.1. If roll back is not success, add this roll back error with previous processing error
                    // Roll back error do not assume transaction was undone
                    // So write it to log
                } catch (SQLException rollbackError) {
                    processingError.addSuppressed(rollbackError);

                    Logger.logTransaction(
                            "ROLLBACK outcome uncertain"
                                    + " | transactionId=" + successTrans.getId()
                                    + " | source=" + accountSource
                                    + " | amount=" + amount
                                    + " | processingError=" + processingError.getMessage()
                                    + " | rollbackError=" + rollbackError.getMessage()
                    );

                    return new TransactionResult(TransactionStatus.UNKNOWN,
                            "Deposit result is uncertain cause rollback failed",
                            successTrans.getId());
                }

                // 4.2. If roll back successes
                // Save FAILED transaction through a seperate connection
                Transaction failedTrans = new Transaction(TransactionType.DEPOSIT,
                        accountSource,
                        null,
                        amount,
                        TransactionStatus.FAILED);

                try (Connection rollBackConn = Db.getConnection()) {
                    if (!TransactionDAO.insert(rollBackConn, failedTrans)) {
                        throw new SQLException(
                                "Cannot save FAILED transaction history");
                    }

                    return new TransactionResult(
                            TransactionStatus.FAILED,
                            processingError.getMessage(),
                            failedTrans.getId()
                    );

                } catch (SQLException insertError) {
                    Logger.logTransaction(
                            "FAILED history could not be saved"
                                    + " | attemptedTransactionId=" + successTrans.getId()
                                    + " | source=" + accountSource
                                    + " | amount=" + amount
                                    + " | processingError=" + processingError.getMessage()
                                    + " | historyError=" + insertError.getMessage()
                    );

                    return new TransactionResult(TransactionStatus.FAILED,
                            "Deposit failed. Insertion of failed transaction could not be saved",
                            failedTrans.getId());
                }
            }

            // 4. Commit transaction
            try {
                conn.commit();

                return new TransactionResult(
                        TransactionStatus.SUCCESS,
                        "Deposit successful",
                        successTrans.getId()
                );

                // Commit successes but return error to application
                // Do not assume that transaction is not commited
                // Write error to log
            } catch (SQLException commitError) {
                Logger.logTransaction(
                        "COMMIT outcome uncertain"
                                + " | transactionId=" + successTrans.getId()
                                + " | source=" + accountSource
                                + " | amount=" + amount
                                + " | error=" + commitError.getMessage()
                );

                return new TransactionResult(
                        TransactionStatus.UNKNOWN,
                        "Deposit outcome is uncertain",
                        successTrans.getId()
                );

            }

            // Database error happens
        } catch (SQLException exc) {
            return new TransactionResult(TransactionStatus.FAILED,
                    "Can't access to database",
                    null);
        }
    }


}
