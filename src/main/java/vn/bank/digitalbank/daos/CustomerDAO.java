package vn.bank.digitalbank.daos;

import vn.bank.connection.Db;
import vn.bank.digitalbank.exceptions.DatabaseOperationException;
import vn.bank.digitalbank.models.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    /**
     * Return customer using input account number
     *
     * @param conn Connection
     * @param accountNumber number of account
     * @return Customer
     */
    public static Customer getCustomerByAccount(Connection conn,
                                           String accountNumber) {
        String sql = """
                SELECT cus_id
                FROM ACC
                WHERE number_account = ?
                """;

        Customer cus = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getString("cus_id")
                    );
                }
            }

            return null;

        } catch (SQLException exc) {
            throw new DatabaseOperationException("Can not execute SQL operation");
        }
    }

    /**
     * Add customer to database
     *
     * @param customerId ID of customer
     * @param name name of customer
     * @return true/ false result
     */
    public static boolean insert(String customerId, String name) {
        // SQL statement to add customer
        String sql = """
                INSERT INTO CUS (id, name) 
                VALUES (?, ?)
                """;

        // Connect Java & MySQL
        try(
                Connection conn = Db.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            // Set customer id
            pstmt.setString(1, customerId);

            // Set customer name
            pstmt.setString(2, name);

            // Insert to table Customer
            return pstmt.executeUpdate() == 1;
        } catch (SQLException exc) {
            System.out.println("Can not add new customer" + exc.getMessage());
            return false;
        }
    }

    /**
     * Check if customer is existed on database
     *
     * @param customerId ID of customer
     * @return true/ false result
     */
    public static boolean existById(Connection conn, String customerId) {
        String sql = """
                SELECT 1 FROM CUS 
                WHERE id = ?
                """;

        try(
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, customerId);

            ResultSet rs = pstmt.executeQuery();

            if(!rs.next()) {
                return false;
            }
        } catch (SQLException exc) {
            throw new RuntimeException("Can not access to database", exc);
        }

        return true;
    }

    /**
     * Return all customers
     *
     * @return list of all customers
     */
    public static List<Customer> showAll() {

        List<Customer> customers = new ArrayList<>();

        String sql = """
                SELECT *
                FROM CUS
                """;

        try(
                Connection conn = Db.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("There is no customer");
                return null;
            }

            while(rs.next()) {
                String customerId = rs.getString(1);
                String name = rs.getString(2);

                customers.add(new Customer(customerId, name));
            }

            System.out.printf("%20s %25s%n",
                    "Customer Id",
                    "Customer Name");
            System.out.println("---------------------------------------");

        } catch (SQLException exc) {
            throw new RuntimeException("Can not access to database", exc);
        }

        return customers;
    }

}
