package com.jamesanton.cruncher.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.jamesanton.cruncher.util.FileUtil;
import com.jamesanton.cruncher.util.RandomUtil;

/**
 * This is just a small utility to create files with random numbers as the lines
 * in the file, and can create small or very large files on the file system.
 * 
 * @author James
 *
 */
public class BigFileCreator {
	/**
	 * Creates a new file at the specified path, where each of the rows in the
	 * file is a random number between 1 and the endIndex.
	 * 
	 * @param path
	 * @param startIndex
	 * @param endIndex
	 */
	public File createFileWithRandomNumbersAsLines(String name, long startIndex, long endIndex) {
		File file = null;
		try {
			file = File.createTempFile(name, null);
			file.deleteOnExit();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try (FileOutputStream fop = new FileOutputStream(file)) {
			String outString = "";
			byte[] bytes = null;
			for (long i = startIndex; i < endIndex; i++) {
				outString = Integer.toString(RandomUtil.getRandomNumber(1, endIndex)) + "\n";
				bytes = outString.getBytes();
				fop.write(bytes);
				fop.flush();
			}
			fop.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}



	

}
