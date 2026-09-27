<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>접근 로그 — SecureAuth</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="app-layout">
    <%@ include file="/WEB-INF/jsp/layout/nav.jsp" %>
    <main class="main-content">
        <div class="page-header">
            <h1>접근 로그</h1>
            <div class="header-actions">
                <span id="live-badge" class="badge badge-live">● LIVE</span>
            </div>
        </div>

        <div class="filter-bar">
            <form method="get" action="${pageContext.request.contextPath}/admin/logs">
                <select name="result" onchange="this.form.submit()">
                    <option value="">전체</option>
                    <option value="SUCCESS" ${filter == 'SUCCESS' ? 'selected' : ''}>성공</option>
                    <option value="FAILURE" ${filter == 'FAILURE' ? 'selected' : ''}>실패</option>
                </select>
            </form>
            <span class="filter-total">총 <strong><c:out value="${total}"/></strong>건</span>
        </div>

        <table class="data-table" id="log-table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>일시</th>
                    <th>사용자</th>
                    <th>IP 주소</th>
                    <th>액션</th>
                    <th>결과</th>
                    <th>상세</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty logs}">
                        <tr><td colspan="7" class="td-empty">로그가 없습니다.</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="log" items="${logs}">
                            <tr class="${log.result == 'FAILURE' ? 'row-danger' : ''}">
                                <td class="td-mono td-id"><c:out value="${log.id}"/></td>
                                <td class="td-mono"><c:out value="${log.loggedAt}"/></td>
                                <td><c:out value="${log.username != null ? log.username : '-'}"/></td>
                                <td class="td-mono"><c:out value="${log.ipAddress}"/></td>
                                <td><c:out value="${log.action}"/></td>
                                <td>
                                    <span class="badge badge-${log.result == 'SUCCESS' ? 'success' : 'danger'}">
                                        <c:out value="${log.result}"/>
                                    </span>
                                </td>
                                <td class="td-detail"><c:out value="${log.detail != null ? log.detail : '-'}"/></td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>

        <!-- 페이지네이션 -->
        <div class="pagination">
            <c:if test="${page > 1}">
                <c:url var="prevUrl" value="/admin/logs">
                    <c:param name="page"   value="${page - 1}"/>
                    <c:param name="result" value="${filter}"/>
                </c:url>
                <a href="${prevUrl}" class="page-btn">&#8592; 이전</a>
            </c:if>
            <span class="page-info"><c:out value="${page}"/> / <c:out value="${totalPages > 0 ? totalPages : 1}"/></span>
            <c:if test="${page < totalPages}">
                <c:url var="nextUrl" value="/admin/logs">
                    <c:param name="page"   value="${page + 1}"/>
                    <c:param name="result" value="${filter}"/>
                </c:url>
                <a href="${nextUrl}" class="page-btn">다음 &#8594;</a>
            </c:if>
        </div>
    </main>
</div>
<script src="${pageContext.request.contextPath}/js/main.js"></script>
<script>
    // 30초마다 최신 로그 수 확인 (LIVE 배지 점멸)
    const BASE = '${pageContext.request.contextPath}';
    let lastCount = ${total};

    setInterval(() => {
        fetch(BASE + '/api/stats')
            .then(r => r.json())
            .then(d => {
                if (d.totalLogs !== lastCount) {
                    lastCount = d.totalLogs;
                    document.getElementById('live-badge').classList.add('pulse');
                    setTimeout(() => document.getElementById('live-badge').classList.remove('pulse'), 1000);
                }
            })
            .catch(() => {});
    }, 30000);
</script>
</body>
</html>
