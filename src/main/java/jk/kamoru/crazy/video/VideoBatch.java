package jk.kamoru.crazy.video;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.PostConstruct;

import jk.kamoru.crazy.video.service.VideoService;
import jk.kamoru.util.ArrayUtils;

import org.apache.commons.io.FileUtils;
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

	@Value("#{prop['batch.watched.moveVideo']}")	private boolean MOVE_WATCHED_VIDEO;
	@Value("#{prop['batch.rank.deleteVideo']}") 	private boolean DELETE_LOWER_RANK_VIDEO;
	@Value("#{prop['batch.score.deleteVideo']}") 	private boolean DELETE_LOWER_SCORE_VIDEO;
	
	@Value("#{local['path.move.file']}")	private String[] PATH_MOVE_FILE;

	public boolean isMOVE_WATCHED_VIDEO() {
		return MOVE_WATCHED_VIDEO;
	}
	public void setMOVE_WATCHED_VIDEO(boolean setValue) {
		MOVE_WATCHED_VIDEO = setValue;
		showProperties();
	}
	public boolean isDELETE_LOWER_RANK_VIDEO() {
		return DELETE_LOWER_RANK_VIDEO;
	}
	public void setDELETE_LOWER_RANK_VIDEO(boolean setValue) {
		DELETE_LOWER_RANK_VIDEO = setValue;
		showProperties();
	}
	public boolean isDELETE_LOWER_SCORE_VIDEO() {
		return DELETE_LOWER_SCORE_VIDEO;
	}
	public void setDELETE_LOWER_SCORE_VIDEO(boolean setValue) {
		DELETE_LOWER_SCORE_VIDEO = setValue;
		showProperties();
	}

	@PostConstruct
	private void showProperties() {
		logger.info("Batch properties");
		logger.info("  - batch.watched.moveVideo = {}", MOVE_WATCHED_VIDEO);
		logger.info("  - batch.rank.deleteVideo  = {}", DELETE_LOWER_RANK_VIDEO);
		logger.info("  - batch.score.deleteVideo = {}", DELETE_LOWER_SCORE_VIDEO);
	}
	
	@Scheduled(cron="0 */5 * * * *")
	public synchronized void batchVideoSource() {
		
		logger.info("BATCH Video START");
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
		logger.info("BATCH Video END. Elapsed time : {} ms", elapsedTime);
	}
	
	@Scheduled(cron="0 */1 * * * *")
	public synchronized void moveFile() {
		logger.info("BATCH File move START");

		// 설정이 안됬거나
		if (PATH_MOVE_FILE == null) {
			logger.error("PATH is null");
			return;
		}
		// 값이 없거나, 3배수가 아니거나
		if (PATH_MOVE_FILE.length == 0 || PATH_MOVE_FILE.length % 3 != 0) {
			logger.error("PATH length is zero or odd", ArrayUtils.toStringComma(PATH_MOVE_FILE));
			return;
		}
		// 2,3번째가 폴더가 아니거나
		for (int i=0; i<PATH_MOVE_FILE.length; i++) {
			if (i % 3 == 0)
				continue;
			else
				if (!new File(PATH_MOVE_FILE[i]).isDirectory()) {
					logger.error("PATH [{}] is not Directory", PATH_MOVE_FILE[i]);
					return;
				}
		}
		for (int i=0; i<PATH_MOVE_FILE.length;) {
			String ext = PATH_MOVE_FILE[i++];
			File from = new File(PATH_MOVE_FILE[i++]);
			File to   = new File(PATH_MOVE_FILE[i++]);
			for (File file : FileUtils.listFiles(from, new String[]{ext.toUpperCase(), ext.toLowerCase()}, false)) {
				try {
					logger.info("Moving... {} to {}", file.getAbsolutePath(), to.getAbsolutePath());
					FileUtils.moveFileToDirectory(file, to, false);
				}
				catch (IOException e) {
					logger.error("File to move", e);
				}
			}
		}
		
		logger.info("BATCH File move END");
	}
	
}
