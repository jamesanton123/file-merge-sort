package data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * This is just a small utility to create files with random numbers as the lines
 * in the file, and can create small or very large files on the file system.
 * 
 * @author James
 *
 */
public class BigFileCreator {
	private static final Logger LOG = Logger.getLogger(BigFileCreator.class);

	/**
	 * Creates a new file at the specified path, where each of the rows in the
	 * file is a random number between 1 and the endIndex.
	 * 
	 * @param path
	 * @param startIndex
	 * @param endIndex
	 * @throws IOException 
	 */
	public File createFileWithRandomNumbersAsLines(String name, long startIndex, long endIndex) throws IOException {
		LOG.info("Begin creating big random number line file");
		File file = File.createTempFile(name, null);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		try {
			for (long i = startIndex; i < endIndex; i++) {
				if(i % 100000 == 0) LOG.info(i + " of " + endIndex);
				bw.write(Integer.toString(getRandomNumber(1, endIndex)));
				bw.write("\n");
			}
		} catch (IOException e) {
			LOG.error("Error when creating large test file");
		} finally{
			bw.close();
		}

		long fileSizeInBytes = file.length();
		long fileSizeInKB = fileSizeInBytes / 1024;
		long fileSizeInMB = fileSizeInKB / 1024;
		LOG.info("File size in megabytes: " + fileSizeInMB);
		return file;
	}
	
	private int getRandomNumber(long min, long max) {
		return (int) (Math.random() * max + min);
	}
}
