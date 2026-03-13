package newsletter.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple JDBC connection utility.
 * Edit JDBC_URL, DB_USER, DB_PASSWORD to match your environment.
 *
public class DBConnection {
    // <-- Edit these to your MySQL credentials if needed -->
    // replace the JDBC_URL line with this:
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/enewsletter_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root"; // put your DB password here

    static {
        try {
            // Register driver explicitly (optional for modern drivers, but safe)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Make sure connector is on classpath.");
            e.printStackTrace();
        }
    }

    private DBConnection() { /* util class */
/**
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
    }
}
*/


import java.io.InputStream;
import java.util.Properties;

/**
 * Simple DB utility that loads JDBC configuration from classpath resource
 * src/main/resources/db.properties and provides a Connection via DriverManager.
 *
 * Expected db.properties keys:
 *   jdbc.url
 *   jdbc.user
 *   jdbc.password
 *
 * Note: This implementation uses DriverManager. For production, replace with
 * a connection pool (HikariCP) and do not store credentials in source control.
 */
public final class DBUtil {
    private static final String PROPS_RESOURCE = "db.properties";
    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream(PROPS_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Database properties file not found on classpath: " + PROPS_RESOURCE);
            }
            Properties p = new Properties();
            p.load(in);
            url = p.getProperty("jdbc.url");
            user = p.getProperty("jdbc.user");
            password = p.getProperty("jdbc.password");

            if (url == null || url.isBlank()) {
                throw new IllegalStateException("jdbc.url is not set in " + PROPS_RESOURCE);
            }

            // Load MySQL driver class explicitly to ensure compatibility on some environments.
            // If you use a different DB, change the driver class accordingly.
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                // Driver not found on classpath; DriverManager may still work if JDBC 4 driver is present.
                // Re-throw as runtime to fail fast.
                throw new IllegalStateException("MySQL JDBC driver not found on classpath.", e);
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to initialize DBUtil: " + e.getMessage());
        }
    }

    private DBUtil() { /* utility */ }

    /**
     * Obtain a new JDBC Connection. Caller is responsible for closing the connection.
     *
     * @return new Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (user == null || user.isBlank()) {
            // If user is not provided, call DriverManager.getConnection(url)
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, user, password);
    }
}

