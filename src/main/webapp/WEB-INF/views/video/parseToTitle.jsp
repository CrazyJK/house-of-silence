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
function resizeSecondDiv() {
	$("#inputDiv").outerHeight(calculatedDivHeight - 25);	
}
function toggleInputDiv() {
	$("#inputDiv").toggle("fast", function() {
		if ($(this).css("display") == "none") {
			$("#resultList").css("margin-left", "0px");
		}
		else {
			$("#resultList").css("margin-left", "320px");
		}
	});
}
</script>
</head>
<body>
<div id="header_div" class="div-box">
	<form method="post" onsubmit="loading(true, 'Parsing...')">
		<table style="width:100%;">
			<tr>
				<td width="150px;">
					<input type="button" onclick="toggleInputDiv()" value="hide"/>
					<input type="submit" value="Parse(${fn:length(titleList)})"/>
				</td>
				<td></td>
				<td><textarea class="titleArea" placeholder="parsing result" readonly><c:forEach items="${titleList}" var="title" varStatus="status">${title}
</c:forEach></textarea></td>
			</tr>
		</table>
		<div id="inputDiv" style="position:absolute; top:100px; left:20px; width:300px;">
			<textarea id="titleData" name="titleData" class="titleArea" placeholder="input title data">${titleData}</textarea>
		</div>
	</form>
</div>

<div id="content_div" class="div-box" style="overflow:auto;">
	<table id="resultList" style="margin-left:320px; width:100%;">
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
								<input id="dataTitle_${title.opus}" class="text" style="width:800px;" value="${title}"/>
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