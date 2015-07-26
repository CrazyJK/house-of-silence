package jk.kamoru.crazy;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ForceDelete {

	public static void main(String[] args) throws IOException {
		String path = "E:\\Movies\\iMovie 보관함.imovielibrary";
		FileUtils.deleteDirectory(new File(path));

	}

}
