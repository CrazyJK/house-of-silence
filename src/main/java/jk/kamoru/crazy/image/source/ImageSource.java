package jk.kamoru.crazy.image.source;

import java.util.List;

import jk.kamoru.crazy.image.domain.Image;

/**
 * Image source
 * @author kamoru
 *
 */
public interface ImageSource {

	/**
	 * image
	 * @param idx
	 * @return
	 */
	Image getImage(int idx);
	
	/**
	 * total image list
	 * @return
	 */
	List<Image> getImageList();

	/**
	 * total image size
	 * @return
	 */
	int getImageSourceSize();
	
	/**
	 * image source reload
	 */
	void reload();

	/**
	 * delete image
	 * @param idx
	 */
	void delete(int idx);
	
}
