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
	height:100%;
	font-size:11px;
	opacity: 0.75;
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
$(document).ready(function() {
	$("#titleData").outerHeight(calculatedDivHeight - 5);	
});
</script>
</head>
<body>
<div id="header_div" class="div-box">
	<form method="post" onsubmit="loading(true, 'Parsing...')">
		<table style="width:100%;">
			<tr>
				<td width="100px;"><input type="submit" value="Parse(${fn:length(titleList)})"/></td>
				<td></td>
				<td><textarea class="titleArea" placeholder="result area"><c:forEach items="${titleList}" var="title" varStatus="status">${title}
</c:forEach></textarea></td>
			</tr>
		</table>
		<div style="position:absolute; top:100px; left:20px; width:300px;">
			<textarea id="titleData" name="titleData" class="titleArea" placeholder="title area">${titleData}</textarea>
		</div>
	</form>
</div>

<div id="content_div" class="div-box" style="overflow:auto;">
	<table style="padding-left:320px; width:100%;">
		<tr>
			<td>
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
			</td>
		</tr>
	</table>
</div>


</body>
</html>