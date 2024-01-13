package com.sleepcamel.fileduplicatefinder.ui.utils.associations

import org.apache.commons.io.FilenameUtils

@Singleton
class FileAssociations {

	def handlers = [:]
	
	def registerHandler(String extension, Closure c){
		if ( !handlers.containsKey(extension) ){
			handlers[extension] = new FileHandler(closure : c)
		}
	}
	
	FileHandler getHandlerFor(String filename){
		handlers[FilenameUtils.getExtension(filename)]
	}
}
