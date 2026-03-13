package newsletter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import newsletter.api.NewsApiClient;
import newsletter.api.NewsCache;
import newsletter.model.NewsItem;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * JSON endpoint for front-end to fetch cached news.
 * URL: /api/news
 */
@WebServlet(name = "ApiNewsServlet", urlPatterns = {"/api/news"})
public class ApiNewsServlet extends HttpServlet {

    private NewsCache cache;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        try {
            Properties p = new Properties();
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("app.properties")) {
                if (in != null) {
                    p.load(in);
                }
            }
            int ttl = Integer.parseInt(p.getProperty("newsapi.cache.ttl.seconds", "120"));
            NewsApiClient client = new NewsApiClient(); // assumes NewsApiClient reads app.properties
            cache = new NewsCache(ttl, client);
        } catch (Exception e) {
            throw new ServletException("Failed to initialize News API client/cache", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<NewsItem> list = cache.getLatest();
            resp.setContentType("application/json;charset=UTF-8");
            mapper.writeValue(resp.getOutputStream(), list);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json;charset=UTF-8");
            mapper.writeValue(resp.getOutputStream(), java.util.Collections.singletonMap("error", "Failed to fetch news"));
        }
    }
}

