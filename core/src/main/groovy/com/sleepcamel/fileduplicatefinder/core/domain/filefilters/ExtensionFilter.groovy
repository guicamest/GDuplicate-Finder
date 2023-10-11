package com.sleepcamel.fileduplicatefinder.core.domain.filefilters

import java.util.regex.Pattern;


class ExtensionFilter extends NameFilter {

	ExtensionFilter(extension){
		super(Pattern.compile('.*\\.'+extension+'$'))
	}
	
}
