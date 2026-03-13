<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Profile - ENews</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>
<div class="container">
    <%@ include file="header.jsp" %>

    <main style="padding:16px 0;">
        <h2>Your Profile</h2>

        <c:if test="${not empty error}">
            <div class="error" style="margin-bottom:12px;">${fn:escapeXml(error)}</div>
        </c:if>
        <c:if test="${not empty message}">
            <div style="margin-bottom:12px;color:green;">${fn:escapeXml(message)}</div>
        </c:if>

        <c:choose>
            <c:when test="${empty user}">
                <p class="small">Profile information is not available. Please <a href="${pageContext.request.contextPath}/login">login</a> again.</p>
            </c:when>
            <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/profile" style="max-width:520px;">
                    <div style="margin-bottom:8px;">
                        <label>Username</label><br/>
                        <input type="text" value="${fn:escapeXml(user.username)}" disabled style="width:100%;padding:8px;background:#f3f3f3;"/>
                    </div>
                    <div style="margin-bottom:8px;">
                        <label for="email">Email</label><br/>
                        <input id="email" name="email" type="email" value="${fn:escapeXml(user.email)}" required style="width:100%;padding:8px;"/>
                    </div>
                    <div style="margin-bottom:8px;">
                        <label for="fullName">Full name</label><br/>
                        <input id="fullName" name="fullName" type="text" value="${fn:escapeXml(user.fullName)}" style="width:100%;padding:8px;"/>
                    </div>
                    <div style="margin-bottom:8px;">
                        <label for="password">New password (leave blank to keep current)</label><br/>
                        <input id="password" name="password" type="password" style="width:100%;padding:8px;"/>
                    </div>
                    <div>
                        <button type="submit" class="btn">Update profile</button>
                    </div>
                </form>

                <hr style="margin:18px 0;"/>

                <form method="post" action="${pageContext.request.contextPath}/delete-account" onsubmit="return confirm('Are you sure you want to delete your account? This action cannot be undone.');">
                    <button type="submit" style="background:#b00020;color:#fff;padding:8px 12px;border:none;border-radius:4px;cursor:pointer;">Delete account</button>
                </form>
            </c:otherwise>
        </c:choose>
    </main>

    <%@ include file="footer.jsp" %>
</div>
</body>
</html>
