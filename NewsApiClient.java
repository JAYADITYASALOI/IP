package newsletter.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import newsletter.model.NewsItem;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Simple NewsAPI client that fetches top headlines and converts them to NewsItem objects.
 *
 * Configuration is read from src/main/resources/app.properties with keys:
 *   newsapi.key
 *   newsapi.topheadlines.url
 *   newsapi.country
 *   newsapi.pageSize
 *
 * This class uses java.net.http.HttpClient and Jackson (com.fasterxml.jackson.databind.ObjectMapper).
 */
public final class NewsApiClient {
    private final String apiKey;
    private final String topHeadlinesUrl;
    private final String country;
    private final int pageSize;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public NewsApiClient() throws Exception {
        Properties p = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("app.properties")) {
            if (in == null) {
                throw new IllegalStateException("app.properties not found on classpath");
            }
            p.load(in);
        }
        this.apiKey = p.getProperty("newsapi.key", "").trim();
        this.topHeadlinesUrl = p.getProperty("newsapi.topheadlines.url", "https://newsapi.org/v2/top-headlines").trim();
        this.country = p.getProperty("newsapi.country", "in").trim();
        this.pageSize = Integer.parseInt(p.getProperty("newsapi.pageSize", "20").trim());
        if (this.apiKey.isEmpty()) {
            throw new IllegalStateException("newsapi.key is not set in app.properties");
        }
        this.http = HttpClient.newHttpClient();
    }

    /**
     * Fetch top headlines from the configured NewsAPI endpoint and convert to a list of NewsItem.
     *
     * @return list of NewsItem (may be empty)
     * @throws Exception on network, parsing, or API errors
     */
    public List<NewsItem> fetchTopHeadlines() throws Exception {
        String uri = String.format("%s?country=%s&pageSize=%d&apiKey=%s",
                topHeadlinesUrl, country, pageSize, apiKey);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        int status = resp.statusCode();
        if (status != 200) {
            // include body for debugging; caller should handle/log appropriately
            throw new RuntimeException("News API returned status " + status + ": " + resp.body());
        }

        String body = resp.body();
        JsonNode root = mapper.readTree(body);
        JsonNode articles = root.path("articles");
        List<NewsItem> list = new ArrayList<>();
        if (articles.isArray()) {
            for (JsonNode a : articles) {
                NewsItem n = new NewsItem();
                // map common fields safely
                n.setTitle(nullIfEmpty(a.path("title").asText(null)));
                n.setSummary(nullIfEmpty(a.path("description").asText(null)));
                // content may contain trailing "[+...]" from some providers; keep as-is for now
                n.setContent(nullIfEmpty(a.path("content").asText(null)));

                // source object may contain name and url
                JsonNode source = a.path("source");
                if (!source.isMissingNode()) {
                    n.setSourceName(nullIfEmpty(source.path("name").asText(null)));
                } else {
                    n.setSourceName(null);
                }

                // url to original article
                n.setSourceUrl(nullIfEmpty(a.path("url").asText(null)));

                // publishedAt is ISO-8601; parse robustly
                String publishedAtStr = a.path("publishedAt").asText(null);
                Timestamp publishedTs = parseTimestampOrNow(publishedAtStr);
                n.setPublishedAt(publishedTs);

                // trending flag not provided by API; leave false (caller may set based on logic)
                n.setTrending(false);

                list.add(n);
            }
        }
        return list;
    }

    private static String nullIfEmpty(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Timestamp parseTimestampOrNow(String iso) {
        if (iso == null || iso.isBlank()) {
            return Timestamp.from(Instant.now());
        }
        try {
            Instant inst = Instant.parse(iso);
            return Timestamp.from(inst);
        } catch (Exception e) {
            // fallback: try to parse as Instant via other means or return now
            try {
                // attempt to parse without timezone if possible
                Instant inst = Instant.parse(iso + "Z");
                return Timestamp.from(inst);
            } catch (Exception ex) {
                return Timestamp.from(Instant.now());
            }
        }
    }
}

