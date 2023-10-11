package com.sleepcamel.fileduplicatefinder.core.domain.filefilters

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper

class DirectoryFilter implements FileWrapperFilter {

	boolean accept(FileWrapper arg0) {
		return arg0.isDir();
	}

}
