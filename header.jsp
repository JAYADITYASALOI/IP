<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<header>
    <div style="display:flex;justify-content:space-between;align-items:center;padding:12px 0;border-bottom:1px solid #eee;">
        <div>
            <a href="${pageContext.request.contextPath}/" style="text-decoration:none;color:inherit;">
                <h1 style="margin:0;font-size:1.4rem;">ENews</h1>
            </a>
        </div>
        <nav>
            <c:choose>
                <c:when test="${not empty sessionScope.username}">
                    <span style="margin-right:12px;font-size:0.95rem;color:#333;">Welcome, <strong>${fn:escapeXml(sessionScope.username)}</strong></span>
                    <a href="${pageContext.request.contextPath}/profile" style="margin-right:10px;color:#0366d6;text-decoration:none;">Profile</a>
                    <a href="${pageContext.request.contextPath}/logout" style="color:#0366d6;text-decoration:none;">Logout</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login" style="margin-right:10px;color:#0366d6;text-decoration:none;">Login</a>
                    <a href="${pageContext.request.contextPath}/register" style="color:#0366d6;text-decoration:none;">Register</a>
                </c:otherwise>
            </c:choose>
        </nav>
    </div>
</header>
