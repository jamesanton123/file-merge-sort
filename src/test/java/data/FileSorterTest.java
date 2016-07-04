package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.jamesanton.cruncher.data.FileSorter;
import com.jamesanton.cruncher.util.FileUtil;

@RunWith(Parameterized.class)
public class FileSorterTest {
	private FileSorter f = new FileSorter();
	private static final Logger LOG = Logger.getLogger(FileSorterTest.class);
	private static final String FILE_A_PATH = "in.txt";
	private static final Long FILE_A_START = 0L;
	private File bigFile = null;
	private File out = null;
	private int fileSizeInLines;
	
	@Parameters
	public static Collection<Object[]> data() {
		Collection<Object[]> collection = new ArrayList<Object[]>();
		collection.add(new Object[]{1000});
		collection.add(new Object[]{10000});
		collection.add(new Object[]{100000});
		collection.add(new Object[]{5000000});
		collection.add(new Object[]{10000000});
		return collection;
	}
	
	public FileSorterTest(int fileSizeInLines) {
        this.fileSizeInLines = fileSizeInLines;
    }
	
	@Before
	public void setup() throws IOException{
		BigFileCreator bfc = new BigFileCreator();
		bigFile = bfc.createFileWithRandomNumbersAsLines(FILE_A_PATH, FILE_A_START, FILE_A_START + fileSizeInLines);
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
		long startMillis = System.currentTimeMillis();
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
		LOG.info(String.format("Num lines = %s \t time millis = %s", fileSizeInLines, 
				System.currentTimeMillis() - startMillis));
		
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
