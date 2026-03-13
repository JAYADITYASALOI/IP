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

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("userId") == null) {
            String next = req.getRequestURI();
            resp.sendRedirect(req.getContextPath() + "/login?next=" + next);
            return;
        }
        int userId = (Integer) s.getAttribute("userId");
        try {
            Optional<User> opt = UserDao.findById(userId);
            if (!opt.isPresent()) {
                s.invalidate();
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
            req.setAttribute("user", opt.get());
            req.getRequestDispatcher("/jsp/profile.jsp").forward(req, resp);
        } catch (SQLException sqle) {
            throw new ServletException("Failed to load profile", sqle);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        int userId = (Integer) s.getAttribute("userId");
        String email = trim(req.getParameter("email"));
        String fullName = trim(req.getParameter("fullName"));
        String newPassword = req.getParameter("password");

        try {
            Optional<User> opt = UserDao.findById(userId);
            if (!opt.isPresent()) {
                s.invalidate();
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
            User user = opt.get();
            user.setEmail(email != null ? email : user.getEmail());
            user.setFullName(fullName != null ? fullName : user.getFullName());
            if (newPassword != null && !newPassword.isEmpty()) {
                String hashed = PasswordUtil.hashPassword(newPassword);
                user.setPasswordHash(hashed);
            }
            boolean ok = UserDao.update(user);
            if (!ok) {
                req.setAttribute("error", "Failed to update profile.");
            } else {
                req.setAttribute("message", "Profile updated successfully.");
            }
            req.setAttribute("user", user);
            req.getRequestDispatcher("/jsp/profile.jsp").forward(req, resp);
        } catch (SQLException sqle) {
            throw new ServletException("Failed to update profile", sqle);
        } catch (Exception e) {
            throw new ServletException("Unexpected error while updating profile", e);
        }
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
