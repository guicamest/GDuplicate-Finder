package com.sleepcamel.fileduplicatefinder.core.domain.filefilters;

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper;

public interface FileWrapperFilter {

	boolean accept(FileWrapper arg0);
}
