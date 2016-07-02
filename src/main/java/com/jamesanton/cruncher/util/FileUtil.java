package com.jamesanton.cruncher.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class FileUtil {
	private static final Logger LOG = Logger.getLogger(FileUtil.class);
	
	/**
	 * Creates a file if it doesn't exist.
	 * Then returns the file specified at the path.
	 * @param path
	 * @return
	 */
	public static File createFileIfNotExists(String path) {
		File f = new File(path);
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				LOG.error("Could not create new file.", e);
			}
		}		
		return f;
	}
	
	/**
	 * A simple file destroyer
	 * @param paths
	 * @throws IOException
	 */
	public static void removeFilesAndFolder(String... paths) throws IOException {
		for (String path : paths) {
			File f = new File(path);
			if (f != null && f.exists()) {
				FileUtils.forceDelete(f);
			}
		}
	}
}
