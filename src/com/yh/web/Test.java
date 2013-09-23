package com.yh.web;

import java.io.File;
import java.util.List;

import com.yh.web.cache.ScheduleTask;

/**
 * @author gudh
 * 
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<File> files = ScheduleTask.scanEmptyFolders("D:\\mm");
		System.out.println();
		for (File f : files) {
			System.out.println(f.getAbsolutePath());
		}
		ScheduleTask.deleteFolders(files);
	}

}
