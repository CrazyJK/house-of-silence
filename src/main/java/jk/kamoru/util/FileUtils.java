package jk.kamoru.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jk.kamoru.crazy.video.VIDEO;
import jk.kamoru.crazy.video.VideoException;

/**
 * commons.io.FileUtils 상속하고 필요한 기능 추가
 * @author kamoru
 *
 */
public class FileUtils extends org.apache.commons.io.FileUtils {
	
	public static final String EXTENSION_SEPARATOR = ".";

	
	/**
	 * 입력된 경로의 모든 파일명을 상위폴더명+일련번호로 변경
	 * @param basepath
	 */
	public static void renameToFoldername(File basepath) {
		Assert.isTrue(basepath.isDirectory(), "It is not directory! ");
		File[] files = basepath.listFiles();
		int digit = String.valueOf(files.length).length();
		int count = 0;
		for(File f : files) {
			String parent = f.getParent();
			String folder = parent.substring(parent.lastIndexOf(System.getProperty("file.separator")) + 1, parent.length());
			String fullname = f.getName();
			int lastIndex = fullname.lastIndexOf(".");
			String ext = fullname.substring(lastIndex + 1);
			String newName = folder + StringUtils.addZero(++count, digit);
			
			f.renameTo(new File(parent, newName + "." + ext.toLowerCase()));
		}
	}
	
	public static void renameToMelon(File basepath) {
		Assert.isTrue(basepath.isDirectory());
		for (File file : FileUtils.listFiles(basepath, null, false)) {
			String name = file.getName();
			if (name.length() > 4)
				file.renameTo(new File(file.getParentFile(), name.substring(4)));
		}
	}

	/**
	 * 확장자를 뺀 파일 이름
	 * @param file
	 * @return 확장자 없는 파일명
	 */
	public static String getNameExceptExtension(File file) {
		Assert.isTrue(file.isFile(), "It is not file! - " + file.getAbsolutePath());
		return StringUtils.substringBeforeLast(file.getName(), EXTENSION_SEPARATOR);
	}

	/**
	 * 파일의 확장자
	 * @param file
	 * @return 파일 확장자
	 */
	public static String getExtension(File file) {
		Assert.isTrue(file.isFile(), "It is not file! - " + file.getAbsolutePath());
		return StringUtils.substringAfterLast(file.getName(), EXTENSION_SEPARATOR);
	}

	/**
	 * 확장자에 맞는 파일을 찾는다
	 * @param directories 찾을 디렉토리
	 * @param extensions 파일 확장자. null이면 모든 파일
	 * @param recursive 하위폴더 검색 여부
	 * @return
	 */
	public static List<File> listFiles(String[] directories, String[] extensions, boolean recursive) {
		List<File> dirFiles = new ArrayList<File>();
		for (String directory : directories)
			dirFiles.add(new File(directory));
		return listFiles(dirFiles, extensions, recursive);
	}

	/**
	 * 확장자에 맞는 파일을 찾는다
	 * @param dirFiles 찾을 폴더
	 * @param extensions 파일 확장자. null이면 모든 파일
	 * @param recursive 하위폴더 검색 여부
	 * @return
	 */
	public static List<File> listFiles(List<File> dirFiles, String[] extensions, boolean recursive) {
		List<File> list = new ArrayList<File>();
		for (File dir : dirFiles)
			if (dir.isDirectory())
				list.addAll(listFiles(dir, extensions, recursive));
			else
				list.add(dir);
		return list;
	}

	/**
	 * 빈 디렉토리인지
	 * @param dir
	 * @return
	 */
	public static boolean isEmptyDirectory(File dir) {
		return listFiles(dir, null, true).isEmpty();
	}
	
	/**파일 이름 변경
	 * @param file
	 * @param name
	 * @return
	 */
	public static boolean rename(File file, String name) {
		if (file == null)
			return false;
		else 
			return file.renameTo(new File(file.getParentFile(), name + EXTENSION_SEPARATOR + getExtension(file)));
	}
	
	/**파일인지 검증. 존재하는지, 진짜 파일인지
	 * @param file
	 * @param fileDesc 파일설명
	 * @throws KamoruException
	 */
	public static void validateFile(File file, String fileDesc) {
		Assert.isTrue(file.exists(), fileDesc + " dose not exists");
		Assert.isTrue(file.isFile(), fileDesc + " is not a normal file");
	}
	
	/**파일인지 검증. 존재하는지, 진짜 파일인지
	 * @param file
	 * @throws KamoruException
	 */
	public static void validateFile(File file) {
		validateFile(file, "file");
	}
	
	/**디렉토리인지 검증. 존재하는지, 진짜 디렉토리인지
	 * @param directory
	 * @param dirDesc 디렉토리 설명
	 * @throws KamoruException
	 */
	public static void validateDirectory(File directory, String dirDesc) {
		Assert.isTrue(directory.exists(), dirDesc + " dose not exists");
		Assert.isTrue(directory.isDirectory(), dirDesc + " is not a directory");
	}
	
	/**디렉토리인지 검증. 존재하는지, 진짜 디렉토리인지
	 * @param directory
	 * @throws KamoruException
	 */
	public static void validateDirectory(File directory) {
		validateDirectory(directory, "file");
	}
	
	/**
	 * properties 형태의 파일을 읽어 Map으로 반환
	 * @see #saveFileFromMap(File, Map)
	 * @param file
	 * @return 파일이 없으면 빈 Map
	 * @throws UtilException
	 */
	public static Map<String, String> readFileToMap(File file) {
		validateFile(file, "file of readFileToMap");
		try {
			Map<String, String> map = new HashMap<String, String>();
			for (String str : FileUtils.readLines(file, VIDEO.FILE_ENCODING)) {
				String[] strs = StringUtils.split(str, "=", 2);
				if (strs.length > 1)
					map.put(strs[0], strs[1]);
			}
			return map;
		} catch (IOException e) {
			throw new VideoException("read file error", e);
		}
	}

	/**
	 * Map 내용을 file로 저장
	 * @see #readFileToMap(File)
	 * @param file
	 * @param params
	 * @throws UtilException
	 */
	public static void saveFileFromMap(File file, Map<String, String> params) {
		if (params == null || params.size() == 0)
			throw new VideoException("params is null or size 0");
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(entry.getKey().toUpperCase().trim()).append("=").append(entry.getValue().trim());
		}
		try {
			FileUtils.writeStringToFile(file, sb.toString(), VIDEO.FILE_ENCODING);
		} catch (IOException e) {
			throw new VideoException("write file error", e);
		}
	}

}
