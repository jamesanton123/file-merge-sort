package com.jamesanton.cruncher.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.jamesanton.cruncher.util.FileUtil;

/**
 * An implementation of an external hybrid merge sort on a file
 * @param f
 * @param lineComparator
 */
public class FileSorter {
	private int numFilesToBreakInto;
	private int maximumNumberOfLinesPerFile = 10000;

	
	public File sortFile(File inFile, Comparator<String> lineComparator) {
		File out = null;
		String tempDir = FileUtils.getTempDirectory().getAbsolutePath();
		String brokenUpPath = tempDir + File.separator + "brokenUpFiles";
		String sortedPath = tempDir + File.separator + "sortedSmallFilesPath";
		String outFile = tempDir + File.separator + "out";
			
		breakUpFile(inFile, brokenUpPath);
		sortSmallFiles(brokenUpPath, sortedPath, lineComparator);
		try {
			out = mergeSortedFiles(sortedPath, outFile, lineComparator);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out;
	}

	/**
	 * Merges the sorted small files back together
	 * @throws IOException 
	 */
	private File mergeSortedFiles(String sortedPath, String outFile, Comparator<String> lineComparator) throws IOException {
		File out = FileUtil.createFileIfNotExists(outFile);
		List<BufferedReader> buffers = new ArrayList<BufferedReader>();
		List<String> bufferTop = new ArrayList<String>(buffers.size());
		
		// Create a buffered reader for each of the sorted files
		for(File in : (new File(sortedPath).listFiles())){
			buffers.add(new BufferedReader(new FileReader(in.getAbsolutePath())));
		}
		
		// At this point each buffer is sitting on a file waiting to drain it
		
		// Set the initial bufferTops
		for(int i = 0; i < buffers.size(); i++){
			bufferTop.add(i, buffers.get(i).readLine());
		}
		int index = -1;
		FileWriter fw = new FileWriter(out);
		while(bufferTop.size() > 0){
			// Find the index of the first item after its sorted
			index = getLowestItemIndex(bufferTop, lineComparator);
			// Write the line from that buffertop index to the file
			String value = bufferTop.get(index);
			fw.write(value + "\n");
			
			// Pop the next string from the buffer at that index into the buffertop
			String nextVal = buffers.get(index).readLine();
			
			if(nextVal != null){
				bufferTop.set(index, nextVal);
			}else{
				// Remove the index of the null buffertop from the buffertop list 
				// and remove its asssociated bufferedreader from the buffers list
				bufferTop.remove(index);
				buffers.remove(index);
			}
		}
		fw.close();
		return out;
	}

	private int getLowestItemIndex(List<String> bufferTop, Comparator<String> lineComparator) {
		List<String> clone = new ArrayList<String>(bufferTop);
		Collections.sort(clone, lineComparator);
		return bufferTop.indexOf(clone.get(0));
	}

	/**
	 * Sorts
	 * @param inPath
	 * @param outPath
	 */
	private void sortSmallFiles(String inPath, String outPath, Comparator<String> lineComparator) {
		(new File(outPath)).mkdirs();
		for (File f : (new File(inPath).listFiles())) {
			List<String> lines = null;
			try {
				lines = FileUtils.readLines(f);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Collections.sort(lines, lineComparator);
			try {
				File newFile = new File(outPath + File.separator + f.getName());
				newFile.deleteOnExit();
				FileUtils.writeLines(newFile, lines);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	

	/**
	 * Breaks up a file into smaller files
	 * 
	 * @param f
	 * @param brokenUpFilePath 
	 */
	private void breakUpFile(File f, String brokenUpFilePath) {
		FileOutputStream fos = null;
		String line;
		// Create new broken up file path
		(new File(brokenUpFilePath)).mkdirs();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			long numLinesWrittenInFile = 0;
			long numFilesCreated = 0;
			File smallFile;
			byte[] bytes = null;
			while ((line = br.readLine()) != null) {
				// Create a new file if we need to
				if (numLinesWrittenInFile == 0) {
					smallFile = FileUtil.createFileIfNotExists(brokenUpFilePath + File.separator + "part" + (numFilesCreated + 1) + ".txt");
					smallFile.deleteOnExit();
					fos = new FileOutputStream(smallFile);
					numFilesCreated++;
				}
				// Write the line
				line += "\n";
				bytes = line.getBytes();
				fos.write(bytes);
				fos.flush();
				// Increment accordingly
				numLinesWrittenInFile++;
				if (numLinesWrittenInFile == maximumNumberOfLinesPerFile) {
					numLinesWrittenInFile = 0;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
