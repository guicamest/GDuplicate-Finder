package com.sleepcamel.fileduplicatefinder.core.domain.filefilters

import java.util.regex.Pattern;

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper


class NameFilter implements FileWrapperFilter {

	Pattern regex;
	
	NameFilter(String name){
		this(name, true)
	}
	
	NameFilter(String name, Boolean caseInsensitive){
		if ( caseInsensitive ){
			regex = Pattern.compile('.*'+name+'.*$', Pattern.CASE_INSENSITIVE)
		}else{
			regex = Pattern.compile('.*'+name+'.*$')
		}
	}
	
	NameFilter(Pattern pattern){
		regex = pattern;
	}
	
	boolean accept(FileWrapper arg0) {
		if ( arg0.isDir() ) return true;
		return regex.matcher(arg0.getName()).find();
	}

}
