package vn.bank.digitalbank.services;

import org.mockito.Mockito;
import vn.bank.connection.Db;
import vn.bank.digitalbank.commons.TransactionStatus;
import vn.bank.digitalbank.daos.AccountDAO;
import vn.bank.digitalbank.daos.CustomerDAO;
import vn.bank.digitalbank.daos.TransactionDAO;
import vn.bank.digitalbank.models.Transaction;
import vn.bank.digitalbank.models.TransactionResult;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * This class test function TransactionService.deposit
 * Test contains: deposit successfully,
 *
 *
 */
class TransactionServiceDepositTest {

    private static final String CUSTOMER_ID = "000000000001";
    private static final String ACCOUNT = "000001";
    private static final BigDecimal AMOUNT = new BigDecimal("5000000");

    /**
     * Test: Verify that function deposit correctly handles a successful deposit
     * @throws SQLException This exception is thrown if error happens when commit & rollback
     *
     */
    @Test
    void testDeposit_Success() throws SQLException {

        Connection conn = mock(Connection.class);

        try (MockedStatic<Db> dbMock = mockStatic(Db.class);
             MockedStatic<CustomerDAO> customerMock = mockStatic(CustomerDAO.class);
             MockedStatic<AccountDAO> accountMock = mockStatic(AccountDAO.class);
             MockedStatic<TransactionDAO> transactionMock = mockStatic(TransactionDAO.class)) {

            // Mock Connection: success
            dbMock.when(Db::getConnection).thenReturn(conn);

            // Mock customer exists: true & account is owned by customer: true
            customerMock.when(() ->
                    CustomerDAO.existById(conn, CUSTOMER_ID)
            ).thenReturn(true);

            accountMock.when(() ->
                    AccountDAO.existByCustomerAndAccount(conn, CUSTOMER_ID, ACCOUNT)
            ).thenReturn(true);

            // Mock calling add amount to balance & insert transaction to database: success
            accountMock.when(() ->
                    AccountDAO.addBalance(conn, ACCOUNT, AMOUNT)
            ).thenReturn(true);

            transactionMock.when(() ->
                    TransactionDAO.insert(
                            Mockito.eq(conn),
                            Mockito.any(Transaction.class))
            ).thenReturn(true);

            // Call deposit, this function call uses results from mocking call
            TransactionResult result = TransactionService.deposit(
                    CUSTOMER_ID, ACCOUNT, AMOUNT
            );

            // Verify if function executed success & return success status, ID
            assertEquals(TransactionStatus.SUCCESS, result.getStatus());
            assertNotNull(result.getTransactionId());

            // Verify if function AccountDAO.addBalance was executed
            accountMock.verify(() ->
                    AccountDAO.addBalance(conn, ACCOUNT, AMOUNT)
            );

            // Verify if commit was called & rollback was never called
            verify(conn).commit();

            verify(conn, never()).rollback();
        }
    }

    /**
     * Test: If account number is null or contains oly spaces,
     * deposit must fail by returning a failed TransactionResult
     *
     */
    @Test
    void testDeposit_NullBlankAccountNumber() {

        TransactionResult resultNull = TransactionService.deposit(
                CUSTOMER_ID, null, AMOUNT
        );

        TransactionResult resultBlank = TransactionService.deposit(
                CUSTOMER_ID, "   ", AMOUNT
        );

        assertEquals(TransactionStatus.FAILED, resultNull.getStatus());
        assertEquals(TransactionStatus.FAILED, resultBlank.getStatus());

    }

    /**
     * Test: If customer ID is null or contains oly spaces,
     * deposit must fail by returning a failed TransactionResult
     *
     */
    @Test
    void testDeposit_NullBlankCustomerId() {

        TransactionResult resultNull = TransactionService.deposit(
                null, ACCOUNT, AMOUNT
        );

        TransactionResult resultBlank = TransactionService.deposit(
                "   ", ACCOUNT, AMOUNT
        );

        assertEquals(TransactionStatus.FAILED, resultNull.getStatus());
        assertEquals(TransactionStatus.FAILED, resultBlank.getStatus());

    }

    /**
     * Test: If amount is not valid,
     * deposit must fail by returning a failed TransactionResult
     *
     */
    @Test
    void testDeposit_InvalidAmount() {

        TransactionResult nullAmount = TransactionService.deposit(
                CUSTOMER_ID, ACCOUNT, null
        );

        TransactionResult zeroAmount = TransactionService.deposit(
                CUSTOMER_ID, ACCOUNT, BigDecimal.ZERO
        );

        TransactionResult negativeAmount = TransactionService.deposit(
                CUSTOMER_ID, ACCOUNT, new BigDecimal("-10000000")
        );

        assertEquals(TransactionStatus.FAILED, nullAmount.getStatus());
        assertEquals(TransactionStatus.FAILED, zeroAmount.getStatus());
        assertEquals(TransactionStatus.FAILED, negativeAmount.getStatus());
    }

    /**
     * Test: Customer doesn't exist,
     * deposit must fail by returning a failed TransactionResult
     *
     */
    @Test
    void testDeposit_CustomerNotFound() throws SQLException {

        Connection conn = mock(Connection.class);

        try (
                MockedStatic<Db> dbMock = mockStatic(Db.class);
                MockedStatic<CustomerDAO> customerMock = mockStatic(CustomerDAO.class)) {

            // Mock Connection: success
            dbMock.when(Db::getConnection).thenReturn(conn);

            // Mock customer exists: false
            customerMock.when(() ->
                    CustomerDAO.existById(conn, CUSTOMER_ID)
            ).thenReturn(false);

            // Verify if function return failed transaction
            TransactionResult result = TransactionService.deposit(
                    CUSTOMER_ID, ACCOUNT, AMOUNT
            );

            assertEquals(TransactionStatus.FAILED, result.getStatus());

            // Verify DAO was called
            customerMock.verify(() ->
                    CustomerDAO.existById(conn, CUSTOMER_ID)
            );

            // Verify Connection was closed
            verify(conn).close();
        }
    }

    /**
     * Test: Customer doesn't own account,
     * deposit must fail by returning a failed TransactionResult
     *
     */
    @Test
    void testDeposit_CustomerDoesNotOwnAccount() {

        Connection conn = mock(Connection.class);

        try (
                MockedStatic<Db> dbMock = mockStatic(Db.class);
                MockedStatic<AccountDAO> accountMock = mockStatic(AccountDAO.class);
                MockedStatic<CustomerDAO> customerMock = mockStatic(CustomerDAO.class)) {

            // Mock Connection: success
            dbMock.when(Db::getConnection).thenReturn(conn);

            // Mock customer exists: true
            customerMock.when(() ->
                    CustomerDAO.existById(conn, CUSTOMER_ID)
            ).thenReturn(true);

            // Mock customer owns account: false
            accountMock.when(() ->
                    AccountDAO.existByCustomerAndAccount(conn, CUSTOMER_ID, ACCOUNT)
            ).thenReturn(false);

            // Verify if function return failed transaction
            TransactionResult result = TransactionService.deposit(
                    CUSTOMER_ID, ACCOUNT, AMOUNT
            );

            assertEquals(TransactionStatus.FAILED, result.getStatus());
        }
    }

    /**
     * Test: Add balance failed, must throws SQLException, roll back, store failed transaction
     *
     */
    @Test
    void testDeposit_AddBalanceFailed() throws SQLException {

        Connection conn = mock(Connection.class);
        Connection rollBackConn = mock(Connection.class);

        try (
                MockedStatic<Db> dbMock = mockStatic(Db.class);
                MockedStatic<AccountDAO> accountMock = mockStatic(AccountDAO.class);
                MockedStatic<CustomerDAO> customerMock = mockStatic(CustomerDAO.class);
                MockedStatic<TransactionDAO> transactionMock = mockStatic(TransactionDAO.class)) {

            dbMock.when(Db::getConnection).thenReturn(conn, rollBackConn);

            customerMock.when(() ->
                            CustomerDAO.existById(conn, CUSTOMER_ID))
                    .thenReturn(true);

            accountMock.when(() ->
                            AccountDAO.existByCustomerAndAccount(
                                    conn, CUSTOMER_ID, ACCOUNT))
                    .thenReturn(true);

            // Mock addBalance: failed
            accountMock.when(() ->
                            AccountDAO.addBalance(conn, ACCOUNT, AMOUNT))
                    .thenReturn(false);

            // Mock insert transaction: true;
            transactionMock.when(() ->
                            TransactionDAO.insert(
                                    Mockito.eq(rollBackConn),
                                    Mockito.any(Transaction.class)))
                    .thenReturn(true);

            TransactionResult result = TransactionService.deposit(
                    CUSTOMER_ID, ACCOUNT, AMOUNT);

            // Verify transaction is failed
            assertEquals(TransactionStatus.FAILED, result.getStatus());

            // Balance was added, so Service must roll back
            verify(conn).rollback();

            // Main transaction must not be committed
            verify(conn, never()).commit();

            // Verify SUCCESS transaction was attempted
            transactionMock.verify(() ->
                            TransactionDAO.insert(
                                    Mockito.eq(conn),
                                    Mockito.argThat(t ->
                                            t.getStatus() == TransactionStatus.SUCCESS)),
                    never());

            // Verify FAILED transaction was stored using a separate connection
            transactionMock.verify(() ->
                    TransactionDAO.insert(
                            Mockito.eq(rollBackConn),
                            Mockito.argThat(t ->
                                    t.getStatus() == TransactionStatus.FAILED)));

        }
    }


    /**
     * Test: Add balance succeeds, but inserting SUCCESS transaction fails.
     * Must throw SQLException, roll back, and store FAILED transaction.
     */
    @Test
    void testDeposit_InsertSuccessHistoryFailed() throws SQLException {

        Connection conn = mock(Connection.class);
        Connection rollBackConn = mock(Connection.class);

        try (
                MockedStatic<Db> dbMock = mockStatic(Db.class);
                MockedStatic<AccountDAO> accountMock = mockStatic(AccountDAO.class);
                MockedStatic<CustomerDAO> customerMock = mockStatic(CustomerDAO.class);
                MockedStatic<TransactionDAO> transactionMock = mockStatic(TransactionDAO.class)) {

            dbMock.when(Db::getConnection).thenReturn(conn, rollBackConn);

            customerMock.when(() ->
                            CustomerDAO.existById(conn, CUSTOMER_ID))
                    .thenReturn(true);

            accountMock.when(() ->
                            AccountDAO.existByCustomerAndAccount(
                                    conn, CUSTOMER_ID, ACCOUNT))
                    .thenReturn(true);

            // Mock addBalance: success
            accountMock.when(() ->
                            AccountDAO.addBalance(conn, ACCOUNT, AMOUNT))
                    .thenReturn(true);

            // Mock insert SUCCESS transaction: false
            transactionMock.when(() ->
                            TransactionDAO.insert(
                                    Mockito.eq(conn),
                                    Mockito.argThat(t ->
                                            t.getStatus() == TransactionStatus.SUCCESS)))
                    .thenReturn(false);

            // Mock insert FAILED transaction: true
            transactionMock.when(() ->
                            TransactionDAO.insert(
                                    Mockito.eq(rollBackConn),
                                    Mockito.argThat(t ->
                                            t.getStatus() == TransactionStatus.FAILED)))
                    .thenReturn(true);

            TransactionResult result = TransactionService.deposit(
                    CUSTOMER_ID, ACCOUNT, AMOUNT);

            // Verify transaction is failed
            assertEquals(TransactionStatus.FAILED, result.getStatus());

            // Balance was added, so Service must roll back
            verify(conn).rollback();

            // Main transaction must not be committed
            verify(conn, never()).commit();

            // Verify addBalance was called
            accountMock.verify(() ->
                    AccountDAO.addBalance(conn, ACCOUNT, AMOUNT));

            // Verify SUCCESS transaction was attempted
            transactionMock.verify(() ->
                    TransactionDAO.insert(
                            Mockito.eq(conn),
                            Mockito.argThat(t ->
                                    t.getStatus() == TransactionStatus.SUCCESS)));

            // Verify FAILED transaction was stored using a separate connection
            transactionMock.verify(() ->
                    TransactionDAO.insert(
                            Mockito.eq(rollBackConn),
                            Mockito.argThat(t ->
                                    t.getStatus() == TransactionStatus.FAILED)));
        }
    }

    /**
     * Test: Rollback succeeds after inserting SUCCESS transaction fails,
     * must store FAILED transaction using a separate connection.
     */
    @Test
    void testDeposit_RollbackSuccess() throws SQLException {

        Connection conn = mock(Connection.class);
        Connection rollBackConn = mock(Connection.class);

        try (
                MockedStatic<Db> dbMock = mockStatic(Db.class);
                MockedStatic<AccountDAO> accountMock = mockStatic(AccountDAO.class);
                MockedStatic<CustomerDAO> customerMock = mockStatic(CustomerDAO.class);
                MockedStatic<TransactionDAO> transactionMock = mockStatic(TransactionDAO.class)) {

            dbMock.when(Db::getConnection).thenReturn(conn, rollBackConn);

            customerMock.when(() ->
                            CustomerDAO.existById(conn, CUSTOMER_ID))
                    .thenReturn(true);

            accountMock.when(() ->
                            AccountDAO.existByCustomerAndAccount(
                                    conn, CUSTOMER_ID, ACCOUNT))
                    .thenReturn(true);

            // Add balance succeeds
            accountMock.when(() ->
                            AccountDAO.addBalance(conn, ACCOUNT, AMOUNT))
                    .thenReturn(true);

            // Insert SUCCESS transaction fails
            transactionMock.when(() ->
                            TransactionDAO.insert(
                                    Mockito.eq(conn),
                                    Mockito.argThat(t ->
                                            t.getStatus() == TransactionStatus.SUCCESS)))
                    .thenReturn(false);

            // Insert FAILED transaction succeeds
            transactionMock.when(() ->
                            TransactionDAO.insert(
                                    Mockito.eq(rollBackConn),
                                    Mockito.argThat(t ->
                                            t.getStatus() == TransactionStatus.FAILED)))
                    .thenReturn(true);

            TransactionResult result = TransactionService.deposit(
                    CUSTOMER_ID, ACCOUNT, AMOUNT);

            // Verify transaction is failed
            assertEquals(TransactionStatus.FAILED, result.getStatus());

            // Balance was added, so Service must roll back
            verify(conn).rollback();

            // Main transaction must not be committed
            verify(conn, never()).commit();

            // SUCCESS transaction was attempted
            transactionMock.verify(() ->
                    TransactionDAO.insert(
                            Mockito.eq(conn),
                            Mockito.argThat(t ->
                                    t.getStatus() == TransactionStatus.SUCCESS)));

            // FAILED transaction is stored using a separate connection
            transactionMock.verify(() ->
                    TransactionDAO.insert(
                            Mockito.eq(rollBackConn),
                            Mockito.argThat(t ->
                                    t.getStatus() == TransactionStatus.FAILED)));
        }
    }


    /**
     * Test: Rollback fails after inserting SUCCESS transaction fails,
     * must return UNKNOWN and must not store FAILED transaction.
     */
    @Test
    void testDeposit_RollbackFailed() throws SQLException {

        Connection conn = mock(Connection.class);

        try (
                MockedStatic<Db> dbMock = mockStatic(Db.class);
                MockedStatic<AccountDAO> accountMock = mockStatic(AccountDAO.class);
                MockedStatic<CustomerDAO> customerMock = mockStatic(CustomerDAO.class);
                MockedStatic<TransactionDAO> transactionMock = mockStatic(TransactionDAO.class)) {

            dbMock.when(Db::getConnection).thenReturn(conn);

            customerMock.when(() ->
                            CustomerDAO.existById(conn, CUSTOMER_ID))
                    .thenReturn(true);

            accountMock.when(() ->
                            AccountDAO.existByCustomerAndAccount(
                                    conn, CUSTOMER_ID, ACCOUNT))
                    .thenReturn(true);

            // Add balance succeeds
            accountMock.when(() ->
                            AccountDAO.addBalance(conn, ACCOUNT, AMOUNT))
                    .thenReturn(true);

            // Insert SUCCESS transaction fails
            transactionMock.when(() ->
                            TransactionDAO.insert(
                                    Mockito.eq(conn),
                                    Mockito.argThat(t ->
                                            t.getStatus() == TransactionStatus.SUCCESS)))
                    .thenReturn(false);

            // Rollback fails
            doThrow(new SQLException("Rollback failed"))
                    .when(conn).rollback();

            TransactionResult result = TransactionService.deposit(
                    CUSTOMER_ID, ACCOUNT, AMOUNT);

            // Verify transaction result is UNKNOWN
            assertEquals(TransactionStatus.UNKNOWN, result.getStatus());

            // Rollback was attempted
            verify(conn).rollback();

            // Main transaction must not be committed
            verify(conn, never()).commit();

            // FAILED transaction must not be stored
            transactionMock.verify(() ->
                            TransactionDAO.insert(
                                    Mockito.eq(conn),
                                    Mockito.argThat(t ->
                                            t.getStatus() == TransactionStatus.FAILED)),
                    never());

        }
    }

    /**
     * Test: Commit succeeds after adding balance and inserting SUCCESS transaction,
     * must return SUCCESS.
     */
    @Test
    void testDeposit_CommitSuccess() throws SQLException {

        Connection conn = mock(Connection.class);

        try (
                MockedStatic<Db> dbMock = mockStatic(Db.class);
                MockedStatic<AccountDAO> accountMock = mockStatic(AccountDAO.class);
                MockedStatic<CustomerDAO> customerMock = mockStatic(CustomerDAO.class);
                MockedStatic<TransactionDAO> transactionMock = mockStatic(TransactionDAO.class)) {

            dbMock.when(Db::getConnection).thenReturn(conn);

            customerMock.when(() ->
                            CustomerDAO.existById(conn, CUSTOMER_ID))
                    .thenReturn(true);

            accountMock.when(() ->
                            AccountDAO.existByCustomerAndAccount(
                                    conn, CUSTOMER_ID, ACCOUNT))
                    .thenReturn(true);

            // Add balance succeeds
            accountMock.when(() ->
                            AccountDAO.addBalance(conn, ACCOUNT, AMOUNT))
                    .thenReturn(true);

            // Insert SUCCESS transaction succeeds
            transactionMock.when(() ->
                            TransactionDAO.insert(
                                    Mockito.eq(conn),
                                    Mockito.argThat(t ->
                                            t.getStatus() == TransactionStatus.SUCCESS)))
                    .thenReturn(true);

            TransactionResult result = TransactionService.deposit(
                    CUSTOMER_ID, ACCOUNT, AMOUNT);

            // Verify transaction result is success
            assertEquals(TransactionStatus.SUCCESS, result.getStatus());

            // Commit must be called
            verify(conn).commit();

            // Rollback must not be called
            verify(conn, never()).rollback();

            // Verify balance was added
            accountMock.verify(() ->
                    AccountDAO.addBalance(conn, ACCOUNT, AMOUNT));

            // Verify SUCCESS transaction was inserted
            transactionMock.verify(() ->
                    TransactionDAO.insert(
                            Mockito.eq(conn),
                            Mockito.argThat(t ->
                                    t.getStatus() == TransactionStatus.SUCCESS)));
        }
    }


    /**
     * Test: Commit fails after adding balance and inserting SUCCESS transaction.
     * Must return UNKNOWN because transaction outcome is uncertain.
     */
    @Test
    void testDeposit_CommitFailed() throws SQLException {

        Connection conn = mock(Connection.class);

        try (
                MockedStatic<Db> dbMock = mockStatic(Db.class);
                MockedStatic<AccountDAO> accountMock = mockStatic(AccountDAO.class);
                MockedStatic<CustomerDAO> customerMock = mockStatic(CustomerDAO.class);
                MockedStatic<TransactionDAO> transactionMock = mockStatic(TransactionDAO.class)) {

            dbMock.when(Db::getConnection).thenReturn(conn);

            customerMock.when(() ->
                            CustomerDAO.existById(conn, CUSTOMER_ID))
                    .thenReturn(true);

            accountMock.when(() ->
                            AccountDAO.existByCustomerAndAccount(
                                    conn, CUSTOMER_ID, ACCOUNT))
                    .thenReturn(true);

            // Add balance succeeds
            accountMock.when(() ->
                            AccountDAO.addBalance(conn, ACCOUNT, AMOUNT))
                    .thenReturn(true);

            // Insert SUCCESS transaction succeeds
            transactionMock.when(() ->
                            TransactionDAO.insert(
                                    Mockito.eq(conn),
                                    Mockito.argThat(t ->
                                            t.getStatus() == TransactionStatus.SUCCESS)))
                    .thenReturn(true);

            // Commit fails
            doThrow(new SQLException("Commit failed"))
                    .when(conn).commit();

            TransactionResult result = TransactionService.deposit(
                    CUSTOMER_ID, ACCOUNT, AMOUNT);

            // Verify transaction result is UNKNOWN
            assertEquals(TransactionStatus.UNKNOWN, result.getStatus());

            // Commit was attempted
            verify(conn).commit();

            // Your current Service logic does not rollback after commit failure
            verify(conn, never()).rollback();

            // SUCCESS transaction was inserted before commit was attempted
            transactionMock.verify(() ->
                    TransactionDAO.insert(
                            Mockito.eq(conn),
                            Mockito.argThat(t ->
                                    t.getStatus() == TransactionStatus.SUCCESS)));
        }
    }

}
