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
    <title>보안 정책 — SecureAuth</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="app-layout">
    <%@ include file="/WEB-INF/jsp/layout/nav.jsp" %>
    <main class="main-content">
        <div class="page-header">
            <h1>보안 정책 설정</h1>
        </div>

        <c:if test="${param.saved == '1'}">
            <div class="alert alert-success" style="margin-bottom:16px;">정책이 저장되었습니다.</div>
        </c:if>

        <div class="card" style="max-width:520px;">
            <form method="post" action="${pageContext.request.contextPath}/admin/policy">
                <input type="hidden" name="_csrf" value="<%= csrf %>">
                <c:forEach var="entry" items="${policies}">
                    <div class="form-group">
                        <label><c:out value="${entry.key}"/></label>
                        <div style="display:flex;gap:8px;align-items:center;">
                            <input type="hidden" name="key" value="<c:out value='${entry.key}'/>">
                            <input type="text" name="value"
                                   value="<c:out value='${entry.value}'/>"
                                   style="width:120px;">
                        </div>
                    </div>
                </c:forEach>
                <button type="submit" class="btn btn-primary" style="margin-top:8px;">저장</button>
            </form>
        </div>
    </main>
</div>
</body>
</html>
