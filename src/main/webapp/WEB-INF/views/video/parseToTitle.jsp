<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"    uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"   uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" 	 uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri='http://www.springframework.org/tags/form'%>

<!DOCTYPE html>
<html>
<head>
<title>Parse to Title</title>
<style type="text/css">
.titleArea {
	width:100%;
	font-size:11px;
}
code {
	margin: 0 5px;
	color: red;
}
</style>
<script src="<c:url value="/res/zeroclipboard/ZeroClipboard.js"/>"></script>
<script type="text/javascript">
function fnFindVideo(opus) {
	fnMarkChoice(opus);
	popup('<s:eval expression="@prop['url.search.video']"/>' + opus, 'videoSearch', 900, 950);
}
</script>
</head>
<body>
<div id="header_div" class="div-box">
	<form method="post">
		<table style="width:100%;">
			<tr>
				<td width="100px;"><input type="submit" value="Parse(${fn:length(titleList)})"/></td>
				<td><textarea name="titleData" class="titleArea">${titleData}</textarea></td>
				<td><textarea class="titleArea"><c:forEach items="${titleList}" var="title" varStatus="status">${title}
</c:forEach></textarea></td>
			</tr>
		</table>
	</form>
</div>

<div id="content_div" class="div-box" style="overflow:auto;">
	<table class="video-table">
		<c:if test="${empty titleList}">
			<tr>
				<td>
					No Video
				</td>
			</tr>
		</c:if>
		<c:forEach items="${titleList}" var="title" varStatus="status">
			<tr id="check-${title.opus}" style="font-size:11px; color:blue;">
				<td class="number">
					${status.count}
				</td>
				<td class="label">
					<span style="margin-right:10px;">
						<a id="copyBtn_${title.opus}" data-clipboard-target="dataTitle_${title.opus}" onclick="fnFindVideo('${title.opus}')">Get Info </a>
						<c:if test="${title.check}"><code>${title.checkDesc}</code></c:if>
					</span>
					<input id="dataTitle_${title.opus}" class="text" style="width:600px;" value="${title}"/>
				</td>
			</tr>
			<script type="text/javascript">new ZeroClipboard(document.getElementById("copyBtn_${title.opus}"));</script>
		</c:forEach>
	</table>
</div>

</body>
</html>