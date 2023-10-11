package com.sleepcamel.fileduplicatefinder.core.domain

class DuplicateEntry implements Serializable {

	def hash
	def files = []
	
	def hasDuplicates(){
		files.size() > 1
	}
	
	def addFile(file){
		if ( !files.contains(file) ){
			files.add(file)
		}
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
