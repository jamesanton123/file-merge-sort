package com.jamesanton.cruncher.data.sorter;

public class SorterException extends Exception {

	private static final long serialVersionUID = -6467909029029558993L;
		
	public SorterException(String message) {
		super(message);
	}
	
	public SorterException(String message, Exception e) {
		super(message, e);
	}
}
