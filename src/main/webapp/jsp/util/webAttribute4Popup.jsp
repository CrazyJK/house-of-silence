<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"        uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<link rel="shortcut icon" type="image/x-icon" href="<c:url value="/res/img/favicon-kamoru.ico"/>">
<title>Web Attribute</title>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css">
<link rel="stylesheet" href="<c:url value="/res/css/crazy-deco.css" />" />
<link rel="stylesheet" href="<c:url value="/res/css/common.css" />" />
</head>
<body>
<span style="float:right;"><a href="javascript:location.reload();">reload</a></span>
<jsp:include page="webAttribute.jsp"></jsp:include>
</body>
</html>