package com.java8.c1;

import java.io.File;
import java.io.FileFilter;

public class FileFilterTest {

	public static void main(String[] args) {
		File[] files = new File("D:\\8000").listFiles();
		System.out.println(files.length);

		files = new File("D:\\8000").listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isHidden();
			}
		});
		System.out.println(files.length);

		files = new File("D:\\8000").listFiles(File::isHidden);
		System.out.println(files.length);
	}

}
