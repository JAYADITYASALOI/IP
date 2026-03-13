<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>ENews - Home</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
    <style>
        /* Minimal inline styles to ensure readable layout if css/style.css is missing */
        body { font-family: Arial, Helvetica, sans-serif; margin: 0; padding: 0; background:#f7f7f7; color:#222; }
        .container { max-width: 980px; margin: 24px auto; padding: 16px; background:#fff; box-shadow:0 1px 4px rgba(0,0,0,0.08); }
        header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
        header h1 { margin:0; font-size:1.6rem; }
        nav a { margin-left:12px; color:#0366d6; text-decoration:none; }
        .hero { padding:12px 0; border-bottom:1px solid #eee; margin-bottom:16px; }
        .section { margin-bottom:20px; }
        .headline-list { list-style:none; padding:0; margin:0; }
        .headline-item { padding:12px 8px; border-bottom:1px solid #eee; display:flex; flex-direction:column; }
        .headline-item a.title { font-weight:600; color:#111; text-decoration:none; margin-bottom:6px; }
        .meta { font-size:0.9rem; color:#666; }
        .btn { display:inline-block; padding:8px 12px; background:#0366d6; color:#fff; text-decoration:none; border-radius:4px; }
        .small { font-size:0.9rem; color:#666; }
        .error { color:#b00020; }
    </style>
</head>
<body>
<div class="container">
    <header>
        <h1><a href="${pageContext.request.contextPath}/" style="text-decoration:none;color:inherit;">ENews</a></h1>
        <nav>
            <c:choose>
                <c:when test="${not empty sessionScope.username}">
                    <span class="small">Welcome, <strong>${fn:escapeXml(sessionScope.username)}</strong></span>
                    <a href="${pageContext.request.contextPath}/profile">Profile</a>
                    <a href="${pageContext.request.contextPath}/logout">Logout</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login">Login</a>
                    <a href="${pageContext.request.contextPath}/register">Register</a>
                </c:otherwise>
            </c:choose>
        </nav>
    </header>

    <div class="hero">
        <p class="small">Latest headlines fetched from the configured news provider. For full internal articles, visit the site articles list below.</p>
        <a class="btn" href="${pageContext.request.contextPath}/news">View Site Articles</a>
    </div>

    <div class="section" id="external-headlines">
        <h2>Top Headlines (external)</h2>
        <div id="headlines-loading" class="small">Loading headlines…</div>
        <ul id="headlines" class="headline-list" aria-live="polite"></ul>
        <div id="headlines-error" class="error" style="display:none;"></div>
    </div>

    <div class="section">
        <h2>About</h2>
        <p class="small">This site shows cached headlines from an external provider and stores internal articles in the application database. Clicking internal articles requires login.</p>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/app.js"></script>
<script>
    (function () {
        const listEl = document.getElementById('headlines');
        const loadingEl = document.getElementById('headlines-loading');
        const errorEl = document.getElementById('headlines-error');

        function isoToLocal(iso) {
            try {
                const d = new Date(iso);
                if (isNaN(d.getTime())) return '';
                return d.toLocaleString();
            } catch (e) {
                return '';
            }
        }

        function renderHeadlines(items) {
            listEl.innerHTML = '';
            if (!items || items.length === 0) {
                loadingEl.textContent = 'No headlines available.';
                return;
            }
            loadingEl.style.display = 'none';
            items.forEach(function (it, idx) {
                const li = document.createElement('li');
                li.className = 'headline-item';

                const a = document.createElement('a');
                a.className = 'title';
                // Prefer linking to internal article if an id exists, otherwise open provider link
                if (it.id) {
                    a.href = '${pageContext.request.contextPath}/news/detail?id=' + encodeURIComponent(it.id);
                    a.target = '_self';
                } else if (it.sourceUrl) {
                    a.href = it.sourceUrl;
                    a.target = '_blank';
                    a.rel = 'noopener noreferrer';
                } else {
                    a.href = '#';
                }
                a.textContent = it.title || '(No title)';
                li.appendChild(a);

                const meta = document.createElement('div');
                meta.className = 'meta';
                const source = it.sourceName ? it.sourceName : 'Unknown source';
                const published = it.publishedAt ? isoToLocal(it.publishedAt) : '';
                meta.textContent = source + (published ? ' • ' + published : '');
                li.appendChild(meta);

                if (it.summary) {
                    const desc = document.createElement('div');
                    desc.className = 'small';
                    desc.textContent = it.summary;
                    li.appendChild(desc);
                }

                listEl.appendChild(li);
            });
        }

        // Fetch cached news from server endpoint
        fetch('${pageContext.request.contextPath}/api/news', { credentials: 'same-origin' })
            .then(function (resp) {
                if (!resp.ok) throw new Error('Network response was not ok: ' + resp.status);
                return resp.json();
            })
            .then(function (data) {
                // The API may return NewsItem objects; ensure we map fields consistently.
                // Some implementations return java.sql.Timestamp which becomes a string in JSON.
                renderHeadlines(data);
            })
            .catch(function (err) {
                loadingEl.style.display = 'none';
                errorEl.style.display = 'block';
                errorEl.textContent = 'Failed to load headlines. Showing site articles instead.';
                console.error('Failed to fetch /api/news:', err);
            });
    })();
</script>
</body>
</html>
