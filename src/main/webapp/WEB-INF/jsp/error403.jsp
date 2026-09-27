<%@ page contentType="text/html; charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>403 — SecureAuth</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="login-body">
<div class="login-wrap">
    <div class="login-card" style="text-align:center;">
        <div style="font-size:48px;color:#DC3545;">&#9888;</div>
        <h2>접근 거부</h2>
        <p>이 페이지에 접근할 권한이 없습니다.</p>
        <p style="font-family:monospace;color:#888;font-size:13px;"><c:out value="${pageContext.errorData.message}"/></p>
        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary" style="margin-top:16px;display:inline-block;">
            대시보드로 돌아가기
        </a>
    </div>
</div>
</body>
</html>
