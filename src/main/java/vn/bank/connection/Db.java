package vn.bank.connection;

import vn.bank.digitalbank.exceptions.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {

    // Address of db: "jdbc:mysql://HOST:PORT/DATABASE_NAME"
    private static final String URL =
            System.getenv("DB_URL");

    // Account to access to db
    private static final String USER =
            System.getenv("DB_USERNAME");

    // Password to access to db
    // This pass is not official!
    private static final String PASS =
            System.getenv("DB_PASSWORD");

    // MySQl verifies account & access right of account to db,
    // grants connection
    public static Connection getConnection() {
        // Check environment variables
        if (URL == null || USER == null || PASS == null) {
            throw new IllegalStateException(
                    "Database environment variables are not configured"
            );
        }

        try {
            return DriverManager.getConnection(URL, USER, PASS);

            // When database access error occurs / url is null
        } catch (SQLException exc) {
            throw new DatabaseConnectionException("Can not connect to database");
        }
    }

}
