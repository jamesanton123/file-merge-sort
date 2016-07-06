package com.jamesanton.cruncher.data.splitter;

public class SplitterException extends Exception {
	

	private static final long serialVersionUID = 657748478994700531L;
	
	public SplitterException(String message) {
		super(message);
	}
	
	public SplitterException(String message, Exception e) {
		super(message, e);
	}
}
