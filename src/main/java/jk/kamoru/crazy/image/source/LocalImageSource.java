package jk.kamoru.crazy.image.source;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jk.kamoru.crazy.image.IMAGE;
import jk.kamoru.crazy.image.ImageNotFoundException;
import jk.kamoru.crazy.image.domain.Image;
import jk.kamoru.util.FileUtils;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

/**
 * Implementation of {@link ImageSource}
 * @author kamoru
 *
 */
@Repository
@Slf4j
public class LocalImageSource implements ImageSource {

	private List<Image> imageList = new ArrayList<Image>();

	@Value("#{local['path.image.storage']}") private String[] imageStoragePath;

	private synchronized void load() {
		int idx = 0;

		imageList.clear();
		for (String path : this.imageStoragePath) {
			File dir = new File(path);
			if (dir.isDirectory()) {
				log.debug("image scanning : {}", dir);
				for (File file : FileUtils.listFiles(dir, IMAGE.imageSuffix, true))
					imageList.add(new Image(file, idx++));
			}
		}
		log.info("Total found image size : {}", imageList.size());
		
		Collections.sort(imageList, new Comparator<Image>() {
			@Override
			public int compare(Image o1, Image o2) {
				return NumberUtils.compare(o1.getLastModified(), o2.getLastModified());
			}
		});
	}

	private List<Image> imageSource() {
		if (imageList == null)
			load();
		return imageList;
	}

	@Override
	public Image getImage(int idx) {
		try {
			return imageSource().get(idx);
		}
		catch(IndexOutOfBoundsException  e) {
			throw new ImageNotFoundException(idx);
		}
	}

	@Override
	public List<Image> getImageList() {
		return imageSource();
	}

	@Override
	public int getImageSourceSize() {
		return imageSource().size();
	}

	@Override
	public void delete(int idx) {
		imageSource().get(idx).delete();
		imageSource().remove(idx);
	}

	@Override
	@Scheduled(cron = "0 */7 * * * *")
	public void reload() {
		load();
	}

}
