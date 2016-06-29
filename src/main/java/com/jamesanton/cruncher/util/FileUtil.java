package com.jamesanton.cruncher.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {
	
	/**
	 * Creates a file if it doesn't exist.
	 * Then returns the file specified at the path.
	 * @param path
	 * @return
	 */
	public static File createFileIfNotExists(String path) {
		File f = new File(path);
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}
}
