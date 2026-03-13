<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Login - ENews</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>
<div class="container">
    <%@ include file="header.jsp" %>

    <main style="padding:16px 0;">
        <h2>Login</h2>

        <c:if test="${not empty error}">
            <div class="error" style="margin-bottom:12px;">${fn:escapeXml(error)}</div>
        </c:if>
        <c:if test="${not empty message}">
            <div style="margin-bottom:12px;color:green;">${fn:escapeXml(message)}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/login" style="max-width:420px;">
            <input type="hidden" name="next" value="${fn:escapeXml(param.next)}"/>
            <div style="margin-bottom:8px;">
                <label for="username">Username</label><br/>
                <input id="username" name="username" type="text" required style="width:100%;padding:8px;"/>
            </div>
            <div style="margin-bottom:8px;">
                <label for="password">Password</label><br/>
                <input id="password" name="password" type="password" required style="width:100%;padding:8px;"/>
            </div>
            <div>
                <button type="submit" class="btn">Login</button>
                <a href="${pageContext.request.contextPath}/register" style="margin-left:12px;">Create account</a>
            </div>
        </form>
    </main>

    <%@ include file="footer.jsp" %>
</div>
</body>
</html>
