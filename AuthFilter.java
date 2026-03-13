package newsletter.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * AuthFilter
 *
 * Protects application URLs that require an authenticated user.
 *
 * Behavior:
 * - If the request targets a protected path and the session does not contain "userId",
 *   the filter redirects to /login?next=<originalRequestUri>.
 * - Static resources and public endpoints (login, register, api/news, css, js, images, etc.)
 *   are allowed through without authentication.
 *
 * Configure protected paths by editing the PROTECTED_PATHS set below.
 *
 * This filter is mapped to all requests (/*) and performs lightweight checks.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    /**
     * Paths that require authentication. These are context-relative paths (start with '/').
     * If a request URI starts with one of these paths (after the context path), authentication is required.
     *
     * Edit this set to add or remove protected endpoints.
     */
    private static final Set<String> PROTECTED_PATHS = new HashSet<>();

    static {
        PROTECTED_PATHS.add("/news/detail");
        PROTECTED_PATHS.add("/profile");
        PROTECTED_PATHS.add("/delete-account");
        // add other protected endpoints if needed, e.g.:
        // PROTECTED_PATHS.add("/news/create");
        // PROTECTED_PATHS.add("/admin");
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void destroy() {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        if (!(servletRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String contextPath = req.getContextPath(); // may be "" or "/app"
        String requestUri = req.getRequestURI();   // full path including context
        String pathAfterContext = requestUri.substring(contextPath.length()); // starts with '/'

        // Allow public resources and endpoints without authentication
        if (isPublicPath(pathAfterContext)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        // If the path is protected, ensure user is authenticated
        if (isProtectedPath(pathAfterContext)) {
            HttpSession session = req.getSession(false);
            boolean authenticated = session != null && session.getAttribute("userId") != null;
            if (!authenticated) {
                // Redirect to login with next parameter set to original request URI (including query)
                String original = requestUri;
                String query = req.getQueryString();
                if (query != null && !query.isEmpty()) {
                    original = original + "?" + query;
                }
                String encoded = URLEncoder.encode(original, StandardCharsets.UTF_8.name());
                String loginUrl = contextPath + "/login?next=" + encoded;
                resp.sendRedirect(resp.encodeRedirectURL(loginUrl));
                return;
            }
        }

        // Default: allow
        chain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Determine whether the given path should be treated as public (no auth required).
     * This includes login/register endpoints, API endpoints that are public, and static assets.
     *
     * @param path path after context (starts with '/')
     * @return true if public
     */
    private boolean isPublicPath(String path) {
        if (path == null || path.isEmpty()) return true;

        // Public endpoints
        if (path.equals("/") ||
                path.equals("/index.jsp") ||
                path.equals("/login") ||
                path.equals("/register") ||
                path.equals("/logout") ||
                path.startsWith("/api/") ||    // allow public API endpoints (e.g., /api/news)
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/webjars/") ||
                path.startsWith("/static/") ||
                path.startsWith("/favicon.ico") ||
                path.startsWith("/error")) {
            return true;
        }
        return false;
    }

    /**
     * Check whether the path is one of the protected paths.
     *
     * @param path path after context (starts with '/')
     * @return true if protected
     */
    private boolean isProtectedPath(String path) {
        if (path == null || path.isEmpty()) return false;
        for (String p : PROTECTED_PATHS) {
            if (path.equals(p) || path.startsWith(p + "/") || path.startsWith(p + "?")) {
                return true;
            }
        }
        return false;
    }
}

