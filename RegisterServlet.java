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

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/jsp/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = trim(req.getParameter("username"));
        String email = trim(req.getParameter("email"));
        String password = req.getParameter("password");
        String fullName = trim(req.getParameter("fullName"));
        String next = trim(req.getParameter("next"));

        if (username == null || username.isEmpty() || email == null || email.isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute("error", "Username, email and password are required.");
            req.getRequestDispatcher("/jsp/register.jsp").forward(req, resp);
            return;
        }

        try {
            // check existing username or email
            Optional<User> byUser = UserDao.findByUsername(username);
            if (byUser.isPresent()) {
                req.setAttribute("error", "Account already exists with that username.");
                req.getRequestDispatcher("/jsp/register.jsp").forward(req, resp);
                return;
            }
            Optional<User> byEmail = UserDao.findByEmail(email);
            if (byEmail.isPresent()) {
                req.setAttribute("error", "Account already exists with that email.");
                req.getRequestDispatcher("/jsp/register.jsp").forward(req, resp);
                return;
            }

            // hash password
            String stored = PasswordUtil.hashPassword(password);

            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            u.setPasswordHash(stored);
            u.setFullName(fullName);

            int id = UserDao.create(u);

            // set session
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", id);
            session.setAttribute("username", username);

            // redirect to next or home
            if (next != null && !next.isEmpty()) {
                resp.sendRedirect(resp.encodeRedirectURL(next));
            } else {
                resp.sendRedirect(req.getContextPath() + "/");
            }
        } catch (SQLException sqle) {
            throw new ServletException("Database error during registration", sqle);
        } catch (Exception e) {
            throw new ServletException("Unexpected error during registration", e);
        }
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}

