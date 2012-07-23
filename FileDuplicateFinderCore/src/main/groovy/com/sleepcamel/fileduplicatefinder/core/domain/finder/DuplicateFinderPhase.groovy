package com.sleepcamel.fileduplicatefinder.core.domain.finder

import java.util.concurrent.ConcurrentHashMap

import com.sleepcamel.fileduplicatefinder.core.domain.DuplicateEntry


public class DuplicateFinderPhase implements Serializable {

	def name
	Closure hashClosure
	
	def initialFiles
	def processedFiles = []
	def filesMap = [:] as ConcurrentHashMap
	def processedFilesQty = 0
	def processedFileSize = 0
	
	synchronized def totalFiles = 0
	synchronized def totalFileSize = 0
	
	def duplicatedEntries = []
	def possibleDuplicateFiles = []
	
	def start(firstFiles){
		synchronized (this){
		initialFiles = firstFiles
		processedFiles.clear()
		processedFilesQty = 0
		processedFileSize = 0
		filesMap.clear()
		synchronized (initialFiles){
		totalFiles = initialFiles.size()
		totalFileSize = initialFiles.sum(0){ it.size }
		}
		}
	}
	
	def processFile(file){
		def hash = hashClosure.call(file)
		synchronized (this){
			if ( filesMap[hash] == null ){
				filesMap[hash] = new DuplicateEntry(hash: hash)
			}
			filesMap[hash].addFile(file)
			fileProcessed(file)
		}
	}
	
	def fileProcessed(file){
		if ( !processedFiles.contains(file) ){
			processedFiles << file
			processedFilesQty++
			processedFileSize += file.size
		}
		initialFiles.remove(file)
	}
	
	def syncProgress(){
		if ( processedFilesQty < processedFiles.size() ){
			processedFilesQty++
		}
		processedFileSize = processedFiles.inject(0) { a, b -> a + b.size }
		
		def iterator = processedFiles.iterator().reverse()
		def matches = true
		while ( iterator.hasNext() && matches ){
			def next = iterator.next()
			matches = initialFiles.contains(next)
			if ( matches ){
				initialFiles.remove(next)
			}
		}
	}

	def percentDone(){
		double dnow = processedFileSize
		double dtotal = totalFileSize
		dnow/dtotal
	}

	def finished(){
		possibleDuplicateFiles.clear()
		duplicatedEntries.clear()
		filesMap.each {
			def duplicateEntry = it.value
			boolean hasDuplicates = duplicateEntry.hasDuplicates()
			if ( hasDuplicates ){
				duplicatedEntries << duplicateEntry
			}
		}
		possibleDuplicateFiles.addAll(duplicatedEntries.collect { it.files }.flatten())
	}
	
	int hashCode(){
		name.hashCode()
	}
	
	boolean equals(other){
		name == other.name
	}
}
