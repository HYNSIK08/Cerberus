<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    com.secauth.util.CsrfUtil.getOrCreate(session);
    String csrf = (String) session.getAttribute("CSRF_TOKEN");
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인 — SecureAuth</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="login-body">
<div class="login-wrap">
    <div class="login-logo">
        <span class="logo-icon">&#9632;</span>
        <span class="logo-text">SecureAuth</span>
    </div>
    <div class="login-card">
        <h2>시스템 로그인</h2>
        <c:if test="${not empty error}">
            <div class="alert alert-error"><c:out value="${error}"/></div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/login" autocomplete="off">
            <input type="hidden" name="_csrf" value="<%= csrf %>">
            <div class="form-group">
                <label for="username">아이디</label>
                <input type="text" id="username" name="username"
                       maxlength="50" required autofocus
                       value="<c:out value='${param.username}'/>">
            </div>
            <div class="form-group">
                <label for="password">비밀번호</label>
                <input type="password" id="password" name="password" maxlength="100" required>
            </div>
            <button type="submit" class="btn btn-primary btn-full">로그인</button>
        </form>
    </div>
</div>
</body>
</html>
