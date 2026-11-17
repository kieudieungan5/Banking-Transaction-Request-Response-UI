package vn.bank.digitalbank.services;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import vn.bank.connection.Db;
import vn.bank.digitalbank.daos.AccountDAO;
import vn.bank.digitalbank.daos.CustomerDAO;
import vn.bank.digitalbank.exceptions.BusinessException;
import vn.bank.digitalbank.exceptions.DatabaseConnectionException;
import vn.bank.digitalbank.exceptions.DatabaseOperationException;
import vn.bank.digitalbank.models.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * This class tests CustomerService class:
 * This cover cases: account number is blank or null,
 * account number doesn't exit,
 * Connection closure is failed ,
 * return Customer successfully
 *
 */
class CustomerServiceTest {

    /**
     * Test: account number is null or blank,
     * must throws BusinessException with message
     *
     */
    @Test
    void testGetCustomer_NullBlankAccountNumber() {

        BusinessException exceptionNull = assertThrows(BusinessException.class,
                () -> CustomerService.getCustomerByAccount(null));

        BusinessException exceptionBlank = assertThrows(BusinessException.class,
                () -> CustomerService.getCustomerByAccount("   "));

        assertEquals("Account number must not be blank",
                exceptionBlank.getMessage());

        assertEquals("Account number must not be blank",
                exceptionNull.getMessage());
    }

    /**
     * Test: customer doesn't exist,
     * must throws BusinessException with message
     *
     */
    @Test
    void testGetCustomer_NotExistAccount () {

        Connection conn = mock(Connection.class);

        try (
                MockedStatic<Db> dbMock = mockStatic(Db.class);
                MockedStatic<AccountDAO> accountMock = mockStatic(AccountDAO.class)) {

            // Mock Connection: success
            dbMock.when(Db::getConnection).thenReturn(conn);

            // Mock account exists: false
            accountMock.when(() ->
                    AccountDAO.existByAccountNumber(conn, "000001")
            ).thenReturn(false);

            // Verify if function throws exception
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> CustomerService.getCustomerByAccount("000001"));

            assertEquals("This account does not exit",
                    exception.getMessage());
        }

    }

    /**
     * Test: connection is broken when preparing sql
     * DAO call getCustomerByAccount() with that connection and throw SQLException
     * DAO must catch exception & turn to DatabaseOperationException
     *
     */
    @Test
    void testGetCustomer_ConnectionBroken_ThrowsDatabaseOperationException()
            throws SQLException {
        // Create a mock Connection
        Connection conn = mock(Connection.class);
        // When preparing sql, connection is broken
        when(conn.prepareStatement(anyString()))
                .thenThrow(new SQLException("Connection is closed"));
        // Verify if DAO catch SQLException & turn to DatabaseOperationException
        assertThrows(DatabaseOperationException.class,
                () -> CustomerDAO.getCustomerByAccount(conn, "000001"));
    }

    /**
     * Test: Connection is success & sql is prepared but SQL execution failed.
     * DAO call getCustomerByAccount() with that connection & sql statement, throws SQLExecution during executeQuery()
     * DAO must catch exception & turn to DatabaseOperationException
     *
     */
    @Test
    void testGetCustomer_ExecutionSQLFailed_ThrowsDatabaseOperationException()
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
                () -> CustomerDAO.getCustomerByAccount(conn, "000001"));
    }

    /**
     * Test Connection closure: SQL execution is success but Connection closure is failed
     * When closing connection SQLException is thrown,
     * Service must catch & turn to DatabaseConnectionException
     */
    @Test
    void testGetCustomer_ConnectionClosureFailed_ThrowsDatabaseConnectionException()
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
            accountDAOMock.when(() ->
                    AccountDAO.existByAccountNumber(conn, "000001")
            ).thenReturn(true);

            // Call the mocked CustomerDAO.getCustomerByAccount() method and return the mocked result:
            // Customer
            customerDAOMock.when(() ->
                    CustomerDAO.getCustomerByAccount(conn, "000001")
            ).thenReturn(new Customer("990000000001"));

            // Simulate an SQLException when closing the mocked Connection
            doThrow(new SQLException("Can not close connection"))
                    .when(conn).close();

            // Verify if Service catch SQLException & turn to DatabaseConnectionException
            assertThrows(DatabaseConnectionException.class,
                    () -> CustomerService.getCustomerByAccount("000001"));


        }

    }

    /**
     * Test: return customer success
     */
    @Test
    void testGetCustomer_Success_ReturnsCustomer() throws SQLException{

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
            accountDAOMock.when(() ->
                    AccountDAO.existByAccountNumber(conn, "000001")
            ).thenReturn(true);

            Customer expected = new Customer("100000000000");

            customerDAOMock.when(() ->
                    CustomerDAO.getCustomerByAccount(conn, "000001")).
                    thenReturn(expected);

            // Call CustomerService.getCustomerByAccount method with mocked results
            Customer actual = CustomerService.getCustomerByAccount("000001");

            // Verify
            assertEquals(expected, actual);

            // Verify DAO methods were called
            accountDAOMock.verify(() ->
                    AccountDAO.existByAccountNumber(conn, "000001"));

            customerDAOMock.verify(() ->
                    CustomerDAO.getCustomerByAccount(conn, "000001"));

            // Verify connection was closed
            verify(conn).close();

        }

    }

}