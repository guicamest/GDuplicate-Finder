package com.sleepcamel.fileduplicatefinder.ui.utils.associations


class FileHandler {

	Closure closure
	
	def handle(String fileName){
		closure.call(new File(fileName))
	}
}
