package com.sleepcamel.fileduplicatefinder.core.domain.filefilters

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper


class SizeFilter implements FileWrapperFilter {

	long minSize = -1;
	long maxSize = Long.MAX_VALUE;
	
	boolean accept(FileWrapper arg0) {
		def fileSize = arg0.size;
		
		if ( arg0.isDir() ) return true;
		
		fileSize >= minSize && fileSize <= maxSize;
	}

}
