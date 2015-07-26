package jk.kamoru.crazy.video;

import jk.kamoru.crazy.video.service.VideoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VideoBatch {

	private static final Logger logger = LoggerFactory.getLogger(VideoBatch.class);

	@Autowired VideoService videoService;

	@Value("#{prop['batch.watched.moveVideo']}")	private static boolean MOVE_WATCHED_VIDEO;
	@Value("#{prop['batch.rank.deleteVideo']}") 	private static boolean DELETE_LOWER_RANK_VIDEO;
	@Value("#{prop['batch.score.deleteVideo']}") 	private static boolean DELETE_LOWER_SCORE_VIDEO;

	public boolean isMOVE_WATCHED_VIDEO() {
		return MOVE_WATCHED_VIDEO;
	}
	public void setMOVE_WATCHED_VIDEO(boolean setValue) {
		MOVE_WATCHED_VIDEO = setValue;
		logger.info("set {}", MOVE_WATCHED_VIDEO);
	}
	public boolean isDELETE_LOWER_RANK_VIDEO() {
		return DELETE_LOWER_RANK_VIDEO;
	}
	public void setDELETE_LOWER_RANK_VIDEO(boolean setValue) {
		DELETE_LOWER_RANK_VIDEO = setValue;
	}
	public boolean isDELETE_LOWER_SCORE_VIDEO() {
		return DELETE_LOWER_SCORE_VIDEO;
	}
	public void setDELETE_LOWER_SCORE_VIDEO(boolean setValue) {
		DELETE_LOWER_SCORE_VIDEO = setValue;
	}

	@Scheduled(cron="0 */5 * * * *")
	public synchronized void batchVideoSource() {
		
		logger.info("BATCH START");
		long startTime = System.currentTimeMillis();

		logger.info("  BATCH : delete lower rank video [{}]", DELETE_LOWER_RANK_VIDEO);
		if (DELETE_LOWER_RANK_VIDEO)
			videoService.removeLowerRankVideo();
		
		logger.info("  BATCH : delete lower score video [{}]", DELETE_LOWER_SCORE_VIDEO);
		if (DELETE_LOWER_SCORE_VIDEO)
			videoService.removeLowerScoreVideo();
		
		logger.info("  BATCH : delete garbage file");
		videoService.deleteGarbageFile();
		
		logger.info("  BATCH : arrange to same folder");
		videoService.arrangeVideo();
		
		logger.info("  BATCH : move watched video [{}]", MOVE_WATCHED_VIDEO);
		if (MOVE_WATCHED_VIDEO)
			videoService.moveWatchedVideo();

		logger.info("  BATCH : reload");
		videoService.reload();
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		logger.info("BATCH END. Elapsed time : {} ms", elapsedTime);
	}
	
}
