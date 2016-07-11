package com.jamesanton.cruncher.data.sorter.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.jamesanton.cruncher.data.sorter.FileSorter;
import com.jamesanton.cruncher.data.sorter.SorterException;

public class SequenceFileSorter implements FileSorter{
	private static final Logger LOG = Logger.getLogger(SequenceFileSorter.class);
	private Comparator<String> lineComparator;
	private File sourceFolder;
	private File outputFolder;
	
	public SequenceFileSorter(Comparator<String> lineComparator, File sourceFolder, File outputFolder) {
		this.lineComparator = lineComparator;
		this.sourceFolder = sourceFolder;
		this.outputFolder = outputFolder;
	}
	
	/**
	 * Sorts each of the files in the folder
	 * 
	 * @param inPath
	 * @param outPath
	 * @throws IOException
	 */
	@Override
	public void sort() throws SorterException {
		LOG.info("Begin sorting your " + sourceFolder.listFiles().length + " files");
		sourceFolder.mkdirs();
		for (File f : (sourceFolder.listFiles())) {
			List<String> lines = null;
			try {
				lines = FileUtils.readLines(f);
			} catch (IOException e) {
				throw new SorterException("Couldn't read lines while sorting", e);
			}
			Collections.sort(lines, lineComparator);
			File newFile = new File(outputFolder.getAbsolutePath() + File.separator + f.getName());
			try {
				FileUtils.writeLines(newFile, lines);
			} catch (IOException e) {
				throw new SorterException("Couldn't write lines while sorting", e);
			}
		}
	}
}
