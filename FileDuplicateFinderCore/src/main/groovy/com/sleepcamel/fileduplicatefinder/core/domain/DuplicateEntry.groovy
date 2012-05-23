package com.sleepcamel.fileduplicatefinder.core.domain

class DuplicateEntry implements Serializable {

	String hash
	def files = []
	
	def hasDuplicates(){
		files.size() > 1
	}
	
	String toString(){
		hash
	}
}
