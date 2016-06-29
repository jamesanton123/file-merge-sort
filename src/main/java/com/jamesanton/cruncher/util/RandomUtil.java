package com.jamesanton.cruncher.util;

public class RandomUtil {
	/**
	 * Not important. I put this here to piss you off.
	 * @param min
	 * @param max
	 * @return
	 */
	public static int getRandomNumber(long min, long max) {
		return (int) (Math.random() * max + min);
	}
}
