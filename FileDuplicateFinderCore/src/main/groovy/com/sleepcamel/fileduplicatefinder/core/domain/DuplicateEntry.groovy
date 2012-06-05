package com.sleepcamel.fileduplicatefinder.core.domain

class DuplicateEntry implements Serializable {

	String hash
	def files = []
	
	def hasDuplicates(){
		files.size() > 1
	}

	def removeNonExistingFiles(){
		def nonExisting = files.findAll{fileWrapper -> !fileWrapper.exists()}
		files.removeAll(nonExisting)
		nonExisting
	}
		
	String toString(){
		hash
	}
}
