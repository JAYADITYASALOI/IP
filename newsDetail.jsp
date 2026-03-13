<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Article - ENews</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>
<div class="container">
    <%@ include file="header.jsp" %>

    <main style="padding:16px 0;">
        <c:choose>
            <c:when test="${empty article}">
                <h2>Article not found</h2>
                <p class="small">The requested article could not be found.</p>
            </c:when>
            <c:otherwise>
                <h2>${fn:escapeXml(article.title)}</h2>
                <div class="meta" style="margin-bottom:12px;">
                    <c:if test="${not empty article.sourceName}">
                        ${fn:escapeXml(article.sourceName)}
                    </c:if>
                    <c:if test="${not empty article.publishedAt}">
                        &nbsp;•&nbsp;<fmt:formatDate value="${article.publishedAt}" pattern="yyyy-MM-dd HH:mm:ss"/>
                    </c:if>
                </div>

                <c:if test="${not empty article.content}">
                    <div style="white-space:pre-wrap;margin-bottom:12px;">${fn:escapeXml(article.content)}</div>
                </c:if>

                <c:if test="${not empty article.sourceUrl}">
                    <div style="margin-top:12px;">
                        <a href="${fn:escapeXml(article.sourceUrl)}" target="_blank" rel="noopener noreferrer">Read original source</a>
                    </div>
                </c:if>

                <div style="margin-top:18px;">
                    <a class="btn" href="${pageContext.request.contextPath}/news">Back to articles</a>
                </div>
            </c:otherwise>
        </c:choose>
    </main>

    <%@ include file="footer.jsp" %>
</div>
</body>
</html>
