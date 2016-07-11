package com.jamesanton.cruncher.data.merger.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import com.jamesanton.cruncher.data.merger.FileMerger;
import com.jamesanton.cruncher.data.merger.MergerException;
import com.jamesanton.cruncher.util.FileUtil;

public class OneToOneFileTOBufferedReaderMerger implements FileMerger{
	private static final Logger LOG = Logger.getLogger(OneToOneFileTOBufferedReaderMerger.class);
	private Comparator<String> lineComparator;
	private File inputFolder;
	
	/**
	 * Constructs an instance of this type of merger.
	 * @param lineComparator
	 * @param inputFolder Must contain files already sorted using the same comparator
	 * @param outFile
	 */
	public OneToOneFileTOBufferedReaderMerger(Comparator<String> lineComparator, File inputFolder){
		this.lineComparator = lineComparator;
		this.inputFolder = inputFolder;
	}
	
	/**
	 * Merges the sorted small files back together
	 * 
	 * @throws IOException
	 */
	public File merge() throws MergerException {
		LOG.info("Begin merging");
		File out = FileUtil.createFileIfNotExists("out");
		List<BufferedReader> bufferedReaders = new ArrayList<BufferedReader>();
		BufferedWriter bw = null;
		try{			
			// Create a buffered reader for each of the sorted files
			for (File in : inputFolder.listFiles()) {
				bufferedReaders.add(new BufferedReader(new FileReader(in.getAbsolutePath())));			
			}
			LOG.info("num buffered readers = " + bufferedReaders.size());						
			// Set the initial bufferTops
			String[] buffersTop = new String[bufferedReaders.size()];
			for (int i = 0; i < bufferedReaders.size(); i++) {
				buffersTop[i] = bufferedReaders.get(i).readLine();
			}
			int index = -1;			
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
		} catch (IOException e) {
			throw new MergerException("Exception while merging", e);
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
	
	/**
	 * Finds the index of the smallest item in an array.
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
}
