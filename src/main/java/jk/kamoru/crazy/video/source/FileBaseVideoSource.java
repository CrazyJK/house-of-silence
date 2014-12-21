package jk.kamoru.crazy.video.source;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import jk.kamoru.crazy.CRAZY;
import jk.kamoru.crazy.video.ActressNotFoundException;
import jk.kamoru.crazy.video.StudioNotFoundException;
import jk.kamoru.crazy.video.VIDEO;
import jk.kamoru.crazy.video.VideoNotFoundException;
import jk.kamoru.crazy.video.domain.Actress;
import jk.kamoru.crazy.video.domain.Studio;
import jk.kamoru.crazy.video.domain.Video;
import jk.kamoru.crazy.video.util.VideoUtils;
import jk.kamoru.util.FileUtils;
import jk.kamoru.util.StringUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
	

public class FileBaseVideoSource implements VideoSource {
	
	protected static final Logger logger = LoggerFactory.getLogger(FileBaseVideoSource.class);

	public static final String unclassifiedActress = "Amateur";

	private final String UNKNOWN 			 = "_Unknown";
	private final String unclassifiedStudio  = UNKNOWN;
	private final String unclassifiedOpus 	 = UNKNOWN;

	// data source
	private Map<String, Video>     videoMap	= new HashMap<String, Video>();
	private Map<String, Studio>   studioMap	= new HashMap<String, Studio>();
	private Map<String, Actress> actressMap = new HashMap<String, Actress>();
	
	// Domain provider
	@Inject Provider<Video>     videoProvider;
	@Inject Provider<Studio>   studioProvider;
	@Inject Provider<Actress> actressProvider;

	// logic variables
	private static boolean firstLoad = false;
	private static boolean loading = false;
	
	// property
	private String[] paths;

	// property setter
	public void setPaths(String...paths) {
		this.paths = paths;
	}
	
	/**
	 * 기존에 만든적이 없으면, video source를 로드를 호출한다.
	 */
	private final void videoSource() {
//		logger.info("firstLoad = {}", firstLoad);
		if (firstLoad) {
//			logger.info("loading = {}", loading);
			if (loading) {
				do {
					try {
						logger.info("loading...");
						Thread.sleep(500);
					} catch (InterruptedException e) {
						logger.error("sleep error", e);
						break;
					}
				} while(loading);
			}
		}
		else {
			load();
		}
			
	}
	
	/**
	 * video데이터를 로드한다.
	 */
	private synchronized void load() {
		firstLoad = true;
		loading = true;
		
		// find files
		Collection<File> files = FileUtils.listFiles(paths, null, true);
		/*
		Collection<File> files = new ArrayList<File>();
		for (String path : paths) {
			File directory = new File(path);
			logger.debug("directory scanning : {}", directory.getAbsolutePath());
			if (directory.isDirectory()) {
				Collection<File> found = FileUtils.listFiles(directory, null, true);
				logger.debug("\tfound file size is {}", found.size());
				files.addAll(found);
			}
			else {
				logger.warn("\tIt is not directory. Pass!!!");
			}
		}*/
		logger.info("    total found file {}", files.size());

		videoMap.clear();
		studioMap.clear();
		actressMap.clear();

		// 3. domain create & data source   
		int unclassifiedNo = 1;
		for (File file : files) {
			try {
				String filename = file.getName();
				String     name = FileUtils.getNameExceptExtension(file);
				String      ext = FileUtils.getExtension(file).toLowerCase();
				
				// 연속 스페이스 제거
				name = StringUtils.normalizeSpace(name);
				// Unnecessary file exclusion
				if (filename.equals(VIDEO.HISTORY_LOG) 
						|| filename.equals(VIDEO.MAC_NETWORKSTORES)
						|| filename.equals(VIDEO.WINDOW_DESKTOPINI)
						|| ext.equals(VIDEO.EXT_ACTRESS) 
						|| ext.equals(VIDEO.EXT_STUDIO))
					continue;
				
				// 1       2     3      4        5     6
				// [studio][opus][title][actress][date]etc...
				String[] names 		= StringUtils.split(name, "]");
				String studioName  	= UNKNOWN;
				String opus    		= UNKNOWN;
				String title   		= filename;
				String actressNames = UNKNOWN;
				String releaseDate 	= "";
				String etcInfo 		= "";
				
				switch (names.length) {
				case 6:
					etcInfo 	 = VideoUtils.removeUnnecessaryCharacter(names[5]);
				case 5:
					releaseDate  = VideoUtils.removeUnnecessaryCharacter(names[4]);
				case 4:
					actressNames = VideoUtils.removeUnnecessaryCharacter(names[3], unclassifiedActress);
				case 3:
					title 		 = VideoUtils.removeUnnecessaryCharacter(names[2], UNKNOWN);
				case 2:
					opus 		 = VideoUtils.removeUnnecessaryCharacter(names[1], unclassifiedOpus);
					studioName 	 = VideoUtils.removeUnnecessaryCharacter(names[0], unclassifiedStudio);
					break;
				case 1:
					studioName 	 = unclassifiedStudio;
					opus 		 = unclassifiedOpus + unclassifiedNo++;
					title 		 = filename;
					actressNames = unclassifiedActress;
					break;
				default: // if names length is over 6
					logger.debug("File [{}] [{}] [{}]", filename, names.length, ArrayUtils.toString(names));
					studioName 	 = VideoUtils.removeUnnecessaryCharacter(names[0], unclassifiedStudio);
					opus 		 = VideoUtils.removeUnnecessaryCharacter(names[1], unclassifiedOpus);
					title 		 = VideoUtils.removeUnnecessaryCharacter(names[2], UNKNOWN);
					actressNames = VideoUtils.removeUnnecessaryCharacter(names[3], unclassifiedActress);
					releaseDate  = VideoUtils.removeUnnecessaryCharacter(names[4]);
					for (int i=5, iEnd=names.length; i<iEnd; i++)
						etcInfo = etcInfo + " " + VideoUtils.removeUnnecessaryCharacter(names[i]);
				}
				
				Video video = videoMap.get(opus.toLowerCase());
				if (video == null) {
					video = this.videoProvider.get();
					video.setOpus(opus.toUpperCase());
					video.setTitle(title);
					video.setReleaseDate(releaseDate);
					video.setEtcInfo(etcInfo);
					videoMap.put(opus.toLowerCase(), video);
					logger.trace("add video - {}", video);
				}
				// set video File
				if (CRAZY.SUFFIX_VIDEO.contains(ext))
					video.addVideoFile(file);
				else if (CRAZY.SUFFIX_IMAGE.contains(ext))
					video.setCoverFile(file);
				else if (CRAZY.SUFFIX_SUBTITLES.contains(ext))
					video.addSubtitlesFile(file);
				else if (VIDEO.EXT_INFO.equalsIgnoreCase(ext))
					video.setInfoFile(file);
				else
					video.addEtcFile(file);
				
				Studio studio = studioMap.get(studioName.toLowerCase());
				if (studio == null) {
					studio = this.studioProvider.get();
					studio.setName(studioName);
					studioMap.put(studioName.toLowerCase(), studio);
					logger.trace("add studio - {}", studio);
				}

				// inject reference
				studio.addVideo(video);
				video.setStudio(studio);
				
				for (String actressName : StringUtils.split(actressNames, ",")) { 
					String forwardActressName = VideoUtils.sortForwardName(actressName);
					Actress actress = actressMap.get(forwardActressName);
					if (actress == null) {
						actress = actressProvider.get();
						actress.setName(actressName.trim());
						actressMap.put(forwardActressName, actress);
						logger.trace("add actress - {}", actress);
					}
					// inject reference
					actress.addVideo(video);
					actress.addStudio(studio);

					studio.addActress(actress);
					video.addActress(actress);
				}
			}
			catch (NullPointerException e) {
				logger.error("{}", e);
			}
			catch (Exception e) {
				logger.error("Error : {} - {}", file.getAbsolutePath(), e);
			}
		}
		logger.info("    total loaded video {}", videoMap.size());
		loading = false;
	}

	@Override
	public void reload() {
		load();
	}
	
	@Override
	public Map<String, Video> getVideoMap() {
		videoSource();
		return videoMap;
	}
	@Override
	public Map<String, Studio> getStudioMap() {
		videoSource();
		return studioMap;
	}
	@Override
	public Map<String, Actress> getActressMap() {
		videoSource();
		return actressMap;
	}
	@Override
	public Video getVideo(String opus) {
		videoSource();
		if (videoMap.containsKey(opus.toLowerCase()))
			return videoMap.get(opus.toLowerCase());
		else
			throw new VideoNotFoundException(opus);
	}
	@Override
	public Studio getStudio(String name) {
		videoSource();
		if (studioMap.containsKey(name.toLowerCase()))
			return studioMap.get(name.toLowerCase());
		else
			throw new StudioNotFoundException(name);
	}
	@Override
	public Actress getActress(String name) {
		videoSource();
		if (actressMap.containsKey(VideoUtils.sortForwardName(name)))
			return actressMap.get(VideoUtils.sortForwardName(name));
		else
			throw new ActressNotFoundException(name);
	}
	@Override
	public List<Video> getVideoList() {
		videoSource();
		return new ArrayList<Video>(videoMap.values());
	}
	@Override
	public List<Studio> getStudioList() {
		videoSource();
		return new ArrayList<Studio>(studioMap.values());
	}
	@Override
	public List<Actress> getActressList() {
		videoSource();
		return new ArrayList<Actress>(actressMap.values());
	}
	@Override
	public void moveVideo(String opus, String destPath) {
		videoSource();
		videoMap.get(opus.toLowerCase()).move(destPath);
	}
	@Override
	public void arrangeVideo(String opus) {
		videoSource();
		videoMap.get(opus.toLowerCase()).arrange();
	}
	@Override
	public void removeVideo(String opus) {
		videoSource();
		videoMap.get(opus.toLowerCase()).removeVideo();
		videoMap.remove(opus.toLowerCase());
	}
	@Override
	public void deleteVideo(String opus) {
		videoSource();
		videoMap.get(opus.toLowerCase()).deleteVideo();
		videoMap.remove(opus.toLowerCase());
	}

}
