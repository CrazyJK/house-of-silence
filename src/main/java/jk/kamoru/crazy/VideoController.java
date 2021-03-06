package jk.kamoru.crazy;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jk.kamoru.crazy.image.service.ImageService;
import jk.kamoru.crazy.video.VIDEO;
import jk.kamoru.crazy.video.VideoBatch;
import jk.kamoru.crazy.video.domain.Action;
import jk.kamoru.crazy.video.domain.Actress;
import jk.kamoru.crazy.video.domain.ActressSort;
import jk.kamoru.crazy.video.domain.History;
import jk.kamoru.crazy.video.domain.Sort;
import jk.kamoru.crazy.video.domain.StudioSort;
import jk.kamoru.crazy.video.domain.Video;
import jk.kamoru.crazy.video.domain.VideoSearch;
import jk.kamoru.crazy.video.domain.View;
import jk.kamoru.crazy.video.service.HistoryService;
import jk.kamoru.crazy.video.service.VideoService;
import jk.kamoru.crazy.video.util.VideoUtils;
import jk.kamoru.util.FileUtils;
import jk.kamoru.util.StringUtils;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Video controller<br>
 * @author kamoru
 */
@Controller
@RequestMapping("/video")
public class VideoController extends AbstractController {

	protected static final Logger logger = LoggerFactory.getLogger(VideoController.class);
	
	@Autowired private ImageService imageService;
	@Autowired private VideoService videoService;
	@Autowired private HistoryService historyService;
	@Autowired private VideoBatch videoBatch;

	/**minimum rank model attrubute by named 'minRank'
	 * @return model attribute
	 */
	@JsonIgnore
	@ModelAttribute("minRank")
	public Integer minRank() {
		return videoService.minRank();
	}

	/**maximum rank model attrubute by named 'maxRank'
	 * @return model attribute
	 */
	@JsonIgnore
	@ModelAttribute("maxRank")
	public Integer maxRank() {
		return videoService.maxRank();
	}

	/**display actress list view
	 * @param model
	 * @param sort default NAME if {@code null}
	 * @return view name
	 */
	@RequestMapping(value="/actress", method=RequestMethod.GET)
	public String actressList(Model model, @RequestParam(value="sort", required=false, defaultValue="NAME") ActressSort sort,
			@RequestParam(value="r", required=false, defaultValue="false") Boolean reverse) {
		logger.trace("actressList sort={} reverse={}", sort, reverse);
		model.addAttribute(videoService.getActressList(sort, reverse));
		model.addAttribute("sorts", ActressSort.values());
		model.addAttribute("sort", sort);
		model.addAttribute("reverse", reverse);
		return "video/actressList";
	}

	/**display a actress detail view
	 * @param model
	 * @param actressName actress name
	 * @return view name
	 */
	@RequestMapping(value="/actress/{actressName}", method=RequestMethod.GET)
	public String actressDetail(Model model, @PathVariable String actressName) {
		logger.trace(actressName);
		model.addAttribute(videoService.getActress(actressName));
		return "video/actressDetail";
	}

	/**save actres info
	 * @param actressName
	 * @param params map of info
	 */
	@RequestMapping(value="/actress/{actressName}", method=RequestMethod.POST)
//	@ResponseStatus(HttpStatus.NO_CONTENT)
	public String saveActressInfo(@PathVariable String actressName, @RequestParam Map<String, String> params) {
		logger.trace("{}", params);
		String oriname = params.get("name");
		String newname = params.get("newname");
		if (!StringUtils.equals(oriname, newname)) {
			videoService.renameOfActress(oriname, newname);
		}
		videoService.saveActressInfo(newname, params);
		return "redirect:/video/actress/" + newname;
	}

	@RequestMapping(value="/actress/{actressName}/favorite/{favorite}", method=RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void putActressFavorite(@PathVariable String actressName, @PathVariable Boolean favorite) {
		logger.trace("{}, {}", actressName, favorite);
		
		Actress actress = videoService.getActress(actressName);
		Map<String, String> params = actress.getInfoMap();
		params.put(Actress.FAVORITE, favorite.toString());
		videoService.saveActressInfo(actressName, params);
	}
	
	/**display status briefing view
	 * @param model
	 * @return view name
	 */
	@RequestMapping(value="/briefing", method=RequestMethod.GET)
	public String briefing(Model model) {
		logger.trace("briefing");

		videoService.reload();
		model.addAttribute("pathMap", 		videoService.groupByPath());
		model.addAttribute("dateMap", 		videoService.groupByDate());
		model.addAttribute("rankMap", 		videoService.groupByRank());
		model.addAttribute("playMap", 		videoService.groupByPlay());
		model.addAttribute("scoreMap", 		videoService.groupByScore());
		model.addAttribute("lengthMap", 	videoService.groupByLength());
		model.addAttribute("extensionMap", 	videoService.groupByExtension());
		
		model.addAttribute(videoService.getStudioList());
		model.addAttribute(videoService.getActressList());
		model.addAttribute(videoService.getVideoList());
		
		model.addAttribute("MOVE_WATCHED_VIDEO", 		videoBatch.isMOVE_WATCHED_VIDEO());
		model.addAttribute("DELETE_LOWER_RANK_VIDEO", 	videoBatch.isDELETE_LOWER_RANK_VIDEO());
		model.addAttribute("DELETE_LOWER_SCORE_VIDEO", 	videoBatch.isDELETE_LOWER_SCORE_VIDEO());
		
		return "video/briefing";
	}

	/**send history list by query
	 * @param model
	 * @param query search keyword
	 * @return view name
	 */
	@RequestMapping(value="/history", method=RequestMethod.GET)
	public String history(Model model, @RequestParam(value="q", required=false, defaultValue="") String query) {
		logger.trace("query={}", query);
		model.addAttribute("historyList", videoService.findHistory(query));
		return "video/history";
	}

	/**display video list view
	 * @param model
	 * @param sort default Opus if {@code null}
	 * @return view name
	 */
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public String videoList(Model model, @RequestParam(value="sort", required=false, defaultValue="O") Sort sort,
			@RequestParam(value="r", required=false, defaultValue="false") Boolean reverse) {
		logger.trace("videoList sort={} reverse={}", sort, reverse);
		model.addAttribute("videoList", videoService.getVideoList(sort, reverse));
		model.addAttribute("sorts", Sort.values());
		model.addAttribute("sort", sort);
		model.addAttribute("reverse", reverse);
		return "video/videoList";
	}

	/**display video torrent info view
	 * @param model
	 * @return view name
	 */
	@RequestMapping(value="/torrent", method=RequestMethod.GET)
	public String torrent(Model model) {
		logger.trace("torrent");
		model.addAttribute("videoList", videoService.torrent());
		return "video/torrent";
	}
	
	/**display video search view by query
	 * @param model
	 * @param query
	 * @return view name
	 */
	@RequestMapping(value="/search", method=RequestMethod.GET)
	public String search(Model model, @RequestParam(value="q", required=false, defaultValue="") String query) {
		logger.trace("query={}", query);
		model.addAttribute("videoList", videoService.findVideoList(query));
		model.addAttribute("historyList", videoService.findHistory(query));

        return "video/search";		
	}

	/**display studio detail view
	 * @param model
	 * @param studio
	 * @return view name
	 */
	@RequestMapping(value="/studio/{studio}", method=RequestMethod.GET)
	public String studioDetail(Model model, @PathVariable String studio) {
		logger.trace(studio);
		model.addAttribute(videoService.getStudio(studio));
		return "video/studioDetail";
	}

	/**save studio info
	 * @param studio
	 * @param params map of info
	 */
	@RequestMapping(value="/studio/{studio}", method=RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void putStudioInfo(@PathVariable String studio, @RequestParam Map<String, String> params) {
		logger.trace("{}", params);
		videoService.saveStudioInfo(studio, params);
	}

	/**display studio list view
	 * @param model
	 * @param sort default NAME
	 * @return view name
	 */
	@RequestMapping(value="/studio", method=RequestMethod.GET)
	public String studioList(Model model, @RequestParam(value="sort", required=false, defaultValue="NAME") StudioSort sort,
			@RequestParam(value="r", required=false, defaultValue="false") Boolean reverse) {
		logger.info("studioList sort={} reverse={}", sort, reverse);
		model.addAttribute(videoService.getStudioList(sort, reverse));
		model.addAttribute("sorts", StudioSort.values());
		model.addAttribute("sort", sort);
		model.addAttribute("reverse", reverse);
		return "video/studioList";
	}

	/**send video cover image<br>
	 * send redirect '/no/cover' if image not found
	 * @param opus
	 * @return image entity
	 * @throws IOException
	 */
	@RequestMapping(value="/{opus}/cover", method=RequestMethod.GET)
	public HttpEntity<byte[]> videoCover(@PathVariable String opus) throws IOException {
		logger.trace("{}", opus);
		File imageFile = videoService.getVideoCoverFile(opus);
		if(imageFile == null)
			return null;
		return httpEntity(videoService.getVideoCoverByteArray(opus), FileUtils.getExtension(imageFile));
	}
	
	/**display video overview view
	 * @param model
	 * @param opus
	 * @return view name
	 */
	@RequestMapping(value="/{opus}/overview", method=RequestMethod.GET)
	public String videoOverview(Model model, @PathVariable("opus") String opus) {
		logger.trace(opus);
		model.addAttribute("video", videoService.getVideo(opus));
		return "video/videoOverview";
	}

	/**save video overview
	 * @param model
	 * @param opus
	 * @param overview
	 */
	@RequestMapping(value="/{opus}/overview", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void opusOverviewPost(Model model, @PathVariable("opus") String opus, @RequestParam("overViewTxt") String overview) {
		logger.trace("{} - {}", opus, overview);
		videoService.saveVideoOverview(opus, overview);
	}

	/**call video player
	 * @param model
	 * @param opus
	 */
	@RequestMapping(value="/{opus}/play", method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void callVideoPlayer(Model model, @PathVariable String opus) {
		logger.trace(opus);
		videoService.playVideo(opus);
	}

	/**save video rank info
	 * @param model
	 * @param opus
	 * @param rank
	 */
	@RequestMapping(value="/{opus}/rank/{rank}", method=RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void putVideoRank(Model model, @PathVariable String opus, @PathVariable int rank) {
		logger.trace("{} : {}", opus, rank);
		videoService.rankVideo(opus, rank);
	}

	/**call Subtitles editor
	 * @param model
	 * @param opus
	 */
	@RequestMapping(value="/{opus}/subtitles", method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void opusSubtitles(Model model, @PathVariable String opus) {
		logger.trace(opus);
		videoService.editVideoSubtitles(opus);
	}

	/**remove video
	 * @param model
	 * @param opus
	 */
	@RequestMapping(value="/{opus}", method=RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeVideo(Model model, @PathVariable("opus") String opus) {
		logger.trace(opus);
		videoService.removeVideo(opus);
	}

	/**display video detail view
	 * @param model
	 * @param opus
	 * @return view name
	 */
	@RequestMapping(value="/{opus}", method=RequestMethod.GET)
	public String videoDetail(Model model, @PathVariable String opus) {
		logger.trace(opus);
		Video video = videoService.getVideo(opus);
		model.addAttribute(video);
		if (video.isArchive())
			return "video/videoDetailArchive";
		else
			return "video/videoDetail";
	}
	
	/**rank, play 초기화
	 * @param model
	 * @param opus
	 * @return
	 */
	@RequestMapping(value="/{opus}/reset", method=RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void videoReset(@PathVariable String opus) {
		logger.trace(opus);
		videoService.resetVideoScore(opus);
	}

	/**잘못 매칭된 비디오. 밖으로 옮기고, info초기화
	 * @param model
	 * @param opus
	 * @return
	 */
	@RequestMapping(value="/{opus}/wrong", method=RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void videoWrong(@PathVariable String opus) {
		logger.trace(opus);
		videoService.resetWrongVideo(opus);
	}

	/**
	 * Test code. 
	 */
	@RequestMapping(value="/{opus}", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void opusPost() {
		logger.warn("POST do not something yet");
		throw new CrazyException(new IllegalStateException("POST do not something yet"));
	}
	
	/**display video main view
	 * @param model
	 * @param videoSearch
	 * @return view name
	 */
	@RequestMapping
	public String videoMain(Model model, @ModelAttribute VideoSearch videoSearch) {
		logger.trace("{}", videoSearch);
		List<Video> videoList =  videoService.searchVideo(videoSearch);

		// 1건만 검색될 경우 slide view가 보이지 않는 문제가 있어, view를 large로 변경
		if (videoList.size() == 1)
			videoSearch.setListViewType(View.L);
		
		model.addAttribute("views", 		View.values());
		model.addAttribute("sorts", 		Sort.values());
//		model.addAttribute("rankSign", 		InequalitySign.values());
		model.addAttribute("rankRange", 	videoService.getRankRange());
		model.addAttribute("playRange", 	videoService.getPlayRange());
		model.addAttribute("videoList", 	videoList);
		model.addAttribute("opusArray", 	VideoUtils.getOpusArrayStyleStringWithVideofile(videoList));
		model.addAttribute("actressList", 	videoService.getActressListInVideoList(videoList));
		model.addAttribute("studioList", 	videoService.getStudioListInVideoList(videoList));
//		model.addAttribute("actressList", 	videoService.getActressList());
//		model.addAttribute("studioList", 	videoService.getStudioList());
		model.addAttribute("bgImageCount", 	imageService.getImageSourceSize());
		return "video/videoMain";
	}

	@RequestMapping("/archive")
	public String videoArchive(Model model, @ModelAttribute VideoSearch videoSearch) {
		logger.trace("{}", videoSearch);
		List<Video> videoList =  videoService.searchVideoInArchive(videoSearch);

		model.addAttribute("views", 		View.values());
		model.addAttribute("sorts", 		Sort.values());
		model.addAttribute("videoList", 	videoList);
//		model.addAttribute("actressList", 	videoService.getActressListInVideoList(videoList));
//		model.addAttribute("studioList", 	videoService.getStudioListInVideoList(videoList));
		model.addAttribute("bgImageCount", 	imageService.getImageSourceSize());
		return "video/videoArchive";
	}
	
	/**reload video source
	 * @param model
	 */
	@RequestMapping("/reload")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reload(Model model) {
		logger.trace("reload");
		videoService.reload();
	}

	/**returns image entity<br>
	 * cache time {@link VIDEO#WEBCACHETIME_SEC}, {@link VIDEO#WEBCACHETIME_MILI}
	 * @param imageBytes
	 * @param suffix
	 * @return image entity
	 */
	private HttpEntity<byte[]> httpEntity(byte[] imageBytes, String suffix) {
		long today = new Date().getTime();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("max-age=" + VIDEO.WEBCACHETIME_SEC);
		headers.setContentLength(imageBytes.length);
		headers.setContentType(MediaType.parseMediaType("image/" + suffix));
		headers.setDate(		today + VIDEO.WEBCACHETIME_MILI);
		headers.setExpires(		today + VIDEO.WEBCACHETIME_MILI);
		headers.setLastModified(today - VIDEO.WEBCACHETIME_MILI);
		
		return new HttpEntity<byte[]>(imageBytes, headers);
	}

	/**display video manager view
	 * @return view name
	 */
	@RequestMapping("/manager")
	public String manager() {
		logger.trace("video manager");
		return "video/manager";
	}

	/**move watched video
	 * @param model
	 */
	@RequestMapping(value="/manager/moveWatchedVideo", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void moveWatchedVideo(Model model) {
		synchronized (java.lang.Object.class) {
			logger.trace("move watched video");
			videoService.moveWatchedVideo();
			videoService.reload();
		}
	}
	
	/**remove lower rank video
	 * @param model
	 */
	@RequestMapping(value="/manager/removeLowerRankVideo", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeLowerRankVideo(Model model) {
		logger.trace("delete lower rank video");
		videoService.removeLowerRankVideo();
	}
	
	/**remove lower score video
	 * @param model
	 */
	@RequestMapping(value="/manager/removeLowerScoreVideo", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeLowerScoreVideo(Model model) {
		logger.trace("delete lower score video");
		videoService.removeLowerScoreVideo();
	}
	
	/**confirm video candidate
	 * @param model
	 * @param opus
	 * @param path
	 */
	@RequestMapping(value="/{opus}/confirmCandidate", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void confirmCandidate(Model model, @PathVariable("opus") String opus, @RequestParam("path") String path) {
		logger.trace("confirm candidate video file");
		videoService.confirmCandidate(opus, path);
	}
	
	/**rename video title
	 * @param opus
	 * @param newName
	 */
	@RequestMapping(value="/{opus}/rename", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void rename(@PathVariable("opus") String opus, @RequestParam("newname") String newName) {
		videoService.rename(opus, newName);
	}
	
	/**
	 * play count reset in history
	 */
	@RequestMapping("/transferPlayCountInfo")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void transferPlayCountInfo() {
		for (Video video : videoService.getVideoList()) {
			int playCount = 0;
			List<History> histories = historyService.findByVideo(video);
			for (History history : histories) {
				if (history.getAction() == Action.PLAY)
					playCount++;
			}
			video.setPlayCount(playCount);
		}
	}
	
	/**rename actress
	 * @param oriname
	 * @param newname
	 * @deprecated {@link #putActressInfo(String, Map)}으로 통합
	 */
	@RequestMapping(value="/actress/{oriname}/renameTo/{newname}", method=RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void actressRename(@PathVariable("oriname") String oriname, @PathVariable("newname") String newname) {
		if (StringUtils.isBlank(oriname) || StringUtils.isBlank(newname)) {
			logger.warn("name is empty. [{}] to [{}]", oriname, newname);
			return;
		}
		logger.info("rename to [{}] from [{}]", newname, oriname);
		videoService.renameOfActress(oriname, newname);
	}

	/**rename studio name
	 * @param oriname
	 * @param newname
	 */
	@RequestMapping(value="/studio/{oriname}/renameTo/{newname}", method=RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void studioRename(@PathVariable("oriname") String oriname, @PathVariable("newname") String newname) {
		if (StringUtils.isBlank(oriname) || StringUtils.isBlank(newname)) {
			logger.warn("name is empty. [{}] to [{}]", oriname, newname);
			return;
		}
		logger.info("rename to [{}] from [{}]", newname, oriname);
		videoService.renameOfStudio(oriname, newname);
	}

	/**parsing title
	 * @param model
	 * @param titleData
	 * @return
	 */
	@RequestMapping("/parseToTitle")
	public String parseToTitle(Model model, @RequestParam(value="titleData", required=false, defaultValue="") String titleData) {
		logger.trace("parse to title");
		model.addAttribute("titleList", videoService.parseToTitleData(titleData));
		model.addAttribute("titleData", titleData);
		return "video/parseToTitle";
	}
	
	/**parsing title
	 * @param model
	 * @param titleData
	 * @return
	 */
	@RequestMapping("/parseToTitle2")
	public String parseToTitle2(Model model, @RequestParam(value="titleData", required=false, defaultValue="") String titleData) {
		logger.trace("parse to title");
		model.addAttribute("titleList", videoService.parseToTitleData2(titleData));
		model.addAttribute("titleData", titleData);
		return "video/parseToTitle2";
	}
	
	/**search torrent
	 * @param model
	 * @param opus
	 * @return
	 */
	@RequestMapping("/torrent/search/{opus}")
	public String torrentSearch(Model model, @PathVariable("opus") String opus) {
		logger.trace("torrentSearch");
		model.addAttribute(videoService.getVideo(opus));
		return "video/torrentSearch";
	}

	@RequestMapping(value="/set/MOVE_WATCHED_VIDEO/{setValue}", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void setMOVE_WATCHED_VIDEO(@PathVariable("setValue") boolean setValue) {
		videoBatch.setMOVE_WATCHED_VIDEO(setValue);
	}
	@RequestMapping(value="/set/DELETE_LOWER_RANK_VIDEO/{setValue}", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void setDELETE_LOWER_RANK_VIDEO(@PathVariable("setValue") boolean setValue) {
		videoBatch.setDELETE_LOWER_RANK_VIDEO(setValue);
	}
	@RequestMapping(value="/set/DELETE_LOWER_SCORE_VIDEO/{setValue}", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void setDELETE_LOWER_SCORE_VIDEO(@PathVariable("setValue") boolean setValue) {
		videoBatch.setDELETE_LOWER_SCORE_VIDEO(setValue);
	}

}
