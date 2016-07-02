package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jamesanton.cruncher.data.FileSorter;
import com.jamesanton.cruncher.util.FileUtil;

public class FileSorterTest {
	private FileSorter f = new FileSorter();
	private static final Logger LOG = Logger.getLogger(FileSorterTest.class);
	private static final String FILE_A_PATH = "in.txt";
	private static final Long FILE_A_START = 0L;
	private static final int TEST_FILE_NUM_LINES = 10000000;
	private File bigFile = null;
	private File out = null;
	
	@Before
	public void setup() throws IOException{
		BigFileCreator bfc = new BigFileCreator();
		bigFile = bfc.createFileWithRandomNumbersAsLines(FILE_A_PATH, FILE_A_START, FILE_A_START + TEST_FILE_NUM_LINES);
	}	
	
	@After
	public void teardown(){
		try {
			FileUtil.removeFilesAndFolder(bigFile.getAbsolutePath());
		} catch (IOException e) {
			LOG.error(e);
		}
		try {
			FileUtil.removeFilesAndFolder(out.getAbsolutePath());
		} catch (IOException e) {
			LOG.error(e);
		}
	}
	
	@Test
	public void verifyFileSorterWorking() throws FileNotFoundException, IOException {	
		try {
			out = f.sortFile(bigFile, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return (Integer.valueOf(o1)).compareTo(Integer.valueOf(o2));
				}
			});
		} catch (IOException e) {			
			LOG.error(e);
		}
		Assert.assertEquals(bigFile.getTotalSpace(), out.getTotalSpace());
		
		// Read the file line by line, verifying that each line is either
		// greater than or equal to the previous line
		String previous = null;
		try (BufferedReader br = new BufferedReader(new FileReader(out))) {
			for (String line; (line = br.readLine()) != null;) {
				if (previous != null) {
					Assert.assertTrue(Integer.parseInt(line) >= Integer
							.parseInt(previous));
				}
				previous = line;
			}
		}
	}
}
