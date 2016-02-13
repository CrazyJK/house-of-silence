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
#currentVideo {
	margin: 5px;
	font-size: 0.8em;
	display: none; 
	position: absolute; 
	top: 60px; 
	left: 0px; 
	width: 100%; 
	height: 50px; 
	background-color: rgb(231, 231, 120); 
	overflow: hidden; 
	margin: 0; 
	padding: 0; 
	z-index: 99;
}
#currentVideo > p {
	margin: 8px;
}
</style>
<script type="text/javascript">
var totalCandidatedVideo = 0;
var MODE_TORRENT = 1;
var MODE_CANDIDATED = 2;
var isHideClickedTorrentButton = false;

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
	popup('<c:url value="/video/"/>' + opus + '/cover/title', 'SearchTorrentCover');
	popup('<c:url value="/video/torrent/search/"/>' + opus, 'torrentSearch', 900, 950);

	if (isHideClickedTorrentButton) {
		$("#check-" + opus).hide();
		$("#currentVideo").hide();
		$("#currentVideo > p").html($("#fullname-"+opus).val());
		$("#currentVideo").fadeIn('slow');
	}
	else {
		fnMarkChoice(opus);
	}
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
function fnChangeMode(mode) {
	if (mode == MODE_TORRENT) {
		isHideClickedTorrentButton = true;
		$(".torrentButton").css("height", "50px");
	}
	else if (mode == MODE_CANDIDATED) {
		isHideClickedTorrentButton = false;
		$(".torrentButton").css("height", "");
	}
}
</script>
</head>
<body>

<div id="header_div" class="div-box">
	<ul class="menu-item-ul">
		<li class="label-large">
			<s:message code="video.torrent-summary" arguments="${fn:length(videoList)}"/><code id="totalCandidatedVideo"></code>
		</li>
		<li class="label-large">Mode
			<label><input type="radio" name="mode" onclick="fnChangeMode(MODE_CANDIDATED);" checked>File</label>
			<label><input type="radio" name="mode" onclick="fnChangeMode(MODE_TORRENT);">Torrent</label>
		</li>
		<li class="label-large">
			<input type="search" id="searchInput" class="searchInput" placeHolder="<s:message code="video.search"/>" />
		</li>
		<li class="label-large">
			<a onclick="confirmAll()">Confirm all!!!</a>
		</li>
	</ul>
</div>

<div id="currentVideo" class="div-box" style="">
	<p></p>
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
			<td class="label torrentButton" onclick="fnGoSearch('${video.opus}');">
				Torrent
			</td>
			<td>
				<input id="fullname-${video.opus}" value="${video.fullname}" class="text" style="width:600px;" onclick="fnViewVideoDetail('${video.opus}')" />
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
