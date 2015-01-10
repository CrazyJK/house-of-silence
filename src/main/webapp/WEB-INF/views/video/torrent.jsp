<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" 	uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html>
<head>
<title><s:message code="video.torrent"/></title>
<script type="text/javascript">
var totalCandidatedVideo = 0;

$(document).ready(function(){
//	$("td").addClass("nowrap");
	$("#totalCandidatedVideo").html(totalCandidatedVideo);
});

function searchInput(keyword) {
	$("div#content_div input").each(function() {
		if ($(this).val().toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
			$(this).parent().parent().show();
		}
		else {
			$(this).parent().parent().hide();
		}
	});
}
function fnGoSearch(opus) {
	fnMarkChoice(opus);
	popup('<c:url value="/video/torrent/search/"/>' + opus, 'torrentSearch', 900, 950);
}
function fnSelectVideo(opus) {
	fnMarkChoice(opus);
	$("#totalCandidatedVideo").html(--totalCandidatedVideo);
}
</script>
</head>
<body>

<div id="header_div" class="div-box">
	<s:message code="video.search"/>
	<input type="search" style="width:200px;" class="searchInput" 
		placeHolder="<s:message code="video.search"/>" onkeyup="searchInput(this.value)"/>
	<s:message code="video.torrent-summary" arguments="${fn:length(videoList)}"/>
	<span id="totalCandidatedVideo"></span>
</div>

<div id="content_div" class="div-box" style="overflow:auto;">
	<table class="video-table">
		<c:if test="${empty videoList}">
		<tr>
			<td>
				No Video
			</td>
		</tr>
		</c:if>
		<c:forEach items="${videoList}" var="video" varStatus="status">
		<tr id="check-${video.opus}" class="nowrap">
			<td class="number">
				${status.count}
			</td>
			<td class="label">
				<span style="margin-right:10px;">
					<a onclick="fnGoSearch('${video.opus}');">Torrent</a>
				</span>
				<input value="${video.fullname}" class="text" style="width:600px;" onclick="fnViewVideoDetail('${video.opus}')" />
			</td>
			<td style="width:100%;">
				<c:forEach items="${video.videoCandidates}" var="candidate">
				<form method="post" target="ifrm" action="<c:url value="/video/${video.opus}/confirmCandidate"/>">
					<input type="submit" value="${candidate.name}" onclick="fnSelectVideo('${video.opus}')"/>
					<input type="hidden" name="path" value="${candidate.absolutePath}"/>
				</form>
				<script type="text/javascript">
					totalCandidatedVideo += 1;	
				</script>
				</c:forEach>
			</td>
		</tr>
		</c:forEach>
	</table>
</div>

</body>
</html>
