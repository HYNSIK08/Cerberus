<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>대시보드 — SecureAuth</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="app-layout">
    <%@ include file="/WEB-INF/jsp/layout/nav.jsp" %>
    <main class="main-content">
        <div class="page-header">
            <h1>대시보드</h1>
            <span class="page-sub">환영합니다, <c:out value="${sessionScope.username}"/> 님</span>
        </div>

        <div class="stat-row">
            <div class="stat-card">
                <div class="stat-label">오늘 로그인 성공</div>
                <div class="stat-value success"><c:out value="${todaySuccess}"/></div>
            </div>
            <div class="stat-card">
                <div class="stat-label">오늘 로그인 실패</div>
                <div class="stat-value danger"><c:out value="${todayFail}"/></div>
            </div>
            <div class="stat-card">
                <div class="stat-label">전체 사용자</div>
                <div class="stat-value"><c:out value="${totalUsers}"/></div>
            </div>
        </div>

        <div class="section">
            <h2 class="section-title">최근 접근 이력 (내 계정)</h2>
            <table class="data-table">
                <thead>
                    <tr>
                        <th>일시</th>
                        <th>액션</th>
                        <th>IP 주소</th>
                        <th>결과</th>
                        <th>상세</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty recentLogs}">
                            <tr><td colspan="5" class="td-empty">접근 이력이 없습니다.</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="log" items="${recentLogs}">
                                <tr>
                                    <td class="td-mono"><c:out value="${log.loggedAt}"/></td>
                                    <td><c:out value="${log.action}"/></td>
                                    <td class="td-mono"><c:out value="${log.ipAddress}"/></td>
                                    <td>
                                        <span class="badge badge-${log.result == 'SUCCESS' ? 'success' : 'danger'}">
                                            <c:out value="${log.result}"/>
                                        </span>
                                    </td>
                                    <td><c:out value="${log.detail != null ? log.detail : '-'}"/></td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </main>
</div>
</body>
</html>
