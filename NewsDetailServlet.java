package newsletter.servlet;

import newsletter.dao.NewsDao;
import newsletter.model.NewsItem;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet(name = "NewsDetailServlet", urlPatterns = {"/news/detail"})
public class NewsDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendRedirect(req.getContextPath() + "/news");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException nfe) {
            resp.sendRedirect(req.getContextPath() + "/news");
            return;
        }

        try {
            Optional<NewsItem> opt = NewsDao.findById(id);
            if (!opt.isPresent()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Article not found");
                return;
            }
            req.setAttribute("article", opt.get());
            req.getRequestDispatcher("/jsp/newsDetail.jsp").forward(req, resp);
        } catch (SQLException sqle) {
            throw new ServletException("Failed to load article", sqle);
        }
    }
}

