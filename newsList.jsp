<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Articles - ENews</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>
<div class="container">
    <%@ include file="header.jsp" %>

    <main style="padding:16px 0;">
        <h2>Site Articles</h2>

        <c:if test="${empty articles}">
            <p class="small">No articles found.</p>
        </c:if>

        <c:if test="${not empty articles}">
            <ul class="headline-list" style="padding:0;margin:0;">
                <c:forEach var="article" items="${articles}">
                    <li class="headline-item" style="list-style:none;">
                        <a class="title" href="${pageContext.request.contextPath}/news/detail?id=${article.id}">
                            ${fn:escapeXml(article.title)}
                        </a>
                        <div class="meta">
                            <c:choose>
                                <c:when test="${not empty article.sourceName}">
                                    ${fn:escapeXml(article.sourceName)}
                                </c:when>
                                <c:otherwise>
                                    Internal
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${not empty article.publishedAt}">
                                &nbsp;•&nbsp;<fmt:formatDate value="${article.publishedAt}" pattern="yyyy-MM-dd HH:mm:ss"/>
                            </c:if>
                        </div>
                        <c:if test="${not empty article.summary}">
                            <div class="small" style="margin-top:6px;">${fn:escapeXml(article.summary)}</div>
                        </c:if>
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </main>

    <%@ include file="footer.jsp" %>
</div>
</body>
</html>
