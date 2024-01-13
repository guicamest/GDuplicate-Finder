package com.sleepcamel.fileduplicatefinder.ui.model

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper


class RootFileWrapper extends FileWrapper {
	RootFileWrapper(){		super(null)
	}

	def reloadDirectoryFiles = { directory ->
		def parents = []
		
		def dir = directory
		while ( ! (dir in files) ){
			parents << dir
			dir = dir.getParentWrapper()
		}
		parents << dir
		parents.reverse(true)
		
		def currentDirFiles = dirs
		def dirToUpdate
		parents.each { parent ->
			if ( currentDirFiles ){
				dirToUpdate = currentDirFiles.find{ it -> parent == it }
				currentDirFiles = dirToUpdate.dirs
			}
		}
		dirToUpdate.loadFiles(true)
	}
}
