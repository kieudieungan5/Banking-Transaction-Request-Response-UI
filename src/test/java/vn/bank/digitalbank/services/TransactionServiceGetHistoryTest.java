package vn.bank.digitalbank.services;

import vn.bank.connection.Db;
import vn.bank.digitalbank.commons.TransactionStatus;
import vn.bank.digitalbank.commons.TransactionType;
import vn.bank.digitalbank.daos.AccountDAO;
import vn.bank.digitalbank.daos.TransactionDAO;
import vn.bank.digitalbank.exceptions.BusinessException;
import vn.bank.digitalbank.exceptions.DatabaseConnectionException;
import vn.bank.digitalbank.exceptions.DatabaseOperationException;
import vn.bank.digitalbank.models.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This class test function TransactionService.getTransactionHistory
 *
 */
class TransactionServiceGetHistoryTest {

    /**
     * Test: Successfully retrieve transaction history for an existing account.
     *
     * @throws SQLException if closing the database connection fails
     */
    @Test
    void testGetTransactionHistory_Success() throws SQLException {

        Connection conn = mock(Connection.class);

        // Mock a list of transactions
        List<Transaction> expected = List.of(
                new Transaction(TransactionType.TRANSFER,
                        "000001",
                        "001578",
                        new BigDecimal("2712952.00"),
                        TransactionStatus.SUCCESS
                ),
                new Transaction(TransactionType.TRANSFER,
                        "000001",
                        "001359",
                        new BigDecimal("5225904.0"),
                        TransactionStatus.SUCCESS
                )
        );

        try (MockedStatic<Db> dbMock = mockStatic(Db.class);
             MockedStatic<AccountDAO> accountMock = mockStatic(AccountDAO.class);
             MockedStatic<TransactionDAO> transactionMock = mockStatic(TransactionDAO.class)) {

            // Mock connection: success
            dbMock.when(Db::getConnection).thenReturn(conn);

            // Mock account exists: true
            accountMock.when(() ->
                    AccountDAO.existByAccountNumber(conn, "000001")
            ).thenReturn(true);

            // Mock database returns list of transactions
            transactionMock.when(() ->
                    TransactionDAO.getTransactionsByAccount(conn, "000001")
            ).thenReturn(expected);

            // Call Service
            List<Transaction> actual =
                    TransactionService.getTransactionHistory("000001");

            // Verify list returned
            assertEquals(expected, actual);

            // Verify Connection is closed
            verify(conn).close();
        }
    }

    /**
     * Test: Account doesn't exit,
     * must throws BusinessException
     *
     */
    @Test
    void testGetTransactionHistory_AccountNotFound(){

        Connection conn = mock(Connection.class);

        try (MockedStatic<Db> dbMock = mockStatic(Db.class);
             MockedStatic<AccountDAO> accountMock = mockStatic(AccountDAO.class);
             MockedStatic<TransactionDAO> transactionMock = mockStatic(TransactionDAO.class)) {

            // Mock connection: success
            dbMock.when(Db::getConnection).thenReturn(conn);

            // Mock account exists: false
            accountMock.when(() ->
                    AccountDAO.existByAccountNumber(conn, "000001")
            ).thenReturn(false);

            // Verify functions throws BusinessException with mesage
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> TransactionService.getTransactionHistory("000001")
            );

            assertEquals("Account does not exit",
                    exception.getMessage());

        }
    }

    /**
     * Test: account number is null or blank,
     * must throws BusinessException with message
     *
     */
    @Test
    void testGetTransactionHistory_NullBlankAccountNumber() {

        BusinessException exceptionNull = assertThrows(BusinessException.class,
                () -> TransactionService.getTransactionHistory(null));

        BusinessException exceptionBlank = assertThrows(BusinessException.class,
                () -> TransactionService.getTransactionHistory("   "));

        assertEquals("Account number must be filled",
                exceptionBlank.getMessage());

        assertEquals("Account number must be filled",
                exceptionNull.getMessage());
    }


    /**
     * Test: Connection is broken when preparing sql
     * DAO call getTransactionsByAccount() with that connection and throw SQLException
     * DAO must catch exception & turn to DatabaseOperationException
     *
     */
    @Test
    void testGetTransactionHistory_ConnectionBroken_ThrowsDatabaseOperationException()
            throws SQLException {
        // Create a mock Connection
        Connection conn = mock(Connection.class);
        // When preparing sql, connection is broken
        when(conn.prepareStatement(anyString()))
                .thenThrow(new SQLException("Connection is closed"));
        // Verify if DAO catch SQLException & turn to DatabaseOperationException
        assertThrows(DatabaseOperationException.class,
                () -> TransactionDAO.getTransactionsByAccount(conn, "000001"));
    }

    /**
     * Test: Connection is success & sql is prepared but SQL execution failed.
     * DAO call getTransactionsByAccount() with that connection & sql statement,
     * throws SQLExecution during executeQuery()
     * DAO must catch exception & turn to DatabaseOperationException
     *
     */
    @Test
    void testGetTransactionHistory_ExecutionSQLFailed_ThrowsDatabaseOperationException()
            throws SQLException {
        // Create a mock Connection
        Connection conn = mock(Connection.class);
        // Create mock PreparedStatement
        PreparedStatement stmt = mock(PreparedStatement.class);
        // Simulate sql statement is prepared successfully
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        // Simulate sql execution failure
        when(stmt.executeQuery()).thenThrow(new SQLException("SQL execution failed"));
        // Verify if DAO catch SQLException & turn to DatabaseOperationException
        assertThrows(DatabaseOperationException.class,
                () -> TransactionDAO.getTransactionsByAccount(conn, "000001"));
    }

    /**
     * Test Connection closure: SQL execution is success but Connection closure is failed
     * When closing connection SQLException is thrown,
     * Service must catch & turn to DatabaseConnectionException
     */
    @Test
    void testGetTransactionHistory_ConnectionClosureFailed_ThrowsDatabaseConnectionException()
            throws SQLException {

        // Create a mock Connection
        Connection conn = mock(Connection.class);

        // Create object to control the mocking of static methods in Db, DAO classes
        try (MockedStatic<Db> dbMock = mockStatic(Db.class);
             MockedStatic<AccountDAO> accountDAOMock = mockStatic(AccountDAO.class);
             MockedStatic<TransactionDAO> transactionDAOMock = mockStatic(TransactionDAO.class)) {

            // Call the mocked Db.getConnection() method and return the mocked Connection object
            dbMock.when(Db::getConnection).thenReturn(conn);

            // Call the mocked AccountDAO.existByAccountNumber() method and return the mocked result:
            // account is exited
            accountDAOMock.when(() ->
                    AccountDAO.existByAccountNumber(conn,"000001")
            ).thenReturn(true);

            // Call the mocked TransactionDAO.getTransactionsByAccount() method and return the mocked result:
            // list of account
            List<Transaction> expected = List.of(
                    new Transaction(TransactionType.TRANSFER,
                            "000001",
                            "001578",
                            new BigDecimal("2712952.00"),
                            TransactionStatus.SUCCESS
                    ),
                    new Transaction(TransactionType.TRANSFER,
                            "000001",
                            "001359",
                            new BigDecimal("5225904.0"),
                            TransactionStatus.SUCCESS
                    )
            );

            transactionDAOMock.when(() ->
                    TransactionDAO.getTransactionsByAccount(conn, "000001")
            ).thenReturn(expected);

            // Simulate an SQLException when closing the mocked Connection
            doThrow(new SQLException("Can not close connection"))
                    .when(conn).close();

            // Verify if Service catch SQLException & turn to DatabaseConnectionException
            assertThrows(DatabaseConnectionException.class,
                    () -> TransactionService.getTransactionHistory("000001"));


        }

    }

}
