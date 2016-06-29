package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;

import org.junit.Assert;
import org.junit.Test;

import com.jamesanton.cruncher.data.BigFileCreator;
import com.jamesanton.cruncher.data.FileSorter;


public class FileSorterTest {
	private static final Long FILE_SIZE = 1000000L;
	private static final String FILE_A_PATH = "in.txt";
	private static final Long FILE_A_START = 0L;
	private static final Long FILE_A_END = FILE_A_START + FILE_SIZE;
	
	@Test
	public void verifyFileSorterWorking(){
		BigFileCreator bfc = new BigFileCreator();		
		File bigFile = bfc.createFileWithRandomNumbersAsLines(FILE_A_PATH, FILE_A_START, FILE_A_END);
		
		FileSorter f = new FileSorter();
		File out = f.sortFile(bigFile, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return (Integer.valueOf(o1)).compareTo(Integer.valueOf(o2));
			}
		});
		Assert.assertEquals(bigFile.getTotalSpace(), out.getTotalSpace());
		out.deleteOnExit();
		// Read the file line by line, verifying that each line is either greater than or equal to the previous line
		String previous = null;
		try(BufferedReader br = new BufferedReader(new FileReader(out))) {
		    for(String line; (line = br.readLine()) != null; ) {
		    	if(previous != null){
		    		Assert.assertTrue(Integer.parseInt(line) >= Integer.parseInt(previous));
		    	}
		    	previous = line;
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
