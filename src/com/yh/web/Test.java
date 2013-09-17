package com.yh.web;

import java.util.regex.Pattern;

/**
 * @author gudh
 *
 */
public class Test {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//CachePolicy.initPolicy(null);
		String url = "http://120.193.11.52/rdown/64-all/20130803000000/92/a6/7bce.mp3";
		String regex = ".*\\.(mp3|MP3)(\\?[^/]*)?$";
		System.out.println(Pattern.matches(regex, url));
	}

}
