package vn.bank.digitalbank.services;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import vn.bank.connection.Db;
import vn.bank.digitalbank.daos.AccountDAO;
import vn.bank.digitalbank.daos.CustomerDAO;
import vn.bank.digitalbank.exceptions.BusinessException;
import vn.bank.digitalbank.exceptions.DatabaseConnectionException;
import vn.bank.digitalbank.exceptions.DatabaseOperationException;
import vn.bank.digitalbank.models.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * This class tests AccountService class: function returns Account by customer I
 * This cover cases: customer id is blank,
 * customer id does not exit,
 * Connection closure is failed ,
 * return successfully
 *
 */
class AccountServiceTest {

    /**
     * Test: customer ID is null or blank,
     * must throws BusinessException with message
     *
     */
    @Test
    void testGetAccountById_NullBlankCustomerId() {

        BusinessException exceptionNull = assertThrows(BusinessException.class,
                () -> AccountService.getAccountById(null));

        BusinessException exceptionBlank = assertThrows(BusinessException.class,
                () -> AccountService.getAccountById("   "));

        assertEquals("Customer ID must not be blank",
                exceptionBlank.getMessage());

        assertEquals("Customer ID must not be blank",
                exceptionNull.getMessage());
    }

    /**
     * Test: customer doesn't exist,
     * must throws BusinessException with message
     *
     */
    @Test
    void testGetAccountById_NotExistId () {

        Connection conn = mock(Connection.class);

        try (
                MockedStatic<Db> dbMock = mockStatic(Db.class);
                MockedStatic<CustomerDAO> customerMock = mockStatic(CustomerDAO.class)) {

            // Mock Connection: success
            dbMock.when(Db::getConnection).thenReturn(conn);

            // Mock customer exists: false
            customerMock.when(() ->
                    CustomerDAO.existById(conn, "100000000000")
            ).thenReturn(false);

            // Verify if function throws exception
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> AccountService.getAccountById("100000000000"));

            assertEquals("This customer does not exit",
                    exception.getMessage());
        }

    }

    /**
     * Test: connection is broken when preparing sql statement
     * DAO call getAccount() with that connection and throw SQLException
     * DAO must catch exception & turn to DatabaseOperationException
     *
     */
    @Test
    void testGetAccount_ConnectionBroken_ThrowsDatabaseOperationException()
            throws SQLException {
        // Create a mock Connection
        Connection conn = mock(Connection.class);
        // When preparing sql, connection is broken
        when(conn.prepareStatement(anyString()))
                .thenThrow(new SQLException("Connection is closed"));
        // Verify if DAO catch SQLException & turn to DatabaseOperationException
        assertThrows(DatabaseOperationException.class,
                () -> AccountDAO.getAccount(conn, "100000000000"));
    }

    /**
     * Test: Connection is success & sql is prepared but SQL execution failed.
     * DAO call getAccount() with that connection & sql statement, throws SQLExecution during executeQuery()
     * DAO must catch exception & turn to DatabaseOperationException
     *
     */
    @Test
    void testGetAccount_ExecutionSQLFailed_ThrowsDatabaseOperationException()
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
                () -> AccountDAO.getAccount(conn, "100000000000"));
    }

    /**
     * Test Connection closure: SQL execution is success but Connection closure is failed
     * When closing connection SQLException is thrown,
     * Service must catch & turn to DatabaseConnectionException
     */
    @Test
    void testGetAccount_ConnectionClosureFailed_ThrowsDatabaseConnectionException()
            throws SQLException {

        // Create a mock Connection
        Connection conn = mock(Connection.class);

        // Create object to control the mocking of static methods in Db, DAO classes
        try (MockedStatic<Db> dbMock = mockStatic(Db.class);
             MockedStatic<AccountDAO> accountDAOMock = mockStatic(AccountDAO.class);
             MockedStatic<CustomerDAO> customerDAOMock = mockStatic(CustomerDAO.class)) {

            // Call the mocked Db.getConnection() method and return the mocked Connection object
            dbMock.when(Db::getConnection).thenReturn(conn);

            // Call the mocked AccountDAO.existByAccountNumber() method and return the mocked result:
            // account is exited
            customerDAOMock.when(() ->
                    CustomerDAO.existById(conn, "100000000000")
            ).thenReturn(true);

            // Call the mocked AccountDAO.getAccount() method and return the mocked result:
            // list of account
            List<Account> accs = new ArrayList<>();

            accs.add(new Account("990000000001",
                    "000001",
                    new BigDecimal("4356151.10")));

            accs.add(new Account("990000000001",
                    "000002",
                    new BigDecimal("13245690.61")));

            accountDAOMock.when(() ->
                    AccountDAO.getAccount(conn, "100000000000")
            ).thenReturn(accs);

            // Simulate an SQLException when closing the mocked Connection
            doThrow(new SQLException("Can not close connection"))
                    .when(conn).close();

            // Verify if Service catch SQLException & turn to DatabaseConnectionException
            assertThrows(DatabaseConnectionException.class,
                    () -> AccountService.getAccountById("100000000000"));


        }

    }

    /**
     * Test: function returns accounts success
     */
    @Test
    void testGetAccount_Success_ReturnsAccounts() throws SQLException {

        // Create a mock Connection
        Connection conn = mock(Connection.class);

        // Create object to control the mocking of static methods in Db, DAO classes
        try (MockedStatic<Db> dbMock = mockStatic(Db.class);
             MockedStatic<AccountDAO> accountDAOMock = mockStatic(AccountDAO.class);
             MockedStatic<CustomerDAO> customerDAOMock = mockStatic(CustomerDAO.class)) {

            // Call the mocked Db.getConnection() method and return the mocked Connection object
            dbMock.when(Db::getConnection).thenReturn(conn);

            // Call the mocked CustomerDAO.existById method and return the mocked result:
            // account is exited
            customerDAOMock.when(() ->
                    CustomerDAO.existById(conn, "100000000000")
            ).thenReturn(true);

            // Call the mocked AccountDAO.getAccount() method and return the mocked result:
            // list of account
            List<Account> expected = new ArrayList<>();

            expected.add(new Account("990000000001",
                    "000001",
                    new BigDecimal("4356151.10")));

            expected.add(new Account("990000000001",
                    "000002",
                    new BigDecimal("13245690.61")));

            accountDAOMock.when(() ->
                    AccountDAO.getAccount(conn, "100000000000")
            ).thenReturn(expected);

            // Call AccountService.getAccountById method with mocked results
            List<Account> actual = AccountService.getAccountById("100000000000");

            // Verify
            assertEquals(expected, actual);

            // Verify DAO methods were called
            customerDAOMock.verify(() ->
                    CustomerDAO.existById(conn, "100000000000"));

            accountDAOMock.verify(() ->
                    AccountDAO.getAccount(conn, "100000000000"));

            // Verify connection was closed
            verify(conn).close();

        }

    }

}