package com.sleepcamel.fileduplicatefinder.core.domain.filefilters;

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper


public class OrWrapperFilter implements FileWrapperFilter{

	FileWrapperFilter[] filters;

	boolean accept(FileWrapper arg0) {
		for(FileWrapperFilter fileFilter:filters){
			if ( fileFilter.accept(arg0) ){
				return true;
			}
		}
		return false;
	}
}
