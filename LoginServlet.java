package newsletter.servlet;

import newsletter.dao.UserDao;
import newsletter.model.User;
import newsletter.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/jsp/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = trim(req.getParameter("username"));
        String password = req.getParameter("password");
        String next = trim(req.getParameter("next"));

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute("error", "Username and password are required.");
            req.getRequestDispatcher("/jsp/login.jsp").forward(req, resp);
            return;
        }

        try {
            Optional<User> opt = UserDao.findByUsername(username);
            if (!opt.isPresent()) {
                req.setAttribute("error", "Account does not exist.");
                req.getRequestDispatcher("/jsp/login.jsp").forward(req, resp);
                return;
            }
            User user = opt.get();
            boolean ok = PasswordUtil.verifyPassword(password, user.getPasswordHash());
            if (!ok) {
                req.setAttribute("error", "Invalid credentials.");
                req.getRequestDispatcher("/jsp/login.jsp").forward(req, resp);
                return;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());

            if (next != null && !next.isEmpty()) {
                resp.sendRedirect(resp.encodeRedirectURL(next));
            } else {
                resp.sendRedirect(req.getContextPath() + "/");
            }
        } catch (SQLException sqle) {
            throw new ServletException("Database error during login", sqle);
        } catch (Exception e) {
            throw new ServletException("Unexpected error during login", e);
        }
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
