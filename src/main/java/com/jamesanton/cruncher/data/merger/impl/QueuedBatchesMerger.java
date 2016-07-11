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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import com.jamesanton.cruncher.data.merger.FileMerger;
import com.jamesanton.cruncher.data.merger.MergerException;
import com.jamesanton.cruncher.util.FileUtil;

public class QueuedBatchesMerger implements FileMerger {
	private static final Logger LOG = Logger.getLogger(QueuedBatchesMerger.class);
	private List<Batch> batchList;
	private Comparator<String> lineComparator;
	private File inputFolder;
	private static final int MAX_NUM_FILES_IN_BATCH = 10;
	private int batchId = 0;
	
	public QueuedBatchesMerger(Comparator<String> lineComparator,
			File inputFolder) {
		this.lineComparator = lineComparator;
		this.inputFolder = inputFolder;
	}

	/**
	 * Merges batches of sorted files recursively 
	 * until there is only one sorted file remaining
	 * 
	 * Returns the sorted file
	 * 
	 * @throws IOException
	 */
	public File merge() throws MergerException {
		batchList = new ArrayList<Batch>();
		File[] inputFiles = inputFolder.listFiles();
		Batch b = null;
		for (File f : inputFiles) {
			// Create a new batch if we need to, and add it to the batch list
			if (b == null || b.isFull()) {
				b = new Batch(MAX_NUM_FILES_IN_BATCH, batchId);
				batchList.add(b);
				batchId++;
			}
			b.addFile(f);
		}
		
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		for(int i = 0; i < batchList.size(); i++){
			MergeBatchTask task = new MergeBatchTask(batchList.get(i));
			System.out.println("A new merge batch task has been added : " + i);
			executor.execute(task);
		}
		while (executor.getTaskCount() != executor.getCompletedTaskCount()){		    
		    try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		executor.shutdown();

		// Threads finished
		LOG.info("Threads finished");
		
		// Since each batch should produce one output file, and delete its original files
		validateThereIsOneFileForEachItemInBatchList(batchList);
		
		if(batchList.size() == 1){			
			return inputFolder.listFiles()[0];
		}else{
			// Recursive call to merge
			return merge();
		}		
	}

	private void validateThereIsOneFileForEachItemInBatchList(List<Batch> batchList) throws MergerException {
		int numFilesFound = inputFolder.listFiles().length;
		int expectedNumFiles = batchList.size();
		if(numFilesFound != expectedNumFiles){
			throw new MergerException("After merging files, batch list was the wrong size. "
					+ "Found " + numFilesFound + " files but expected " + expectedNumFiles + " files. Please check logs.");
		}		
	}

	class MergeBatchTask implements Runnable {
		private Batch batch;

		public MergeBatchTask(Batch batch) {
			this.batch = batch;
		}

		@Override
		public void run() {
			// Read and merge the files in the batch to a new file
			try {
				mergeBatchFiles(batch);
				batch.deleteBatchFiles();
			} catch (MergerException e1) {
				LOG.error("Couldn't merge batch", e1);
			} catch (IOException e) {
				LOG.error("Couldn't delete a batch file");
			}			
		}
	}
	
	public class Batch {
		private List<File> files = new ArrayList<File>();
		private int maxSize;
		private int batchIndex;

		public Batch(int maxSize, int batchIndex) {
			this.maxSize = maxSize;
			this.batchIndex = batchIndex;
		}

		public void addFile(File f) {
			files.add(f);
		}

		public boolean isFull() {
			return size() == maxSize;
		}

		public int size() {
			return files.size();
		}
		
		public void deleteBatchFiles() throws IOException{
			for(int i = 0; i < files.size(); i++){
				FileUtils.forceDelete(files.get(i));
			}
		}
		
		public int getBatchIndex(){
			return batchIndex;
		}
	}
	
	private void mergeBatchFiles(Batch batch) throws MergerException{
		File out = new File(inputFolder.getAbsolutePath() + File.separator + batch.getBatchIndex());
		FileUtil.createFileIfNotExists(out);
		LOG.info("Begin merging");
		List<BufferedReader> bufferedReaders = new ArrayList<BufferedReader>();
		BufferedWriter bw = null;
		try{
			// Create a buffered reader for each of the files in the batch
			for (File in : batch.files) {
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
