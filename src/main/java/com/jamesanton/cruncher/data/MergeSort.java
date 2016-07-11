package com.jamesanton.cruncher.data;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.jamesanton.cruncher.data.merger.FileMerger;
import com.jamesanton.cruncher.data.merger.MergerException;
import com.jamesanton.cruncher.data.merger.impl.OneToOneFileTOBufferedReaderMerger;
import com.jamesanton.cruncher.data.sorter.FileSorter;
import com.jamesanton.cruncher.data.sorter.SorterException;
import com.jamesanton.cruncher.data.sorter.impl.SequenceFileSorter;
import com.jamesanton.cruncher.data.splitter.FileSplitter;
import com.jamesanton.cruncher.data.splitter.SplitterException;
import com.jamesanton.cruncher.data.splitter.impl.MaxLinesFileSplitter;
import com.jamesanton.cruncher.util.FileUtil;

/**
 * An implementation of an external hybrid merge sort on a file
 * 
 * @param f
 * @param lineComparator
 */
public class MergeSort {
	private static final String FILE_SPLITTER_OUTPUT_PATH = "brokenUpFiles";
	private static final String SORTED_PATH = "sortedSmallFilesPath";
	private static final String OUT_FILE = "out";
	private static final Logger LOG = Logger.getLogger(MergeSort.class);
	
	public File sortFile(File inFile, Comparator<String> lineComparator) throws IOException, SplitterException, MergerException, SorterException {
		File out = null;
		
		File fileSplitterOutputFolder = new File(FILE_SPLITTER_OUTPUT_PATH);
		File fileSorterOutputFolder = new File(SORTED_PATH);
		
		FileSplitter fileSplitter = new MaxLinesFileSplitter(10000000, inFile, fileSplitterOutputFolder);
		
		FileSorter fileSorter = new SequenceFileSorter(lineComparator, fileSplitterOutputFolder, fileSorterOutputFolder);
		
		FileMerger fileMerger = new OneToOneFileTOBufferedReaderMerger(lineComparator, new File(SORTED_PATH));
		
		FileUtil.removeFilesAndFolder(FILE_SPLITTER_OUTPUT_PATH, SORTED_PATH, OUT_FILE);
		
		fileSplitter.split();
		fileSorter.sort();
		out = fileMerger.merge();
		
		FileUtil.removeFilesAndFolder(FILE_SPLITTER_OUTPUT_PATH, SORTED_PATH);		
		return out;
	}

	

	

	

	
}
