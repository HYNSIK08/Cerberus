<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    String csrf = (String) session.getAttribute("CSRF_TOKEN");
    if (csrf == null) csrf = com.secauth.util.CsrfUtil.getOrCreate(session);
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>사용자 관리 — SecureAuth</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="app-layout">
    <%@ include file="/WEB-INF/jsp/layout/nav.jsp" %>
    <main class="main-content">
        <div class="page-header">
            <h1>사용자 관리</h1>
        </div>

        <table class="data-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>아이디</th>
                    <th>권한</th>
                    <th>상태</th>
                    <th>로그인 실패</th>
                    <th>잠금 해제</th>
                    <th>최근 로그인</th>
                    <th>가입일</th>
                    <th>작업</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty users}">
                        <tr><td colspan="9" class="td-empty">사용자가 없습니다.</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="u" items="${users}">
                            <tr class="${!u.enabled ? 'row-disabled' : ''}">
                                <td class="td-mono"><c:out value="${u.id}"/></td>
                                <td><strong><c:out value="${u.username}"/></strong></td>
                                <td>
                                    <span class="badge badge-${u.role == 'ADMIN' ? 'admin' : 'user'}">
                                        <c:out value="${u.role}"/>
                                    </span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${u.locked}">
                                            <span class="badge badge-danger">잠금</span>
                                        </c:when>
                                        <c:when test="${u.enabled}">
                                            <span class="badge badge-success">활성</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-muted">비활성</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="td-center">
                                    <c:out value="${u.failedAttempts}"/>
                                </td>
                                <td class="td-mono td-small">
                                    <c:out value="${u.lockedUntil != null ? u.lockedUntil : '-'}"/>
                                </td>
                                <td class="td-mono td-small">
                                    <c:out value="${u.lastLogin != null ? u.lastLogin : '-'}"/>
                                </td>
                                <td class="td-mono td-small">
                                    <c:out value="${u.createdAt}"/>
                                </td>
                                <td>
                                    <c:if test="${u.username != sessionScope.username}">
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/admin/users/toggle"
                                              style="display:inline;">
                                            <input type="hidden" name="_csrf" value="<%= csrf %>">
                                            <input type="hidden" name="userId" value="${u.id}">
                                            <button type="submit"
                                                    class="btn btn-sm ${u.enabled ? 'btn-danger' : 'btn-success'}"
                                                    onclick="return confirm('${u.enabled ? '비활성화' : '활성화'} 하시겠습니까?')">
                                                ${u.enabled ? '비활성화' : '활성화'}
                                            </button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </main>
</div>
</body>
</html>
