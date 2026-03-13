package newsletter.model;

import java.sql.Timestamp;
import java.util.Objects;

public class NewsItem {
    private int id;
    private String title;
    private String summary;
    private String content;
    private String sourceName;
    private String sourceUrl;
    private Timestamp publishedAt;
    private boolean trending;
    private Timestamp createdAt;

    public NewsItem() { }

    public NewsItem(String title, String summary, String content, String sourceName, String sourceUrl, Timestamp publishedAt, boolean trending) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.publishedAt = publishedAt;
        this.trending = trending;
    }

    public NewsItem(int id, String title, String summary, String content, String sourceName, String sourceUrl, Timestamp publishedAt, boolean trending, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.publishedAt = publishedAt;
        this.trending = trending;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Timestamp getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Timestamp publishedAt) {
        this.publishedAt = publishedAt;
    }

    public boolean isTrending() {
        return trending;
    }

    public void setTrending(boolean trending) {
        this.trending = trending;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewsItem newsItem = (NewsItem) o;
        return id == newsItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "NewsItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", publishedAt=" + publishedAt +
                ", trending=" + trending +
                '}';
    }
}

