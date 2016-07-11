package com.jamesanton.cruncher.data.splitter.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.log4j.Logger;

import com.jamesanton.cruncher.data.splitter.FileSplitter;
import com.jamesanton.cruncher.data.splitter.SplitterException;
import com.jamesanton.cruncher.util.FileUtil;

/**
 * This is not a good scalable solution, because even after splitting the large 
 * file into a fixed number of smaller files, the smaller files might still
 * be too large to fit into memory when sorting them.
 * @author James
 *
 */
@Deprecated
public class FixedNumFilesFileSplitter implements FileSplitter {

	private static final Logger LOG = Logger
			.getLogger(FixedNumFilesFileSplitter.class);
	private long numLinesInInputFile;
	private int numFiles;
	private File inputFile;
	private File outputFolder;
	private long maxLinesPerFile;
	
	@Deprecated
	public FixedNumFilesFileSplitter(int numFiles, File inputFile, File outputFolder) {
		this.numFiles = numFiles;
		this.inputFile = inputFile;
		this.outputFolder = outputFolder;
		this.numLinesInInputFile = getNumLinesInInputFile();
		maxLinesPerFile = getMaxLinesPerFile();
	}

	private long getMaxLinesPerFile() {
		double linesPerFileNeeded = numLinesInInputFile / (double)numFiles;
		return (long) Math.ceil(linesPerFileNeeded);
	}

	private long getNumLinesInInputFile() {
		try {
			return Files.lines(inputFile.toPath()).count();
		} catch (IOException e) {
			LOG.error(e);
		}
		return -1;
	}

	@Deprecated
	@Override
	public void split() throws SplitterException {
		LOG.info("Begin splitting your file");
		BufferedWriter bw = null;
		String line;
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
					closeWriter(bw);
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
				if (numLinesWrittenInFile == maxLinesPerFile) {
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

	
	private void closeWriter(BufferedWriter bw) {
		if (bw != null) {
			try {
				bw.close();
			} catch (IOException e) {
				LOG.error(
						"Couldn't close buffered writer when splitting up the files",
						e);
			}
		}
		
	}

}
