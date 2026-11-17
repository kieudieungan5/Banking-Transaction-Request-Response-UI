package vn.bank.digitalbank.daos;

import vn.bank.digitalbank.commons.TransactionStatus;
import vn.bank.digitalbank.commons.TransactionType;
import vn.bank.digitalbank.exceptions.DatabaseOperationException;
import vn.bank.digitalbank.models.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    /**
     * Return list transactions of an account
     * @param conn Connection
     * @param accountNumber number of account
     * @return
     */
    public static List<Transaction> getTransactionsByAccount(
            Connection conn,
            String accountNumber){

        String sql = """
                SELECT * FROM TRANS
                WHERE acc_source = ? 
                ORDER BY time DESC
                """;

        List<Transaction> trans = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Transaction tran = new Transaction(
                        rs.getString("id_trans"),
                        TransactionType.valueOf(rs.getString("type_trans")),
                        rs.getString("acc_source"),
                        rs.getString("acc_receive"),
                        rs.getBigDecimal("amount"),
                        TransactionStatus.valueOf(rs.getString("status")),
                        rs.getString("time")
                );

                trans.add(tran);

            }
        } catch (SQLException exc) {
            throw new DatabaseOperationException("Can not execute SQL statement");
        }

        return trans;
    }

    /**
     * Add transaction to database
     *
     * @param conn Connection
     * @param trans Transaction
     * @return true/ false result
     * @throws SQLException if a database access error occurs
     * or this method is called on a closed connection
     */
    public static boolean insert(Connection conn,
                              Transaction trans) throws SQLException {
        String sql = """
                INSERT INTO TRANS (
                id_trans,
                acc_source,
                acc_receive,
                type_trans,
                amount,
                status) 
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1,trans.getId());
            pstmt.setString(2,trans.getAccountSource());
            pstmt.setString(3,trans.getAccountDestination());
            pstmt.setString(4,trans.getType().name());
            pstmt.setBigDecimal(5,trans.getAmount());
            pstmt.setString(6,trans.getStatus().name());

            return pstmt.executeUpdate() == 1;

        }
    }

}
