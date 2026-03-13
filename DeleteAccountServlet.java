package newsletter.servlet;

import newsletter.dao.UserDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "DeleteAccountServlet", urlPatterns = {"/delete-account"})
public class DeleteAccountServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        int userId = (Integer) s.getAttribute("userId");

        try {
            boolean deleted = UserDao.delete(userId);
            s.invalidate();
            if (deleted) {
                resp.sendRedirect(req.getContextPath() + "/?deleted=true");
            } else {
                // deletion failed for some reason
                resp.sendRedirect(req.getContextPath() + "/profile?error=delete_failed");
            }
        } catch (SQLException sqle) {
            throw new ServletException("Failed to delete account", sqle);
        }
    }
}
