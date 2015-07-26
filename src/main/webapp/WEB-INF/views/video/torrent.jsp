<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" 	uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html>
<head>
<title><s:message code="video.torrent"/></title>
<style type="text/css">
#searchInput {
	width:200px;
}
</style>
<script type="text/javascript">
var totalCandidatedVideo = 0;

$(document).ready(function(){
	$("#totalCandidatedVideo").html(totalCandidatedVideo);
	
	$("#searchInput").bind("keyup", function() {
		var keyword = $(this).val();
		$("div#content_div input:text").each(function() {
			if ($(this).val().toLowerCase().indexOf(keyword.toLowerCase()) > -1)
				$(this).parent().parent().show();
			else
				$(this).parent().parent().hide();
		});
	});
});

function fnGoSearch(opus) {
	fnMarkChoice(opus);
	popup('<c:url value="/video/torrent/search/"/>' + opus, 'torrentSearch', 900, 950);
}
function fnSelectVideo(opus) {
//	fnMarkChoice(opus);
	$("#check-" + opus).hide();
	$("#totalCandidatedVideo").html(--totalCandidatedVideo);
}
function confirmAll() {
//	alert("Not working...");
	$("form input:submit").each(function() {
		$(this).click();
		$(this).parent().parent().parent().hide();
	}); 
}
</script>
</head>
<body>

<div id="header_div" class="div-box">
	<span class="label-large">
		<s:message code="video.torrent-summary" arguments="${fn:length(videoList)}"/><code id="totalCandidatedVideo"></code>
	</span>
	<label class="label-large">
		<input type="search" id="searchInput" class="searchInput" placeHolder="<s:message code="video.search"/>" />
	</label>
	<span class="label-large" onclick="confirmAll()">Confirm all!!!</span>
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
			<td>
				<c:forEach items="${video.videoCandidates}" var="candidate">
				<form method="post" target="ifrm" action="<c:url value="/video/${video.opus}/confirmCandidate"/>">
					<input type="hidden" name="path" value="${candidate.absolutePath}"/>
					<input type="submit" value="${candidate.name}" onclick="fnSelectVideo('${video.opus}');"/>
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
