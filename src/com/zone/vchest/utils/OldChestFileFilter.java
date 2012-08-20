package com.zone.vchest.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

public class OldChestFileFilter implements FilenameFilter {

	public boolean accept(File file, String name) {
		return name.endsWith(".chest");
	}

	public static File[] listRecursively(File folder) {
		if (folder != null && folder.isDirectory()) {
			return _listRecursively(folder, 5).toArray(new File[0]);
		}
		return new File[0];
	}

	public static File[] listRecursively(File folder, int depth) {
		if (folder != null && folder.isDirectory()) {
			return _listRecursively(folder, depth).toArray(new File[0]);
		}
		return new File[0];
	}

	private static ArrayList<File> _listRecursively(File folder, int depth) {
		ArrayList<File> files = new ArrayList<File>();
		if (folder != null && folder.isDirectory()) {
			files.addAll(Arrays.asList(folder.listFiles(new OldChestFileFilter())));
			if (depth > 0) {// now scan folders
				File folders[] = folder.listFiles(new DirFilter());
				if (folders != null) {
					for (File f : folders) {
						files.addAll(_listRecursively(f, depth - 1));
					}
				}
			}
		}
		return files;
	}

	public static class DirFilter implements FilenameFilter {

		public boolean accept(File file, String name) {
			return file.isDirectory();
		}
	}
}