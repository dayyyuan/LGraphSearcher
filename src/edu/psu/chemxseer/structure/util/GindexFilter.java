package edu.psu.chemxseer.structure.util;

import java.io.File;
import java.io.FileFilter;

public class GindexFilter implements FileFilter {

	@Override
	public boolean accept(File pathname) {
		String name = pathname.getName();
		if (name.contains("_"))
			return false;
		else
			return true;
	}

}
