<%@ tag language="java" pageEncoding="UTF-8" body-content="tagdependent"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="s"   uri="http://www.springframework.org/tags"%>

<%@ attribute name="video"   required="true" type="jk.kamoru.crazy.video.domain.Video"%>
<%@ attribute name="view"    required="true"%>
<%@ attribute name="mode"    required="false"%><%-- mode : s(Small), l(Large) --%>
<%@ attribute name="tooltip" required="false"%><%-- tooltip test --%>

<c:set var="cssClass" value="${mode eq 'l' ? 'label-large' : 'label'}"/>
<c:set var="ONE_GB" value="${1024*1024*1024}"/>

<%
	if (view.equalsIgnoreCase("video")) {
%>
<span class="${cssClass} ${video.existVideoFileList ? 'exist' : 'nonExist'}" 
		title="${video.playCount} played" 
		onclick="fnPlay('${video.opus}')">
			${mode eq 's' ? 'V' : 'Video'}
<c:if test="${mode eq 'l'}">			
			<em>${video.size}</em>
			<em><fmt:formatNumber value="${video.length / ONE_GB}" pattern="#,##0GB"/></em>
</c:if>			
</span>
<%
	} else if (view.equalsIgnoreCase("cover")) {
%>
<span class="${cssClass} ${video.existCoverFile ? 'exist' : 'nonExist'}" onclick="fnImageView('${video.opus}')">${mode eq 's' ? 'C' : 'Cover'}</span>
<%
	} else if (view.equalsIgnoreCase("subtitles")) {
%>
<span class="${cssClass} ${video.existSubtitlesFileList ? 'exist' : 'nonExist'}" onclick="fnEditSubtitles('${video.opus}')">${mode eq 's' ? 's' : 'smi'}</span>
<%
	} else if (view.equalsIgnoreCase("overview")) {
%>
<span class="${cssClass} " onclick="fnEditOverview('${video.opus}')" title="${video.overviewText}">${mode eq 's' ? 'O' : video.existOverview ? video.overviewText : 'Overview'}</span>
<%
	} else if (view.equalsIgnoreCase("download")) {
%>
<span class="${cssClass}" title="download date">${video.videoDate}</span>
<%
	} else if (view.equalsIgnoreCase("release")) {
%>
<span class="${cssClass}" title="release date">${video.releaseDate}</span>
<%
	} else if (view.equalsIgnoreCase("actress")) {
%>
<c:forEach items="${video.actressList}" var="actress" varStatus="status">
<span class="${cssClass}" onclick="fnViewActressDetail('${actress.name}')" title="${actress}">${actress.name}
<c:if test="${mode eq 'l'}"><em>${actress.age}</em></c:if></span>
<c:if test="${mode eq 'l'}">
	<span class="${cssClass}" onclick="fnFavorite(this, '${actress.name}')">${actress.favorite ? '★' : '☆'}</span>
	<img src="<c:url value="/res/img/magnify${status.count%2}.png"/>" width="12px" title="<s:message code="video.find-info.actress"/>"
			onclick="popup('<s:eval expression="@prop['url.search.actress']"/>${actress.reverseName}', 'info_${actress.name}', 800, 600)"/>
</c:if>
</c:forEach>
<%
	} else if (view.equalsIgnoreCase("opus")) {
%>
<span class="${cssClass}">${video.opus}</span>
<c:if test="${mode eq 'l'}">
	<img src="<c:url value="/res/img/magnify${status.count%2}.png"/>" width="12px" title="<s:message code="video.find-info.opus"/>"
		onclick="popup('<s:eval expression="@prop['url.search.video']"/>${video.opus}', 'info_${video.opus}', 800, 600)"/>
</c:if>
<%
	} else if (view.equalsIgnoreCase("torrent")) {
%>
<img src="<c:url value="/res/img/magnify${status.count%2}.png"/>" width="12px" title="<s:message code="video.find-info.torrent"/>"
	onclick="popup('<s:eval expression="@prop['url.search.torrent']"/>${video.opus}', 'torrentDownload', 800, 600); this.style.backgroundColor='red';"/>
<%
	} else if (view.equalsIgnoreCase("length")) {
%>
<fmt:formatNumber value="${video.length / ONE_GB}" pattern="#,##0GB"/>
<%
	} else if (view.equalsIgnoreCase("studio")) {
%>
<span class="${cssClass}" onclick="fnViewStudioDetail('${video.studio.name}')" title="${video.studio}">${video.studio.name}</span>
<%
	} else if (view.equalsIgnoreCase("title")) {
%>
<span class="${cssClass}" onclick="fnVideoDetail('${video.opus}')">${video.title}</span>
<%
	} else if (view.equalsIgnoreCase("score")) {
%>
<span class="${cssClass} rangeLabel" title="${video.scoreDesc}">${video.score}</span>
<c:if test="${mode eq 'l'}">
	<span class="label" onclick="fnVideoReset('${video.opus}')">Reset</span>
	<span class="label" onclick="fnVideoWrong('${video.opus}')">Wrong</span>
</c:if>
<%
	} else if (view.equalsIgnoreCase("rank")) {
%>
<input type="range" id="Rank-${video.opus}" name="points" ${mode eq 's' ? 'style="width:70px;"' : ''} 
	min="<s:eval expression="@prop['rank.minimum']"/>" max="<s:eval expression="@prop['rank.maximum']"/>" 
	value="${video.rank}" onmouseup="fnRank('${video.opus}')" onchange="document.getElementById('Rank-${video.opus}-label').innerHTML = this.value;" />
<em id="Rank-${video.opus}-label" class="${cssClass} rangeLabel">${video.rank}</em>
<%
	} else if (view.equalsIgnoreCase("label")) {
%>
<span class="${cssClass}" onclick="fnVideoDetail('${video.opus}')" title="${video.fullname} ${tooltip}" data-opus="${video.opus}">${mode eq 'simple' ? 'O' : video.opus}</span>
<%
	} else {
%>
${view} is invalid argement
<%
	}
%>