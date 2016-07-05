package com.jamesanton.cruncher.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import com.jamesanton.cruncher.util.FileUtil;

/**
 * An implementation of an external hybrid merge sort on a file
 * 
 * @param f
 * @param lineComparator
 */
public class FileSorter {
	private static final int MAX_LINES_PER_SMALL_FILE = 100000;
	private static final String BROKEN_UP_PATH = "brokenUpFiles";
	private static final String SORTED_PATH = "sortedSmallFilesPath";
	private static final String OUT_FILE = "out";
	private static final Logger LOG = Logger.getLogger(FileSorter.class);

	public File sortFile(File inFile, Comparator<String> lineComparator)
			throws IOException {
		File out = null;
		try {
			FileUtil.removeFilesAndFolder(BROKEN_UP_PATH, SORTED_PATH, OUT_FILE);
			breakUpFile(inFile);
			sortSmallFiles(lineComparator);
			out = mergeSortedFiles(lineComparator);			
			FileUtil.removeFilesAndFolder(BROKEN_UP_PATH, SORTED_PATH);
		} catch (IOException e) {
			LOG.error("There was a problem sorting your file...", e);
		}
		return out;
	}

	/**
	 * Clones an array, sorts the clone, binary searches the 
	 * original array for index of the first value in the cloned list
	 * with the given comparator
	 * @param bufferTop
	 * @param lineComparator
	 * @return
	 */
	private int getLowestItemIndex(String[] buffersTop, Comparator<String> lineComparator) {
		int lowestIndex = 0;
		for(int i = 1; i < buffersTop.length; i++){
			if(lineComparator.compare(buffersTop[i], buffersTop[lowestIndex]) < 0){
				lowestIndex = i;
			}
		}
		return lowestIndex;
	}

	/**
	 * Breaks up a file into smaller files
	 * 
	 * @param f
	 * @param brokenUpFilePath
	 * @throws IOException
	 */
	private void breakUpFile(File f)
			throws IOException {
		LOG.info("Begin splitting your file");
		BufferedWriter bw = null;
		String line;
		// Create new broken up file path
		(new File(BROKEN_UP_PATH)).mkdirs();
		BufferedReader br = new BufferedReader(new FileReader(f));
		try {
			long numLinesWrittenInFile = 0;
			long numFilesCreated = 0;
			File smallFile;
			while ((line = br.readLine()) != null) {
				// Create a new file if we need to
				if (numLinesWrittenInFile == 0) {
					if(bw != null){
						try {				
							bw.close();
						} catch (IOException e) {
							LOG.error("Couldn't close buffered writer when splitting up the files", e);
						}
					}
					smallFile = FileUtil.createFileIfNotExists(BROKEN_UP_PATH + File.separator + "part" + (numFilesCreated + 1));
					bw = new BufferedWriter(new FileWriter(smallFile));
					numFilesCreated++;
				}
				// Write the line
				bw.write(line);
				bw.write("\n");
				// Increment accordingly
				numLinesWrittenInFile++;
				if (numLinesWrittenInFile == MAX_LINES_PER_SMALL_FILE) {
					numLinesWrittenInFile = 0;
				}
			}
		} finally {
			if(bw != null){
				try {				
					bw.close();
				} catch (IOException e) {
					LOG.error("Couldn't close buffered writer", e);
				}
			}
			if(br != null){
				try {					
					br.close();									
				} catch (IOException e) {
					LOG.error("Couldn't close buffered reader", e);
				}
			}
		}
	}

	/**
	 * Sorts each of the small files
	 * 
	 * @param inPath
	 * @param outPath
	 * @throws IOException
	 */
	private void sortSmallFiles(Comparator<String> lineComparator) throws IOException {
		LOG.info("Begin sorting smaller files");
		(new File(SORTED_PATH)).mkdirs();
		for (File f : (new File(BROKEN_UP_PATH).listFiles())) {
			List<String> lines = FileUtils.readLines(f);
			Collections.sort(lines, lineComparator);
			File newFile = new File(SORTED_PATH + File.separator + f.getName());
			FileUtils.writeLines(newFile, lines);
		}
	}

	/**
	 * Merges the sorted small files back together
	 * 
	 * @throws IOException
	 */
	private File mergeSortedFiles(Comparator<String> lineComparator) throws IOException {
		LOG.info("Begin merging");
		File out = FileUtil.createFileIfNotExists(OUT_FILE);
		List<BufferedReader> bufferedReaders = new ArrayList<BufferedReader>();
		// Create a buffered reader for each of the sorted files
		for (File in : (new File(SORTED_PATH).listFiles())) {
			bufferedReaders.add(new BufferedReader(new FileReader(in.getAbsolutePath())));			
		}
		LOG.info("num buffered readers = " + bufferedReaders.size());
		// Set the initial bufferTops
		String[] buffersTop = new String[bufferedReaders.size()];
		for (int i = 0; i < bufferedReaders.size(); i++) {
			buffersTop[i] = bufferedReaders.get(i).readLine();
		}
		int index = -1;
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(out));
			LOG.info("Begin merging loop");
			while (buffersTop.length > 0) {
				// Find the index of the first item after its sorted
				index = getLowestItemIndex(buffersTop, lineComparator);				
				bw.write(buffersTop[index]);
				bw.write("\n");
				
				// Pop the next string from the buffered reader at that index 
				String nextVal = bufferedReaders.get(index).readLine();
				if (nextVal != null) {
					// Put it into the buffersTop at that index
					buffersTop[index] = nextVal;
				} else {
					//  What to do if the buffered reader runs out of data? Close it and remove it from the list.
					buffersTop = ArrayUtils.removeElement(buffersTop, buffersTop[index]);
					bufferedReaders.get(index).close();
					bufferedReaders.remove(index);
				}
			}
		}finally{
			try{
				bw.close();
			}catch(IOException e){
				LOG.error("Couldn't close buffered writer while merging.", e);
			}
			// Make sure the buffered readers were closed even if we had an error
			for (BufferedReader br : bufferedReaders) {
				try{
					br.close();
				}catch(IOException e){
					LOG.error("Couldn't close buffered reader while merging.", e);
				}	
			}
		}		
		return out;
	}
}
