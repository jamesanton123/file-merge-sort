package com.jamesanton.cruncher.data.splitter.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.jamesanton.cruncher.data.splitter.FileSplitter;
import com.jamesanton.cruncher.data.splitter.SplitterException;
import com.jamesanton.cruncher.util.FileUtil;

public class MaxLinesFileSplitter implements FileSplitter {

	private static final Logger LOG = Logger
			.getLogger(MaxLinesFileSplitter.class);
	private int maxLines;
	private File inputFile;
	private File outputFolder;

	public MaxLinesFileSplitter(int maxLines, File inputFile, File outputFolder) {
		this.maxLines = maxLines;
		this.inputFile = inputFile;
		this.outputFolder = outputFolder;
	}

	@Override
	public void split() throws SplitterException {
		LOG.info("Begin splitting your file");
		BufferedWriter bw = null;
		String line;
		// Create output folder for our files
		outputFolder.mkdirs();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(inputFile));
		} catch (FileNotFoundException e1) {
			throw new SplitterException("Could not find the file that was provided to the constructor.");
		}
		try {
			long numLinesWrittenInFile = 0;
			long numFilesCreated = 0;
			File smallFile;
			while ((line = br.readLine()) != null) {
				// Create a new file if we need to
				if (numLinesWrittenInFile == 0) {
					if (bw != null) {
						try {
							bw.close();
						} catch (IOException e) {
							LOG.error(
									"Couldn't close buffered writer when splitting up the files",
									e);
						}
					}
					smallFile = FileUtil.createFileIfNotExists(outputFolder
							+ File.separator + "part" + (numFilesCreated + 1));
					bw = new BufferedWriter(new FileWriter(smallFile));
					numFilesCreated++;
				}
				// Write the line
				bw.write(line);
				bw.write("\n");
				// Increment accordingly
				numLinesWrittenInFile++;
				if (numLinesWrittenInFile == maxLines) {
					numLinesWrittenInFile = 0;
				}
			}
		} catch (IOException e) {
			throw new SplitterException("Had difficulty reading a line while splitting the file.");
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					LOG.error("Couldn't close buffered writer", e);
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					LOG.error("Couldn't close buffered reader", e);
				}
			}
		}
	}

}
