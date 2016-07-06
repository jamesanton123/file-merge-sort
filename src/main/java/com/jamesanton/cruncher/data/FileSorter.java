package com.jamesanton.cruncher.data;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.jamesanton.cruncher.data.merger.FileMerger;
import com.jamesanton.cruncher.data.merger.MergerException;
import com.jamesanton.cruncher.data.merger.impl.OneToOneFileTOBufferedReaderMerger;
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
public class FileSorter {
	private static final String FILE_SPLITTER_OUTPUT_PATH = "brokenUpFiles";
	private static final String SORTED_PATH = "sortedSmallFilesPath";
	private static final String OUT_FILE = "out";
	private static final Logger LOG = Logger.getLogger(FileSorter.class);
	
	public File sortFile(File inFile, Comparator<String> lineComparator) throws IOException, SplitterException, MergerException {
		File out = null;
		FileSplitter fileSplitter = new MaxLinesFileSplitter(10000000, inFile, new File(FILE_SPLITTER_OUTPUT_PATH));
		FileMerger fileMerger = new OneToOneFileTOBufferedReaderMerger(lineComparator, new File(SORTED_PATH));
		FileUtil.removeFilesAndFolder(FILE_SPLITTER_OUTPUT_PATH, SORTED_PATH, OUT_FILE);	
		fileSplitter.split();
		sortSmallFiles(lineComparator);
		out = fileMerger.merge();	
		FileUtil.removeFilesAndFolder(FILE_SPLITTER_OUTPUT_PATH, SORTED_PATH);		
		return out;
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
		for (File f : (new File(FILE_SPLITTER_OUTPUT_PATH).listFiles())) {
			List<String> lines = FileUtils.readLines(f);
			Collections.sort(lines, lineComparator);
			File newFile = new File(SORTED_PATH + File.separator + f.getName());
			FileUtils.writeLines(newFile, lines);
		}
	}

	
}
