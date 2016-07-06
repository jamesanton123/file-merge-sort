package com.jamesanton.cruncher.data.merger;

public class MergerException extends Exception {

	private static final long serialVersionUID = -6467909029029558993L;
		
	public MergerException(String message) {
		super(message);
	}
	
	public MergerException(String message, Exception e) {
		super(message, e);
	}
}
