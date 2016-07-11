package com.jamesanton.cruncher.data.merger.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.jamesanton.cruncher.data.merger.FileMerger;
import com.jamesanton.cruncher.data.merger.MergerException;

public class QueuedBatchesMerger implements FileMerger{

	private Queue<Batch> batchQueue = new LinkedList<Batch>();
	private Comparator<String> lineComparator;
	private File inputFolder;
	private static final int MAX_NUM_FILES_IN_BATCH = 10;
	
	public QueuedBatchesMerger(Comparator<String> lineComparator, File inputFolder){
		this.lineComparator = lineComparator;
		this.inputFolder = inputFolder;
		batchTheFilesAndAddBatchesToQueue();
	}
	
	private void batchTheFilesAndAddBatchesToQueue() {
		File[] inputFiles = inputFolder.listFiles();
		Batch b = null;
		for (File f : inputFiles) {
			// Create a new batch if we need to, and add it to the queue
			if(b == null || b.isFull()){
				 b = new Batch(MAX_NUM_FILES_IN_BATCH);
				 batchQueue.add(b);
			}
			b.addFile(f);
		}		
	}

	/**
	 * Merges the sorted small files back together
	 * 
	 * @throws IOException
	 */
	public File merge() throws MergerException {
		throw new MergerException("un implemented");
	}
	
	public class Batch{
		private List<File> files = new ArrayList<File>();
		private int maxSize;
		
		public Batch(int maxSize){
			this.maxSize = maxSize;
		}
		
		public void addFile(File f){
			files.add(f);
		}
		
		public boolean isFull(){
			return size() == maxSize;
		}
		
		public int size(){
			return files.size();
		}
	}
}
