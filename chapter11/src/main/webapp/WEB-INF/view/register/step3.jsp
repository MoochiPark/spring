<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: daewon
  Date: 2020/04/14
  Time: 2:06 오후
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>회원가입</title>
</head>
<body>
<p><strong>${formData.name}님 </strong>회원 가입을 완료했습니다.</p>
<p><a href="<c:url value='/main'/>">[첫 화면 이동]</a> </p>
</body>
</html>
