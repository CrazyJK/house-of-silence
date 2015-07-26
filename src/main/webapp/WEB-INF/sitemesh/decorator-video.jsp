<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="${locale}">
<head>
<meta charset="UTF-8" />
<title><sitemesh:write property='title'>Title goes here</sitemesh:write> - Crazy</title>
<link rel="shortcut icon" type="image/x-icon" href="<c:url value="/res/img/favicon-video.ico" />">
<link rel="stylesheet" href="<c:url value="/res/css/common.css"/>" />
<link rel="stylesheet" href="<c:url value="/res/css/scrollbar.css"/>" />
<link rel="stylesheet" href="<c:url value="/res/css/video-deco.css"/>" />
<link rel="stylesheet" href="<c:url value="/res/css/video-main.css"/>" />
<link rel="stylesheet" href="<c:url value="/res/css/video-common.css"/>" />
<!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
  <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
  <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
<![endif]-->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"></script>
<script src="<c:url value="/res/js/common.js"/>" type="text/javascript"></script>
<script src="<c:url value="/res/js/video.js"/>" type="text/javascript"></script>
<script type="text/javascript">
var context = '<c:url value="/"/>';
var locationPathname = window.location.pathname;
var currBGImageNo = 0;
var bgImageCount = ${bgImageCount};
/** content_div에 이미지를 보여줄지 여부 */
var bgContinue = true;

$(document).ready(function() {

	//set rank color
 	$('input[type="range"]').each(function() {
 		fnRankColor($(this));
 	});

	// Add listener : if labal click, empty input text value
	$("label").bind("click", function(){
		var id = $(this).attr("for");
		$("#" + id).val("");
	});

 	showNav();
 	
	$(window).bind("resize", resizeDivHeight);

	resizeDivHeight();

	if (bgContinue) {
		setBackgroundImage();
		setInterval(
				function() {
					setBackgroundImage();
				}, 
				60*1000);
	}
	loading(false);
});

/**
 * 현재 url비교하여 메뉴 선택 효과를 주고, 메뉴 이외의 창에서는 nav를 보이지 않게
 */
function showNav() {
	var found = false;
	$("nav#deco_nav ul li a").each(function() {
		if ($(this).attr("href") == locationPathname) {
			$(this).parent().addClass("menu-selected");
			found = true;
		}
	});
	if(!found)
		$("nav#deco_nav").css("display", "none");
}
/**
 * post 액션
 */
function actionFrame(reqUrl, method, msg) {
	$.ajax({
		type : method ? method : "POST",
		url : reqUrl,
		beforeSend : function() {
			loading(true, msg ? msg : "Loading...");
		}
	}).done(function(msg) {
		loading(true, "Done", 3000);
	}).fail(function(xhr, status, error) {
		loading(true, "fail : [" + status + "] "+ error);
	}).always(function() {
		//loading(false);
	});
	/*
	var actionFrm = document.forms['actionFrm'];
	actionFrm.action = url;
	if (method)
		$("#hiddenHttpMethod").val(method);
	actionFrm.submit();
	*/
}
function loading(show, msg, interval) {
	if (show)
		$("#loading-wrapper").css("display", "table");
	else
		$("#loading-wrapper").hide();
	if (msg)
		$("#loading-msg").html(msg);
	if (interval)
		$("#loading-wrapper").fadeOut(interval);
}
</script>

<sitemesh:write property="head" />

</head>
<body>

<div id="loading-wrapper">
	<div id="loading-wrapper-inner">
		<div id="loading-container">
			<span id="loading-msg"  onclick="loading(false);">Loading</span>
		</div>
	</div>
</div>
<script type="text/javascript">
loading(true, "Loading...");
</script> 
<nav id="deco_nav">
	<ul>
		<li><a href="<c:url value="/video"/>"><s:message code="video.main"/></a>
		<li><a href="<c:url value="/video/search"/>"><s:message code="video.search"/></a>
		<li><a href="<c:url value="/video/list"/>"><s:message code="video.video"/></a>
		<li><a href="<c:url value="/video/actress"/>"><s:message code="video.actress"/></a>
		<li><a href="<c:url value="/video/studio"/>"><s:message code="video.studio"/></a>
		<li><a href="<c:url value="/image"/>"><s:message code="video.image"/></a>
		<li><a href="<c:url value="/image/canvas"/>"><s:message code="default.canvas"/></a>
		<li><a href="<c:url value="/video/briefing"/>"><s:message code="video.briefing"/></a>
		<li><a href="<c:url value="/video/torrent"/>"><s:message code="video.torrent"/></a>
		<li><a href="<c:url value="/video/parseToTitle"/>"><s:message code="video.parseToTitle"/></a>
		<li><a href="<c:url value="/home"/>"><s:message code="default.home"/></a>
	</ul>
</nav>

<sitemesh:write property="body">Body goes here. Blah blah blah.</sitemesh:write>
	
<form name="actionFrm" target="ifrm" method="post"><input type="hidden" name="_method" id="hiddenHttpMethod"/></form>
<iframe id="actionIframe" name="ifrm" style="display:none; width:100%;"></iframe>

</body>
</html>