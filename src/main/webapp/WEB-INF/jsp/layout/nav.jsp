<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    String csrf = (String) session.getAttribute("CSRF_TOKEN");
    if (csrf == null) {
        csrf = com.secauth.util.CsrfUtil.getOrCreate(session);
    }
    String currentPath = request.getServletPath();
%>
<aside class="sidebar">
    <div class="sidebar-logo">
        <span class="logo-icon">&#9632;</span> SecureAuth
    </div>
    <nav class="sidebar-nav">
        <a href="${pageContext.request.contextPath}/dashboard"
           class="nav-item <%= currentPath.startsWith("/dashboard") ? "active" : "" %>">
            대시보드
        </a>
        <c:if test="${sessionScope.role == 'ADMIN'}">
            <div class="nav-group-label">관리자</div>
            <a href="${pageContext.request.contextPath}/admin/logs"
               class="nav-item <%= currentPath.contains("/admin/logs") ? "active" : "" %>">
                접근 로그
            </a>
            <a href="${pageContext.request.contextPath}/admin/users"
               class="nav-item <%= currentPath.contains("/admin/users") ? "active" : "" %>">
                사용자 관리
            </a>
            <a href="${pageContext.request.contextPath}/admin/policy"
               class="nav-item <%= currentPath.contains("/admin/policy") ? "active" : "" %>">
                보안 정책
            </a>
        </c:if>
    </nav>
    <div class="sidebar-footer">
        <span class="sidebar-user"><c:out value="${sessionScope.username}"/></span>
        <span class="sidebar-role"><c:out value="${sessionScope.role}"/></span>
        <form method="post" action="${pageContext.request.contextPath}/logout" style="margin-top:8px;">
            <input type="hidden" name="_csrf" value="<%= csrf %>">
            <button type="submit" class="btn-logout">로그아웃</button>
        </form>
    </div>
</aside>
