<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String[] colors = {"aqua", "black", "blue", "fuchsia", "gray", "green", "lime", "maroon", "navy", "olive", "orange", "purple", "red", "silver", "teal", "white", "yellow"};
	String rgba = request.getParameter("c");
%>
<!DOCTYPE html>
<html>
<head>
<title>Standard colors</title>
<style type="text/css">
#colors {
	margin: 10px;
}
/* #color div {
	display:inline-block;
	width:100px; height:100px;
	text-shadow: 1px 1px 1px white;
	border-radius: 10px;
	text-align: center;
} */
.colordvcon {
	display: inline-block;
	margin: 3px 1px;
	text-align: center;
	font-family: Courier,monospace;
	padding: 6px;
	width: 132px;
	border-radius: 5px;
	-webkit-border-radius: 5px;
}
.colordva {
	height: 120px;
	width: 120px;
	border-radius: 5px;
	-webkit-border-radius: 5px;
}
</style>
</head>
<body>
<h3>If you see custom color, input parameter by <code>?c=rgba(123,123,123,0.5)</code></h3>
<div id="colors">
<%	for (String color : colors) { %>
	<div class="colordvcon">
		<div class="colordva" style="background-color: <%=color%>;"></div>
		<code><%=color%></code>
	</div>
<%	} %>
	<div class="colordvcon">
		<div class="colordva" style="background-color: <%=rgba%>;"></div>
		<code><%=rgba%></code>
	</div>
</div>

<div class="alert alert-info" role="alert">
	Find more color <a href="http://www.color-hex.com/" target="_blank">www.color-hex.com</a>
</div>

</body>
</html>