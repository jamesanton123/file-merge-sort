package com.jamesanton.cruncher.data.merger;

import java.io.File;

public interface FileMerger {
	public File merge() throws MergerException;
}
