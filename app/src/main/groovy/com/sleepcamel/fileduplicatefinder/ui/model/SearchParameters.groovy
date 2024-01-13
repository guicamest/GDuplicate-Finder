package com.sleepcamel.fileduplicatefinder.ui.model

import groovy.transform.ToString;

@ToString
class SearchParameters implements Serializable {

	private static final long serialVersionUID = -5051598027097903023L
	
	Long minSize
	Long maxSize
	
	List namesFilter
	List extensionsFilter
	List directories
}
