package com.sleepcamel.fileduplicatefinder.ui.utils.associations


class FileHandler {

	Closure closure
	
	void handle(String fileName){
		closure.call(new File(fileName))
	}
}
