package jk.kamoru.crazy.video.util;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jk.kamoru.crazy.video.domain.Video;
import jk.kamoru.util.FileUtils;
import jk.kamoru.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Video에서 이용되는 utility method 모음
 * @author kamoru
 *
 */
public class VideoUtils {

//	/**
//	 * 배열을 컴마(,)로 구분한 문자열로 반환. a, b<br>
//	 * ArrayUtils.toString() 이용
//	 * 
//	 * @param array
//	 * @return string of a, b style
//	 */
//	public static String arrayToString(Object array) {
//		return StringUtils.replaceEach(ArrayUtils.toString(array, "{}"), new String[] {"{", "}"}, new String[] {"", ""});
//	}

	/**
	 * 같은 배우 이름인지 확인
	 * <br>대소문자 구분없이 공백을 기준으로 순차 정렬하여 비교.
	 * 
	 * @param name1
	 * @param name2
	 * @return {@code true} if equal
	 */
	public static boolean equalsActress(String name1, String name2) {
		if (name1 == null || name2 == null)
			return false;
		return StringUtils.equalsIgnoreCase(sortForwardName(name1), sortForwardName(name2));
	}

	/**
	 * 공백이 들어간 이름을 소문자 순차정렬해서 반환
	 * 
	 * @param name
	 * @return string of sort name
	 */
	public static String sortForwardName(String name) {
		if (name == null)
			return "";
		String[] nameArr = StringUtils.split(name);
		Arrays.sort(nameArr);
		return StringUtils.join(nameArr, " ");
	}

	/**
	 * video list을 opus값 배열 스타일로 반환. ["abs-123", "avs-34"]
	 * 
	 * @param videoList
	 * @return string of opus list
	 */
	public static String getOpusArrayStyleString(List<Video> videoList) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0, iEnd = videoList.size(); i < iEnd; i++) {
			if (i > 0)
				sb.append(",");
			sb.append("\"").append(videoList.get(i).getOpus()).append("\"");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * video list중 video파일이 있는것만 골라 opus값 배열 스타일로 반환. ["abs-123", "avs-34"]
	 * 
	 * @param videoList
	 * @return string of opus list with file
	 */
	public static String getOpusArrayStyleStringWithVideofile(List<Video> videoList) {
		List<Video> videoListWithVideofile = new ArrayList<Video>();
		for (Video video : videoList)
			if (video.isExistVideoFileList())
				videoListWithVideofile.add(video);
		return getOpusArrayStyleString(videoListWithVideofile);
	}

	/**
	 * 특수문자를 제거한 문자 반환
	 * 
	 * @param str
	 * @return string
	 */
	public static String removeSpecialCharacters(String str) {
		String str_imsi = "";
		String[] filter_word = { "", "\\.", "\\?", "\\/", "\\~", "\\!", "\\@", "\\#", "\\$", "\\%", "\\^", "\\&", "\\*", "\\(", "\\)", "\\_", "\\+", "\\=",
				"\\|", "\\\\", "\\}", "\\]", "\\{", "\\[", "\\\"", "\\'", "\\:", "\\;", "\\<", "\\,", "\\>", "\\.", "\\?", "\\/" };
		for (int i = 0; i < filter_word.length; i++) {
			// System.out.println(i + "[" + filter_word[i] + "]");
			str_imsi = str.replaceAll(filter_word[i], "");
			str = str_imsi;
		}
		return str;
	}

	/**
	 * 앞 뒤 공백, [ 제거. null이나 공백이면 "" 리턴
	 * 
	 * @param str
	 * @return string
	 */
	public static String removeUnnecessaryCharacter(String str) {
		return removeUnnecessaryCharacter(str, "");
	}

	/**
	 * 앞 뒤 공백, [ 제거
	 * 
	 * @param str
	 * @param defaultString
	 * @return string
	 */
	public static String removeUnnecessaryCharacter(String str, String defaultString) {
		if (str == null || str.trim().length() == 0)
			return defaultString;

		str = str.trim();
		str = str.startsWith("[") ? str.substring(1) : str;
		return str.trim().length() == 0 ? defaultString : str.trim();
	}

	/**
	 * list를 컴마(,)로 구분한 string반환
	 * 
	 * @param list
	 * @return string of list
	 */
	public static <T> String listToSimpleString(List<T> list) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, e = list.size(); i < e; i++) {
			T object = list.get(i);
			if (i > 0)
				sb.append(", ");
			sb.append(object);
		}
		return sb.toString();
	}

	/**
	 * file list를 컴마(,)로 구분한 string으로 반환
	 * 
	 * @param list
	 * @return string of list
	 */
	public static String listFileToSimpleString(List<File> list) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, e = list.size(); i < e; i++) {
			File file = list.get(i);
			if (i > 0)
				sb.append(", ");
			sb.append(file.getAbsolutePath());
		}
		return sb.toString();
	}

	/**
	 * 문자열 equalsIgnoreCase 비교
	 * <br> compare null is true
	 * <br> compare empty is true
	 * 
	 * @param target
	 * @param compare
	 * @return {@code true} if equals
	 */
	public static boolean equals(String target, String compare) {
		return StringUtils.isBlank(compare) || StringUtils.equalsIgnoreCase(target, compare);
	}

	/**
	 * 문자열 containsIgnoreCase 비교
	 * <br> compare null is true
	 * <br> compare empty is true
	 * 
	 * @param target
	 * @param compare
	 * @return {@code true} if contains
	 */
	public static boolean containsName(String target, String compare) {
		return StringUtils.isBlank(compare) || StringUtils.containsIgnoreCase(target, compare);
	}

	/**
	 * 비디오에 배우가 포함되어 있는지
	 * <br> compare null is true
	 * <br> compare empty is true
	 * 
	 * @param target
	 * @param compare
	 * @return {@code true} if contains
	 */
	public static boolean containsActress(Video target, String compare) {
		return StringUtils.isBlank(compare) || target.containsActress(compare);
	}

	/**
	 * 배우 이름 중 하나라도 비디오에 포함되어 있는지
	 * 
	 * @param video
	 * @param actressNameList
	 * @return {@code true} if contains
	 */
	public static boolean containsActress(Video video, List<String> actressNameList) {
		for (String actress : actressNameList)
			if (containsActress(video, actress))
				return true;
		return false;
	}

	/**
	 * 파일 이름으로 순차 정렬
	 * 
	 * @param files
	 * @return
	 */
	public static List<File> sortFile(List<File> files) {
		Collections.sort(files, new Comparator<File>() {

			@Override
			public int compare(File arg0, File arg1) {
				return StringUtils.compareTo(arg0.getName(), arg1.getName());
			}

		});
		return files;
	}

	/**
	 * <p>
	 * 단독으로 파일명을 재조합한다. 상황과 조건에 따라 로직이 달라지므로 사용시 수정 필요
	 * </P>
	 * 
	 * @param path
	 * @param unclassifiedPath
	 */
	public static void changeOldNameStyle(String path, String unclassifiedPath) {
		Collection<File> found = FileUtils.listFiles(new File(path), null, true);
		int classified = -1;
		for (File file : found) {
			String name = file.getName();
			String filename = FileUtils.getNameExceptExtension(file);
			String extname = FileUtils.getExtension(file);

			// if(ctrl.listBGImageName.equals(name) ||
			// ctrl.historyName.equals(name))
			// continue;

			String[] filenamepart = StringUtils.split(filename, ']');
			// System.out.format("%s%n", ArrayUtils.toString(filenamepart));
			int partCount = filenamepart.length;
			String studio = "", opus = "", title = "NoTitle", actress = "UnKnown", date = "";
			switch (partCount) {
			case 5:
				date = removeUnnecessaryCharacter(filenamepart[4]);
			case 4:
				actress = removeUnnecessaryCharacter(filenamepart[3]);
			case 3:
				title = removeUnnecessaryCharacter(filenamepart[2]);
			case 2:
				opus = removeUnnecessaryCharacter(filenamepart[1]);
				String[] opuspart = StringUtils.splitByCharacterType(opus);
				if (opuspart != null && opuspart.length == 2) {
					opus = opuspart[0].toUpperCase() + "-" + opuspart[1];
				}
				studio = removeUnnecessaryCharacter(filenamepart[0]);
				classified = 0;
				break;
			case 1:
				classified = 1;
				break;
			default:
				classified = 2;
				break;
			}
			studio = "IDEAPOCKET".equals(studio) ? "IdeaPocket" : studio;
			if (title.startsWith("20")) {
				date = title.substring(0, 10);
				title = title.substring(10);
			}
			title = title.replaceAll(opus, "").trim();
			String uniqueName = "Unknown";
			if (title.startsWith(uniqueName)) {
				title = title.replaceAll(uniqueName, "").trim();
				actress = uniqueName;
			}
			if (classified == 0) {
				System.out.format("정리됨 : %s -> [%s][%s][%s][%s][%s].%s%n", name, studio, opus, title, actress, extname, date);
				String newName = MessageFormat.format("[{0}][{1}][{2}][{3}][{5}].{4}", studio, opus, title, actress, extname, date);
				try {
					if (!name.equals(newName)) {
						FileUtils.moveFile(file, new File(path, newName));
						System.out.format("    move : %s -> %s%n", name, newName);
					}
				} catch (IOException e) {
					System.out.format("Error : %s%n", e.getMessage());
				}
			} else if (classified == 1) {
				System.out.format("정리안됨 : %s -> move to %s%n", file.getAbsoluteFile(), unclassifiedPath);
				try {
					FileUtils.moveFileToDirectory(file, new File(unclassifiedPath), true);
				} catch (IOException e) {
					System.out.format("Error : %s%n", e.getMessage());
				}
			} else if (classified == 2) {
				System.out.format("인자가 많음 : %s%n", file.getAbsolutePath());
			} else {
				System.out.format("이건 뭥미 : %s%n", file.getAbsolutePath());
			}
		}
	}

	/**
	 * 비디오 현재 폴더에서 릴리즈 날자를 기준으로 yyyy-mm 형식의 하위 폴더를 만든다.
	 * @param video
	 * @return 생성된 폴더 위치
	 */
	public static String makeSubPathByReleaseDate(Video video) {
		if (!StringUtils.isEmpty(video.getReleaseDate())) {
			File subDir = new File(video.getDelegatePathFile(), StringUtils.substring(video.getReleaseDate(), 0, 7).replace(".", "-"));
			if (!subDir.exists()) {
				subDir.mkdir();
			}
			return subDir.getAbsolutePath();
		}
		return video.getDelegatePath();
	}
	
	public static void main(String[] args) throws Exception {
		// VideoUtils.changeOldNameStyle("E:\\AV_JAP",
		// "E:\\AV_JAP\\unclassified");
		// System.out.println(ArrayUtils.toString(VideoUtils.getGoogleImage("Abigaile")));

		// File dir = new File("E:\\aaa");
		// File[] fs = dir.listFiles();
		// for(File f : fs) {
		// System.out.format("%s -> %s%n", f.getName(),
		// removeSpecialCharacters(f.getName()));
		// }

		// ObjectOutputStream 이용한 내용 저장
		/*
		 * File infoFile = new File("/home/kamoru/ETC/info.sample");
		 * List<String> logList = new ArrayList<String>();
		 * logList.add("play 1"); logList.add("play 2"); Map<String, Object>
		 * data = new HashMap<String, Object>(); data.put("text",
		 * "overview text 코멘트"); data.put("rank", new Integer(3));
		 * data.put("log", logList); ObjectOutputStream os = new
		 * ObjectOutputStream(new FileOutputStream(infoFile));
		 * os.writeObject(data); os.flush(); os.close();
		 * 
		 * // ObjectInputStream 이용한 내용 읽기 ObjectInputStream is = new
		 * ObjectInputStream(new FileInputStream(infoFile)); Map<String, Object>
		 * dataR = (HashMap<String, Object>) is.readObject(); is.close();
		 * System.out.println(dataR.get("text"));
		 * System.out.println(dataR.get("rank")); System.out.println(
		 * ArrayUtils.toString( ((ArrayList<String>)dataR.get("log")) ) );
		 */

		JSONObject json = JSONObject.fromObject(FileUtils.readFileToString(new File("/home/kamoru/ETC/info.json.sample")));
		JSONObject infoData = json.getJSONObject("info");

		System.out.println("opus : " + infoData.getString("opus"));
		System.out.println("rank : " + infoData.getString("rank"));
		System.out.println("txt  : " + infoData.getString("txt"));

		JSONArray hisArray = infoData.getJSONArray("history");

		for (int i = 0, e = hisArray.size(); i < e; i++) {
			String str = hisArray.getString(i); // results.getJSONObject(i).getString("url");
			System.out.println("his  : " + str);
		}

		JSONObject root = new JSONObject();

		JSONObject info = new JSONObject();

		info.put("opus", "IPZ-011");

		info.put("rank", "3");

		info.put("txt", "텍스트\nasvfd");

		JSONArray his = new JSONArray();
		his.add("PLAY-1");
		his.add("PLAY-2");

		info.put("history", his);

		root.put("info", info);

		System.out.println("---" + root.toString());

	}

}
