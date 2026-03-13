package newsletter.dao;

import newsletter.model.User;
import newsletter.util.DBUtil;

import java.sql.*;
import java.util.Optional;

/**
 * DAO for accounts table (accounts).
 *
 * Table columns (expected):
 *  account_id INT AUTO_INCREMENT PRIMARY KEY,
 *  user_name VARCHAR(80) UNIQUE,
 *  email_addr VARCHAR(150) UNIQUE,
 *  pwd_hash VARCHAR(512),
 *  display_name VARCHAR(150),
 *  created_ts TIMESTAMP
 */
public final class UserDao {

    private UserDao() { /* utility */ }

    /**
     * Insert a new account. Returns generated account_id.
     * Caller should ensure password is already hashed.
     */
    public static int create(User user) throws SQLException {
        final String sql = "INSERT INTO accounts (user_name, email_addr, pwd_hash, display_name) VALUES (?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFullName());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    user.setId(id);
                    return id;
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Find account by username.
     */
    public static Optional<User> findByUsername(String username) throws SQLException {
        final String sql = "SELECT account_id, user_name, email_addr, pwd_hash, display_name, created_ts FROM accounts WHERE user_name = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Find account by email.
     */
    public static Optional<User> findByEmail(String email) throws SQLException {
        final String sql = "SELECT account_id, user_name, email_addr, pwd_hash, display_name, created_ts FROM accounts WHERE email_addr = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Find account by id.
     */
    public static Optional<User> findById(int id) throws SQLException {
        final String sql = "SELECT account_id, user_name, email_addr, pwd_hash, display_name, created_ts FROM accounts WHERE account_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Update account fields (email, display name, passwordHash).
     * Returns true if a row was updated.
     */
    public static boolean update(User user) throws SQLException {
        final String sql = "UPDATE accounts SET email_addr = ?, pwd_hash = ?, display_name = ? WHERE account_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setInt(4, user.getId());
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    /**
     * Delete account by id. Returns true if deleted.
     */
    public static boolean delete(int id) throws SQLException {
        final String sql = "DELETE FROM accounts WHERE account_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            int deleted = ps.executeUpdate();
            return deleted > 0;
        }
    }

    /**
     * Map a ResultSet row to User model.
     */
    private static User mapRowToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("account_id"));
        u.setUsername(rs.getString("user_name"));
        u.setEmail(rs.getString("email_addr"));
        u.setPasswordHash(rs.getString("pwd_hash"));
        u.setFullName(rs.getString("display_name"));
        Timestamp ts = rs.getTimestamp("created_ts");
        u.setCreatedAt(ts);
        return u;
    }
}
