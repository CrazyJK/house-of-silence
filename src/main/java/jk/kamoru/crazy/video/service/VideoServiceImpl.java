package jk.kamoru.crazy.video.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import jk.kamoru.crazy.CRAZY;
import jk.kamoru.crazy.CrazyProperties;
import jk.kamoru.crazy.video.VIDEO;
import jk.kamoru.crazy.video.VideoException;
import jk.kamoru.crazy.video.dao.VideoDao;
import jk.kamoru.crazy.video.domain.Action;
import jk.kamoru.crazy.video.domain.Actress;
import jk.kamoru.crazy.video.domain.ActressSort;
import jk.kamoru.crazy.video.domain.History;
import jk.kamoru.crazy.video.domain.Sort;
import jk.kamoru.crazy.video.domain.Studio;
import jk.kamoru.crazy.video.domain.StudioSort;
import jk.kamoru.crazy.video.domain.TitlePart;
import jk.kamoru.crazy.video.domain.Video;
import jk.kamoru.crazy.video.domain.VideoSearch;
import jk.kamoru.crazy.video.util.VideoUtils;
import jk.kamoru.util.ArrayUtils;
import jk.kamoru.util.FileUtils;
import jk.kamoru.util.JKUtilException;
import jk.kamoru.util.RuntimeUtils;
import jk.kamoru.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * video service implement class
 * @author kamoru
 */
@Service
@Slf4j
public class VideoServiceImpl extends CrazyProperties implements VideoService {


	/** minimum free space of disk */
	private final long MIN_FREE_SPAC = 10 * FileUtils.ONE_GB;
	/** sleep time of moving video */
	private final long SLEEP_TIME = 5 * 1000;
	
	/** video dao */
	@Autowired VideoDao videoDao;
	@Autowired HistoryService historyService;

	@Override
	public void removeVideo(String opus) {
		log.trace(opus);
		videoDao.removeVideo(opus);
		saveHistory(getVideo(opus), Action.REMOVE);
	}

	@Override
	public void editVideoSubtitles(String opus) {
		log.trace(opus);
		callExecutiveCommand(getVideo(opus), Action.SUBTITLES);
	}

	/**call executive command by action. asynchronous
	 * @param video
	 * @param action PLAY, SUBTITLES in {@link Action}
	 */
	@Async
	private void callExecutiveCommand(Video video, Action action) {
		log.trace("{} : {}", video.getOpus(), action);
		String command = null;
		String[] argumentsArray = null;
		switch(action) {
			case PLAY:
				command = PLAYER;
				argumentsArray = video.getVideoFileListPathArray();
				break;
			case SUBTITLES:
				command = EDITOR;
				argumentsArray = video.getSubtitlesFileListPathArray();
				break;
			default:
				throw new VideoException(video, "Unknown Action");
		}
		if(argumentsArray == null)
			throw new VideoException(video, "No arguments for " + action);
		
		RuntimeUtils.exec(ArrayUtils.addAll(new String[]{command}, argumentsArray));
	}

	@Override
	public List<Map<String, String>> findHistory(String query) {
		log.trace(query);
		
		List<Map<String, String>> foundMapList = new ArrayList<Map<String, String>>();
		List<History> list = null;
		if (StringUtils.equals("ALL", query)) 
			list = historyService.getDeduplicatedList();
		else 
			list = historyService.findByQuery(query);
		for (History history : list) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("date", new SimpleDateFormat(VIDEO.DATE_TIME_PATTERN).format(history.getDate()));
			map.put("opus", history.getOpus());
			map.put("act",  history.getAction().toString());
			map.put("desc", history.getVideo() == null ? history.getDesc() : history.getVideo().getFullname());
			foundMapList.add(map);
		}
		
		log.debug("q={} foundLength={}", query, foundMapList.size());
		if (foundMapList.size() > 1) {
			Collections.sort(foundMapList, new Comparator<Map<String, String>>(){
	
				@Override
				public int compare(Map<String, String> o1, Map<String, String> o2) {
					String thisStr = o1.get("date");
					String compStr = o2.get("date");
	
					String[] s = {thisStr, compStr};
					Arrays.sort(s);
					return s[0].equals(thisStr) ? 1 : -1;
				}
				
			});
		}
		return foundMapList;
	}

	@Override
	public List<Map<String, String>> findVideoList(String query) {
		log.trace(query);
		List<Map<String, String>> foundMapList = new ArrayList<Map<String, String>>();
		if(query == null || query.trim().length() == 0)
			return foundMapList;

		query = query.toLowerCase();
		for(Video video : videoDao.getVideoList()) {
			if(StringUtils.containsIgnoreCase(video.getOpus(), query)
					|| StringUtils.containsIgnoreCase(video.getStudio().getName(), query)
					|| StringUtils.containsIgnoreCase(video.getTitle(), query)
					|| StringUtils.containsIgnoreCase(video.getActressName(), query)) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("opus", video.getOpus());
				map.put("title", video.getTitle());
				map.put("studio", video.getStudio().getName());
				map.put("actress", video.getActressName());
				map.put("existVideo", String.valueOf(video.isExistVideoFileList()));
				map.put("existCover", String.valueOf(video.isExistCoverFile()));
				map.put("existSubtitles", String.valueOf(video.isExistSubtitlesFileList()));
				foundMapList.add(map);
			} 
		}
		log.debug("q={} foundLength={}", query, foundMapList.size());
		Collections.sort(foundMapList, new Comparator<Map<String, String>>() {

			@Override
			public int compare(Map<String, String> o1, Map<String, String> o2) {
				String thisStr = o1.get("opus");
				String compStr = o2.get("opus");

				String[] s = {thisStr, compStr};
				Arrays.sort(s);
				return s[0].equals(thisStr) ? 1 : -1;
			}});
		return foundMapList;
	}

	@Override
	public Actress getActress(String actressName) {
		log.trace(actressName);
		return videoDao.getActress(actressName);
	}

	@Override
	public List<Actress> getActressList() {
		return getActressList(ActressSort.NAME, false);
	}

	@Override
	public List<Actress> getActressListInVideoList(List<Video> videoList) {
		log.trace("size : {}", videoList.size());
		Map<String, Actress> actressMap = new TreeMap<String, Actress>();
		for(Video video : videoList) {
			for (Actress actress : video.getActressList()) {
				Actress actressInMap = actressMap.get(actress.getName());
				if (actressInMap == null) {
					actressInMap = videoDao.getActress(actress.getName());
					actressInMap.emptyVideo();
				}
				actressInMap.addVideo(video);
				actressMap.put(actress.getName(), actressInMap);
			}
		}
		List<Actress> list = new ArrayList<Actress>(actressMap.values());
		Collections.sort(list);
		log.debug("found studio list size {}", list.size());
		return list;
	}

	@Override
	public Studio getStudio(String studioName) {
		log.trace(studioName);
		return videoDao.getStudio(studioName);
	}

	@Override
	public List<Studio> getStudioList() {
		log.trace("getStudioList");
		List<Studio> list = videoDao.getStudioList(); 
		Collections.sort(list);
		return list;
	}
	
	@Override
	public List<Studio> getStudioListInVideoList(List<Video> videoList) {
		log.trace("size : {}", videoList.size());
		Map<String, Studio> studioMap = new TreeMap<String, Studio>();
		for(Video video : videoList) {
			String studioName = video.getStudio().getName();
			Studio studio = studioMap.get(studioName);
			if (studio == null) {
				studio = videoDao.getStudio(studioName);
				studio.emptyVideo();
			}
			studio.addVideo(video);
			studioMap.put(studioName, studio);
		}
		List<Studio> list = new ArrayList<Studio>(studioMap.values());
		Collections.sort(list);
		log.debug("found studio list size {}", list.size());
		return list;
	}

	@Override
	public Video getVideo(String opus) {
		log.trace(opus);
		return videoDao.getVideo(opus);
	}

	@Override
	public byte[] getVideoCoverByteArray(String opus) {
		log.trace(opus);
		return videoDao.getVideo(opus).getCoverByteArray();
	}

	@Override
	public File getVideoCoverFile(String opus) {
		log.trace(opus);
		return videoDao.getVideo(opus).getCoverFile();
	}

	@Override
	public List<Video> getVideoList() {
		log.trace("getVideoList");
		List<Video> list = videoDao.getVideoList(); 
		Collections.sort(list);
		return list;
	}

	@Override
	public void playVideo(String opus) {
		log.trace(opus);
		Video video = videoDao.getVideo(opus);
		if (!video.isExistVideoFileList())
			throw new VideoException(video, "No video file");
		callExecutiveCommand(video, Action.PLAY);
		video.increasePlayCount();
		saveHistory(video, Action.PLAY);
	}

	@Override
	public void rankVideo(String opus, int rank) {
		log.trace("opus={} : rank={}", opus, rank);
		videoDao.getVideo(opus).setRank(rank);
	}

	/**save history by action
	 * @param video
	 * @param action PLAY, OVERVIEW, COVER, SUBTITLES, DELETE in {@link Action}
	 */
	private void saveHistory(Video video, Action action) {
		log.trace("opus={} : action={}", video.getOpus(), action);

		History history = new History(video, action);
		log.debug("save history - {}", history);
		historyService.persist(history);
	}

	@Override
	public void saveVideoOverview(String opus, String overViewText) {
		log.trace("opus={} : text={}", opus, overViewText);
		videoDao.getVideo(opus).saveOverView(overViewText);
	}

	@Override
	public List<Video> searchVideo(VideoSearch search) {
		log.trace("{}", search);
		if (search.getRankRange() == null)
			search.setRankRange(getRankRange());
		
		List<Video> foundList = new ArrayList<Video>();
		for (Video video : videoDao.getVideoList()) {
			if ((VideoUtils.equals(video.getStudio().getName(), search.getSearchText()) 
					|| VideoUtils.equals(video.getOpus(), search.getSearchText()) 
					|| VideoUtils.containsName(video.getTitle(), search.getSearchText()) 
					|| VideoUtils.containsActress(video, search.getSearchText())
					|| VideoUtils.containsName(video.getReleaseDate(), search.getSearchText())) 
				&& (search.isAddCond()   
						? ((search.isExistVideo() ? video.isExistVideoFileList() : !video.isExistVideoFileList())
							&& (search.isExistSubtitles() ? video.isExistSubtitlesFileList() : !video.isExistSubtitlesFileList())) 
						: true)
				&& (search.getSelectedStudio() == null ? true : search.getSelectedStudio().contains(video.getStudio().getName()))
				&& (search.getSelectedActress() == null ? true : VideoUtils.containsActress(video, search.getSelectedActress()))
				&& (rankMatch(video.getRank(), search.getRankRange()))
				&& (playCountMatch(video.getPlayCount(), search.getPlayCount()))
				) 
			{
				video.setSortMethod(search.getSortMethod());
				foundList.add(video);
			}
		}
		if (search.isSortReverse())
			Collections.sort(foundList, Collections.reverseOrder());
		else
			Collections.sort(foundList);

		log.debug("found video list size {}", foundList.size());
		return foundList;
	}
	
	/**compare play count. {@code true} if playCount2 is {@code null} or {@code -1}
	 * @param playCount
	 * @param playCount2
	 * @return {@code true} if same of both or playCount2 {@code null}, {@code -1}
	 */
	private boolean playCountMatch(Integer playCount, Integer playCount2) {
		if (playCount2 == null || playCount2 == -1)
			return true;
		else 
			return playCount == playCount2;
	}

	/**Returns {@code true} if rankRange list contains the specified rank
	 * @param rank
	 * @param rankRange rank range list
	 * @return {@code true} if rankRange list contains the specified rank
	 */
	private boolean rankMatch(int rank, List<Integer> rankRange) {
		return rankRange.contains(rank);
	}

	@Override
	public Map<String, Long[]> groupByPath() {
		log.trace("groupByPath");
		Map<String, Long[]> pathMap = new TreeMap<String, Long[]>();
		Long[] total = new Long[]{0l, 0l};
		for (Video video : videoDao.getVideoList()) {
			String path = video.getDelegatePath();
			if (path.contains(STORAGE_PATHS[0]))
				path = STORAGE_PATHS[0];
			long length = video.getLength();
			Long[] data = pathMap.get(path);
			if (data == null) {
				data = new Long[]{0l, 0l};
			}
			total[0] += 1;
			total[1] += length;
			data[0] += 1;
			data[1] += length; 
			pathMap.put(path, data);
		}
		pathMap.put("Total", total);
		return pathMap;
	}

	@Override
	public void saveActressInfo(String name, Map<String, String> params) {
		log.trace("name={}, params={}", name, params);
		FileUtils.saveFileFromMap(new File(getInfoDir(), name + FileUtils.EXTENSION_SEPARATOR + VIDEO.EXT_ACTRESS), params);
		videoDao.getActress(name).reloadInfo();
	}

	@Override
	public Map<String, List<Video>> groupByDate() {
		log.trace("groupByDate");
		Map<String, List<Video>> map = new TreeMap<String, List<Video>>();
		for (Video video : videoDao.getVideoList()) {
			String yyyyMM = StringUtils.substringBeforeLast(video.getVideoDate(), "-");
			if (map.containsKey(yyyyMM)) {
				map.get(yyyyMM).add(video);
			}
			else {
				List<Video> videoList = new ArrayList<Video>();
				videoList.add(video);
				map.put(yyyyMM, videoList);
			}
		}
		log.debug("video group by date - {}", map);
		return map;
	}

	@Override
	public Map<Integer, List<Video>> groupByRank() {
		log.trace("groupByRank");
		Map<Integer, List<Video>> map = new TreeMap<Integer, List<Video>>(Collections.reverseOrder());
		for (Video video : videoDao.getVideoList()) {
			Integer rank = video.getRank();
			if (map.containsKey(rank)) {
				map.get(rank).add(video);
			}
			else {
				List<Video> videoList = new ArrayList<Video>();
				videoList.add(video);
				map.put(rank, videoList);
			}
		}
		log.debug("video group by rank - {}", map);
		return map;
	}

	@Override
	public Map<Integer, List<Video>> groupByPlay() {
		log.trace("groupByPlay");
		Map<Integer, List<Video>> map = new TreeMap<Integer, List<Video>>(Collections.reverseOrder());
		for (Video video : videoDao.getVideoList()) {
			Integer play = video.getPlayCount();
			if (map.containsKey(play)) {
				map.get(play).add(video);
			}
			else {
				List<Video> videoList = new ArrayList<Video>();
				videoList.add(video);
				map.put(play, videoList);
			}
		}
		log.debug("video group by play - {}", map);
		return map;
	}

	@Override
	public void moveVideo(String opus, String path) {
		log.info("{} move to {}", opus, path);
		videoDao.moveVideo(opus, path);
	}

	@Override
	public void reload() {
		log.trace("reload");
		videoDao.reload();
	}

	@Override
	public void saveStudioInfo(String studio, Map<String, String> params) {
		log.trace("name={}, params={}", studio, params);
		FileUtils.saveFileFromMap(new File(getInfoDir(), studio + FileUtils.EXTENSION_SEPARATOR + VIDEO.EXT_STUDIO), params);
		videoDao.getStudio(studio).reloadInfo();
	}
	
	@Override
	public List<Actress> getActressList(ActressSort sort, boolean reverse) {
		log.trace("sort={} reverse={}", sort, reverse);
		List<Actress> list = videoDao.getActressList();
		for (Actress actress : list)
			actress.setSort(sort);
		if (reverse)
			Collections.sort(list, Collections.reverseOrder());
		else
			Collections.sort(list);
		return list;
	}

	@Override
	public List<Studio> getStudioList(StudioSort sort, boolean reverse) {
		log.trace("sort={} reverse={}", sort, reverse);
		List<Studio> list = videoDao.getStudioList();
		for (Studio studio : list)
			studio.setSort(sort);
		if (reverse)
			Collections.sort(list, Collections.reverseOrder());
		else
			Collections.sort(list);
		return list;
	}

	@Override
	public List<Video> getVideoList(Sort sort, boolean reverse) {
		log.trace("sort={} reverse={}", sort, reverse);
		List<Video> list = videoDao.getVideoList();
		for (Video video : list) 
			video.setSortMethod(sort);
		if (reverse)
			Collections.sort(list, Collections.reverseOrder());
		else
			Collections.sort(list);
		return list;
	}

	@Override
	public List<Integer> getPlayRange() {
		int maxPlayCount = 0;
		for (Video video : videoDao.getVideoList())
			maxPlayCount = maxPlayCount - video.getPlayCount() > 0 ? maxPlayCount : video.getPlayCount();

		List<Integer> playList = new ArrayList<Integer>();
		for (int i=-1; i<=maxPlayCount; i++)
			playList.add(i);
		return playList;
	}

	@Override
	public Integer minRank() {
		return MIN_RANK;
	}

	@Override
	public Integer maxRank() {
		return MAX_RANK;
	}

	@Override
	public List<Integer> getRankRange() {
		List<Integer> rankList = new ArrayList<Integer>();
		for (Integer i=MIN_RANK; i<=MAX_RANK; i++)
			rankList.add(i);
		return rankList;
	}

	@Override
	public void removeLowerRankVideo() {
		for (Video video : videoDao.getVideoList()) {
			if (video.getRank() < BASE_RANK) {
				log.info("remove lower rank video {} : {} : {}", video.getOpus(), video.getRank(), video.getTitle());
				saveHistory(video, Action.REMOVE);
				videoDao.removeVideo(video.getOpus());
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see jk.kamoru.app.video.service.VideoService#removeLowerScoreVideo()
	 * 종합 순위<br>
	 * 점수 배정<br>
	 * 	- rank 			: rankRatio<br>
	 * 	- play count	: playRatio<br>
	 * 	- actress video	: actressRatio	<br>	
	 *  - subtitles     : subtitlesRatio
	 */
	@Override
	public void removeLowerScoreVideo() {
		long maximumSizeOfEntireVideo = MAX_ENTIRE_VIDEO * FileUtils.ONE_GB;
		long sumSizeOfTotalVideo  = 0l;
		long sumSizeOfDeleteVideo = 0l;
		int  countOfTotalVideo    = 0;
		int  countOfDeleteVideo   = 0;
		int  minAliveScore 		  = 0;
		
		List<Video> list = getVideoListSortByScore();
		
		for (Video video : list) {
			if (video.getPlayCount() == 0)
				continue;
			
			int score = video.getScore();
			sumSizeOfTotalVideo += video.getLength();
			countOfTotalVideo++;
			
			if (sumSizeOfTotalVideo > maximumSizeOfEntireVideo) {
				sumSizeOfDeleteVideo += video.getLength();
				countOfDeleteVideo++;
				
				log.info("    {}/{}. Score[{}] - {} {}",
						countOfDeleteVideo,
						countOfTotalVideo,
						score, 
						video.getFullname(),
						video.getScoreDesc());
				saveHistory(video, Action.REMOVE);
				videoDao.removeVideo(video.getOpus());
			}
			else {
				minAliveScore = score;
			}
		}
		if (countOfDeleteVideo > 0)
			log.info("    Total deleted {} video, {} GB", countOfDeleteVideo, sumSizeOfDeleteVideo / FileUtils.ONE_GB);
		log.info("    Current minimum score is {} ", minAliveScore);
	}
	
	/**
	 * Score로 정렬된 비디오 목록
	 * @return
	 */
	private List<Video> getVideoListSortByScore() {
		List<Video> list = videoDao.getVideoList();
		Collections.sort(list, new Comparator<Video>(){
			@Override
			public int compare(Video o1, Video o2) {
				return o2.getScore() == o1.getScore() 
						? StringUtils.compareTo(o2.getReleaseDate(), o1.getReleaseDate()) 
								: o2.getScore() - o1.getScore();
			}});
		return list;
	}
	
	@Override
	public void deleteGarbageFile() {
		for (Video video : videoDao.getVideoList()) {
			if (!video.isExistVideoFileList() 
					&& !video.isExistCoverFile()
//					&& !video.isExistCoverWebpFile() 
					&& !video.isExistSubtitlesFileList()) {
				log.info("    delete garbage file - {}", video);
				videoDao.deleteVideo(video.getOpus());
			}
		}
	}
	
	@Override
	
	public void moveWatchedVideo() {
		/// 폴더의 최대 크기
		long maximumSizeOfEntireVideo = MAX_ENTIRE_VIDEO * FileUtils.ONE_GB;
		// 한번에 옮길 비디오 개수
		int maximumCountOfMoveVideo = 5;
		// 옮긴 비디오 개수
		int countOfMoveVideo = 0;
		// Watched 폴더
		File mainBaseFile = new File(STORAGE_PATHS[0]);
		// Watched 폴더 크기
		long usedSpace = FileUtils.sizeOfDirectory(mainBaseFile);
		// 여유 공간
		long freeSpace = mainBaseFile.getFreeSpace();
		
		log.info("    MOVE WATCHED VIDEO START :: Watched {} GB, free {} GB", usedSpace / FileUtils.ONE_GB, freeSpace / FileUtils.ONE_GB);

		// 전체 비디오중에서
		for (Video video : getVideoListSortByScore()) {
			
			// 드라이드에 남은 공간이 최소 공간보다 작으면 break
			if (freeSpace < MIN_FREE_SPAC) {
				log.info("      Not enough space. {} < {}", freeSpace / FileUtils.ONE_GB, MIN_FREE_SPAC / FileUtils.ONE_GB);
				break;
			}
			// Watched 폴더 크기가 최대 크기보다 커졌으면 break
			if (usedSpace > maximumSizeOfEntireVideo) {
				log.info("      Exceed the maximum size. {}  > {}", usedSpace / FileUtils.ONE_GB, maximumSizeOfEntireVideo / FileUtils.ONE_GB);
				break;
			}
			
			// 플레이 한적이 없는 비디오는 pass
			if (video.getPlayCount() < 1)
				continue;
			// Watched 폴더에 있는 파일도 pass
			if (video.getDelegatePath().contains(mainBaseFile.getAbsolutePath()))
				continue;
			
			// 스튜디오 이름으로 폴더를 준비
			File destDir = new File(mainBaseFile, video.getStudio().getName());
			if (!destDir.exists())
				destDir.mkdir();

			// 비디오를 옮긴다
			countOfMoveVideo++;
			log.info("    move {} to {}", video.getTitle(), destDir.getPath());
			videoDao.moveVideo(video.getOpus(), destDir.getAbsolutePath());
			
			// 다 옮겼으면 break
			if (countOfMoveVideo == maximumCountOfMoveVideo) {
				log.info("      Completed {} videos.", maximumCountOfMoveVideo);
				break;
			}
			else {
				// 잠시 쉰다.
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					log.error("sleep error", e);
				}
			}

			// 공간을 다시 젠다
			usedSpace = FileUtils.sizeOfDirectory(mainBaseFile);
			freeSpace = mainBaseFile.getFreeSpace();
		}
		usedSpace = FileUtils.sizeOfDirectory(mainBaseFile);
		freeSpace = mainBaseFile.getFreeSpace();
		log.info("    MOVE WATCHED VIDEO END :: Watched {} GB, free {} GB", usedSpace / FileUtils.ONE_GB, freeSpace / FileUtils.ONE_GB);
	}

	@Override
	public void arrangeVideo() {
		for (Video video : videoDao.getVideoList()) {
			String opus = video.getOpus();
			log.trace("    arrange video {}", opus);
			
			// if no cover, find archive
			if (video.isExistVideoFileList() && !video.isExistCoverFile()) {
				try {
					Video archiveVideo = videoDao.getArchiveVideo(opus);
					if (archiveVideo.isExistCoverFile()) {
						video.setCoverFile(archiveVideo.getCoverFile());
						log.info("found in archive storage - {}", opus);
					}
				} catch (VideoException e) {}
			}
			videoDao.arrangeVideo(opus);
		}
	}
	
	@Override
	public List<Video> torrent() {
		log.trace("torrent");
		
		List<Video> list = new ArrayList<Video>();
		for (Video video : videoDao.getVideoList())
			if (!video.isExistVideoFileList()) {
				video.setSortMethod(Sort.VC);
				list.add(video);
			}
		
		log.debug("  need torrent videos - {}", list.size());
		
		List<File> foundFiles = new ArrayList<File>();
		
		for (String candidatePath : CANDIDATE_PATHS) {
		
			// get downloaded torrent file
			File torrentDirectory = new File(candidatePath);
			try {
				FileUtils.validateDirectory(torrentDirectory, "torrent path " + candidatePath);
			}
			catch (JKUtilException e) {
				log.error(e.getMessage());
				continue;
			}
		
			String[] extensions = String.format("%s,%s", CRAZY.SUFFIX_VIDEO.toUpperCase(), CRAZY.SUFFIX_VIDEO.toLowerCase()).split(",");
			log.trace("extensions - {}", Arrays.toString(extensions));
			
			Collection<File> torrents = FileUtils.listFiles(torrentDirectory, extensions, true);
			log.info("  found {} cadidates file in [{}]", torrents.size(), torrentDirectory);
			
			foundFiles.addAll(torrents);
		}
		// matching video file
		for (Video video : list) {
			video.resetVideoCandidates();
			String opus = video.getOpus().toLowerCase();
			log.debug("  OPUS : {}", opus);
			for (String key : Arrays.asList(opus, StringUtils.remove(opus, "-"))) {
				for (File file : foundFiles) {
					String fileName = file.getName().toLowerCase();
					log.trace("    compare : {} = {}", fileName, key);
					if (fileName.contains(key)) {
						video.addVideoCandidates(file);
						log.info("    add video candidate {} : {}", opus, file.getAbsolutePath());
					}
				}
			}
		}
		Collections.sort(list);
		return list;
	}

	@Override
	public void confirmCandidate(String opus, String path) {
		log.trace("confirmCandidate : {} - {}", opus, path);
		
		File destinationPath = null;
		for (String extraPath : STAGE_PATHS) {
			if (FileUtils.compareDrive(path, extraPath)) {
				destinationPath = new File(extraPath);
				break;
			}
		}
		if (destinationPath == null)
			throw new VideoException("Not found proper destination path for candidate file");
		
		Video video = videoDao.getVideo(opus);
		int videoFileSize = video.getVideoFileList().size();
		File candidatedVideofile = new File(path);
		File videoFile = new File(destinationPath, 
				String.format("%s%s.%s", 
						video.getFullname(), 
						videoFileSize > 0 ? String.valueOf(++videoFileSize) : "", 
						FileUtils.getExtension(candidatedVideofile)));
		try {
			FileUtils.moveFile(candidatedVideofile, videoFile);
			log.info("move to {}", videoFile.getAbsoluteFile());
		} 
		catch (IOException e) {
			throw new VideoException(video, "candidate file moving error", e);
		}
		video.addVideoFile(videoFile);
	}

	@Override
	public Map<Integer, List<Video>> groupByScore() {
		log.trace("groupByScore");
		Map<Integer, List<Video>> map = new TreeMap<Integer, List<Video>>(Collections.reverseOrder());
		for (Video video : videoDao.getVideoList()) {
			Integer score = video.getScore();
			if (map.containsKey(score)) {
				map.get(score).add(video);
			}
			else {
				List<Video> videoList = new ArrayList<Video>();
				videoList.add(video);
				map.put(score, videoList);
			}
		}
		log.debug("video group by score - {}", map);
		return map;
	}

	@Override
	public void rename(String opus, String newName) {
		Video video = videoDao.getVideo(opus);
		video.rename(newName);
		videoDao.reload();
	}

	@Override
	public void renameOfActress(String name, String newName) {
		Actress actress = videoDao.getActress(name);
		for (Video video : actress.getVideoList()) {
			video.renameOfActress(name, newName);
		}
		actress.renameInfo(newName);
		videoDao.reload();
	}

	@Override
	public void renameOfStudio(String name, String newName) {
		for (Video video : videoDao.getStudio(name).getVideoList())
			video.renameOfStudio(newName);
		videoDao.reload();
	}

	@Override
	public List<TitlePart> parseToTitleData(String titleData) {
		List<TitlePart> titlePartList = new ArrayList<TitlePart>();
		
		if (!StringUtils.isEmpty(titleData)) {
			titleData += System.getProperty("line.separator") + System.getProperty("line.separator") + "eof";
			String[] titles = titleData.split(System.getProperty("line.separator"));

			String text = null;
			try {
				for (int i = 0; i < titles.length; i++) {
					if (titles[i].trim().length() > 0) {
//						log.info("START =======================================");
						// make TitlePart
						TitlePart titlePart = new TitlePart();
						// opus
						text = titles[i++].trim().toUpperCase();
//						log.info("text:{} ------------------", text);
						titlePart.setOpus(text);
//						log.info("1. opus : {}", text);
						
						// actress
						text = titles[i++].trim();
//						log.info("text:{} ------------------", text);
						if (!Pattern.matches("^[a-zA-Z\\s,]+", text)) { // 배우이름이 없어서 날자가 온거면
							text = "";
							i--;
						}
						titlePart.setActress(text);
//						log.info("2. actress : {}", text);
						
						// release date
						text = titles[i++].trim();
//						log.info("text:{} ------------------", text);
						if (!Pattern.matches("\\d{4}.\\d{2}.\\d{2}", text)) { // 날자가 없어서 제목이 온거면
							text = "";
							i--;
						}
						titlePart.setReleaseDate(text);
//						log.info("3. releaseDate : {}", text);
						
						// title
						String title = "";
						while (true) {
							text = titles[i++].trim();
//							log.info("text:{} ------------------", text);
							if (StringUtils.isEmpty(text)) {
//								log.info("text is empty. next ------------------");
								i--;
								break;
							}
							title += text + " ";
						}
						titlePart.setTitle(title.trim());
//						log.info("4. title : {}", title);
						
						// check already contains
						if (videoDao.contains(titlePart.getOpus())) {
							log.info("{} exist", titlePart.getOpus());
							continue;
						}
						
						// history check
						if (historyService.contains(titlePart.getOpus())) {
							titlePart.setSeen();
						}

						// find Studio
						String opusPrefix = StringUtils.substringBefore(titlePart.getOpus(), "-");
						if (NO_PARSE_OPUS_PREFIX.contains(opusPrefix)) {
							titlePart.setStudio("");
						}
						else if (StringUtils.contains(ArrayUtils.toStringComma(REPLACE_OPUS_INFO), opusPrefix)) {
							for (String reOpus : REPLACE_OPUS_INFO) {
								String[] opus = StringUtils.split(reOpus, "-");
								if (StringUtils.equals(opus[0], opusPrefix)) {
									titlePart.setStudio(opus[1]);
									break;
								}
							}
						}
						else {
							List<Map<String, String>> histories = findHistory(opusPrefix + "-");
							if (histories.size() > 0) {
								Map<String, String> data = histories.get(0);
								String desc = data.get("desc");
								
								titlePart.setStudio(StringUtils.substringBefore(StringUtils.substringAfter(desc, "["), "]"));
							}
							else {
								titlePart.setStudio("");
							}
						}
						
						// add TitlePart
						titlePartList.add(titlePart);
					}
				}
			} catch(ArrayIndexOutOfBoundsException e) {
				log.error("End");
			}
			// sort list
			Collections.sort(titlePartList);
		}
		return titlePartList;
	}
	
	@Override
	public List<TitlePart> parseToTitleData2(String titleData) {
		List<TitlePart> titlePartList = new ArrayList<TitlePart>();
		
		final String UNKNOWN 			 = "_Unknown";
//		final String unclassifiedStudio  = UNKNOWN;
		final String unclassifiedOpus 	 = UNKNOWN;
		final String unclassifiedActress = "Amateur";

		
		if (!StringUtils.isEmpty(titleData)) {
			String[] titles = titleData.split(System.getProperty("line.separator"));

			try {
				for (int i = 0; i < titles.length; i++) {
					if (!StringUtils.isEmpty(titles[i])) {
						String[] names 		= StringUtils.split(titles[i], "]");
//						String studioName 	 = VideoUtils.removeUnnecessaryCharacter(names[0], unclassifiedStudio);
						String opus 		 = VideoUtils.removeUnnecessaryCharacter(names[1], unclassifiedOpus);
						String title 		 = VideoUtils.removeUnnecessaryCharacter(names[2], UNKNOWN);
						String actressNames = VideoUtils.removeUnnecessaryCharacter(names[3], unclassifiedActress);
						String releaseDate  = VideoUtils.removeUnnecessaryCharacter(names[4]);

						TitlePart titlePart = new TitlePart();
						titlePart.setOpus(opus);
						titlePart.setTitle(title);
						titlePart.setActress(actressNames);
						titlePart.setReleaseDate(releaseDate);
					
						if (videoDao.contains(titlePart.getOpus())) {
							log.info("{} exist", titlePart.getOpus());
							continue;
						}
						
						// history check
						if (historyService.contains(titlePart.getOpus())) {
							titlePart.setSeen();
						}

						// find Studio
						List<Map<String, String>> histories = findHistory(StringUtils.substringBefore(titlePart.getOpus(), "-") + "-");
						if (histories.size() > 0) {
							Map<String, String> data = histories.get(0);
							String desc = data.get("desc");
							
							titlePart.setStudio(StringUtils.substringBefore(StringUtils.substringAfter(desc, "["), "]"));
						}
						else {
							titlePart.setStudio("");
						}
						
						// add TitlePart
						titlePartList.add(titlePart);
					}
				}
			} catch(ArrayIndexOutOfBoundsException e) {
				// do nothing
			}
			// sort list
			Collections.sort(titlePartList);
		}
		return titlePartList;
	}
	
	@Override
	public Map<Integer, List<Video>> groupByLength() {
		log.trace("groupByLength");
		Map<Integer, List<Video>> map = new TreeMap<Integer, List<Video>>(Collections.reverseOrder());
		for (Video video : videoDao.getVideoList()) {
			Integer length = (int)Math.ceil(video.getLength() / (double)FileUtils.ONE_GB);
//			Integer length = Integer.parseInt(d);

			if (map.containsKey(length)) {
				map.get(length).add(video);
			}
			else {
				List<Video> videoList = new ArrayList<Video>();
				videoList.add(video);
				map.put(length, videoList);
			}
		}
		log.debug("video group by length - {}", map);
		return map;
	}

	@Override
	public Map<String, List<Video>> groupByExtension() {
		log.trace("groupByExtension");
		Map<String, List<Video>> map = new TreeMap<String, List<Video>>(Collections.reverseOrder());
		for (Video video : videoDao.getVideoList()) {
			String ext = video.getExt().toLowerCase();

			if (map.containsKey(ext)) {
				map.get(ext).add(video);
			}
			else {
				List<Video> videoList = new ArrayList<Video>();
				videoList.add(video);
				map.put(ext, videoList);
			}
		}
		log.debug("video group by extension - {}", map);
		return map;
	}

	private File getInfoDir() {
		return new File(STORAGE_PATHS[0], "_info");
	}

	@Override
	public void resetVideoScore(String opus) {
		log.trace("resetVideoScore - {}", opus);
		videoDao.getVideo(opus).resetScore();
		videoDao.reload();
	}

	@Override
	public void resetWrongVideo(String opus) {
		log.trace("resetWrongVideo - {}", opus);
		videoDao.getVideo(opus).moveOutside();
		videoDao.getVideo(opus).resetScore();
		videoDao.reload();
	}

	@Override
	public void arrangeArchiveVideo() {
		log.trace("arrangeArchiveVideo");
		for (Video video : videoDao.getArchiveVideoList()) {
			video.arrange();
		}
	}

	@Override
	public List<Video> searchVideoInArchive(VideoSearch search) {
		log.trace("{}", search);
		if (search.getRankRange() == null)
			search.setRankRange(getRankRange());
		
		List<Video> foundList = new ArrayList<Video>();
		for (Video video : videoDao.getArchiveVideoList()) {
			if ((VideoUtils.equals(video.getStudio().getName(), search.getSearchText()) 
					|| VideoUtils.equals(video.getOpus(), search.getSearchText()) 
					|| VideoUtils.containsName(video.getTitle(), search.getSearchText()) 
					|| VideoUtils.containsActress(video, search.getSearchText())
					|| VideoUtils.containsName(video.getReleaseDate(), search.getSearchText())) 
				&& (search.isAddCond()   
						? ((search.isExistVideo() ? video.isExistVideoFileList() : !video.isExistVideoFileList())
							&& (search.isExistSubtitles() ? video.isExistSubtitlesFileList() : !video.isExistSubtitlesFileList())) 
						: true)
//				&& (search.getSelectedStudio() == null ? true : search.getSelectedStudio().contains(video.getStudio().getName()))
//				&& (search.getSelectedActress() == null ? true : VideoUtils.containsActress(video, search.getSelectedActress()))
//				&& (rankMatch(video.getRank(), search.getRankRange()))
//				&& (playCountMatch(video.getPlayCount(), search.getPlayCount()))
				) 
			{
				video.setSortMethod(search.getSortMethod());
				foundList.add(video);
			}
		}
		if (search.isSortReverse())
			Collections.sort(foundList, Collections.reverseOrder());
		else
			Collections.sort(foundList);

		log.debug("found video list size {}", foundList.size());
		return foundList;
	}

}
