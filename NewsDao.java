package newsletter.dao;

import newsletter.model.NewsItem;
import newsletter.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for articles table (articles).
 *
 * Table columns (expected):
 *  article_id INT AUTO_INCREMENT PRIMARY KEY,
 *  headline VARCHAR(512),
 *  teaser VARCHAR(1024),
 *  body TEXT,
 *  provider_name VARCHAR(255),
 *  provider_link VARCHAR(1024),
 *  pub_time DATETIME,
 *  trending_flag TINYINT(1),
 *  created_ts TIMESTAMP
 */
public final class NewsDao {

    private NewsDao() { /* utility */ }

    /**
     * Save the article if it does not already exist (by provider_link).
     * Returns the existing or newly generated article_id.
     */
    public static int saveIfNotExists(NewsItem item) throws SQLException {
        // Prefer matching by provider_link (sourceUrl). If sourceUrl is null, fall back to title+pub_time.
        if (item.getSourceUrl() != null) {
            Optional<Integer> existing = findIdBySourceUrl(item.getSourceUrl());
            if (existing.isPresent()) {
                return existing.get();
            }
        } else {
            Optional<Integer> existing = findIdByTitleAndPubTime(item.getTitle(), item.getPublishedAt());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        // Insert new article
        final String insert = "INSERT INTO articles (headline, teaser, body, provider_name, provider_link, pub_time, trending_flag) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getTitle());
            ps.setString(2, item.getSummary());
            ps.setString(3, item.getContent());
            ps.setString(4, item.getSourceName());
            ps.setString(5, item.getSourceUrl());
            if (item.getPublishedAt() != null) {
                ps.setTimestamp(6, item.getPublishedAt());
            } else {
                ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            }
            ps.setInt(7, item.isTrending() ? 1 : 0);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Inserting article failed, no rows affected.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    item.setId(id);
                    return id;
                } else {
                    throw new SQLException("Inserting article failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Find article id by provider_link.
     */
    private static Optional<Integer> findIdBySourceUrl(String sourceUrl) throws SQLException {
        final String sql = "SELECT article_id FROM articles WHERE provider_link = ? LIMIT 1";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sourceUrl);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("article_id"));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Find article id by title and pub_time (used when provider_link is not available).
     */
    private static Optional<Integer> findIdByTitleAndPubTime(String title, Timestamp pubTime) throws SQLException {
        final String sql = "SELECT article_id FROM articles WHERE headline = ? AND pub_time = ? LIMIT 1";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setTimestamp(2, pubTime != null ? pubTime : new Timestamp(0));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("article_id"));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * List latest articles ordered by pub_time desc.
     */
    public static List<NewsItem> listLatest(int limit) throws SQLException {
        final String sql = "SELECT article_id, headline, teaser, body, provider_name, provider_link, pub_time, trending_flag, created_ts FROM articles ORDER BY pub_time DESC LIMIT ?";
        List<NewsItem> out = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRowToNewsItem(rs));
                }
            }
        }
        return out;
    }

    /**
     * Find article by id.
     */
    public static Optional<NewsItem> findById(int id) throws SQLException {
        final String sql = "SELECT article_id, headline, teaser, body, provider_name, provider_link, pub_time, trending_flag, created_ts FROM articles WHERE article_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToNewsItem(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Mark or unmark an article as trending.
     */
    public static boolean markTrending(int id, boolean trending) throws SQLException {
        final String sql = "UPDATE articles SET trending_flag = ? WHERE article_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, trending ? 1 : 0);
            ps.setInt(2, id);
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    /**
     * Map a ResultSet row to NewsItem model.
     */
    private static NewsItem mapRowToNewsItem(ResultSet rs) throws SQLException {
        NewsItem n = new NewsItem();
        n.setId(rs.getInt("article_id"));
        n.setTitle(rs.getString("headline"));
        n.setSummary(rs.getString("teaser"));
        n.setContent(rs.getString("body"));
        n.setSourceName(rs.getString("provider_name"));
        n.setSourceUrl(rs.getString("provider_link"));
        Timestamp pub = rs.getTimestamp("pub_time");
        n.setPublishedAt(pub);
        n.setTrending(rs.getInt("trending_flag") == 1);
        n.setCreatedAt(rs.getTimestamp("created_ts"));
        return n;
    }
}
