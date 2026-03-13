package newsletter.servlet;

import newsletter.dao.NewsDao;
import newsletter.model.NewsItem;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "NewsListServlet", urlPatterns = {"/news"})
public class NewsListServlet extends HttpServlet {

    private static final int DEFAULT_LIMIT = 20;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int limit = DEFAULT_LIMIT;
        String limitParam = req.getParameter("limit");
        if (limitParam != null) {
            try {
                limit = Integer.parseInt(limitParam);
            } catch (NumberFormatException ignored) { }
        }

        try {
            List<NewsItem> list = NewsDao.listLatest(limit);
            req.setAttribute("articles", list);
            req.getRequestDispatcher("/jsp/newsList.jsp").forward(req, resp);
        } catch (SQLException sqle) {
            throw new ServletException("Failed to load news list", sqle);
        }
    }
}

