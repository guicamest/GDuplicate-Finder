package com.sleepcamel.fileduplicatefinder.core.domain

import static groovyx.gpars.GParsPool.runForkJoin
import static groovyx.gpars.GParsPool.withPool

import java.util.concurrent.ConcurrentHashMap;

import groovy.beans.Bindable
import groovy.transform.Synchronized


@Bindable
class DuplicateFinderProgress implements Serializable {

	synchronized def totalFiles
	synchronized def totalFileSize
	synchronized def finishedScanning

	List foundFiles
	List processedFiles

	def processedFilesQty
	def processedFileSize
	def finishedFindingDuplicates
	
	def filesMap = [:] as ConcurrentHashMap
	
	def duplicatedEntries = []
	def duplicatedFiles = []
	
	DuplicateFinderProgress(){
		startScan()
		startFindingDuplicates()
	}
	
	def startScan(){
		totalFiles = 0
		totalFileSize = 0
		foundFiles = []
		finishedScanning = false
		
		processedFilesQty = 0
		processedFileSize = 0
		processedFiles = []
		finishedFindingDuplicates = false
		
	}
	
	@Synchronized
	def fileFound(file){
		foundFiles << file
		totalFiles++
		totalFileSize = totalFileSize + file.size
	}
	
	def scanFinished(){
		finishedScanning = true
	}
	
	def startFindingDuplicates(){
		finishedFindingDuplicates = false
		processedFilesQty = 0
		processedFileSize = 0
		filesMap.clear()
	}

	@Synchronized
	def fileProcessed(file){
		synchronized (foundFiles) {
			foundFiles.remove(file)
			processedFiles << file
		}

		def md5 = file.md5()
		synchronized (filesMap) {
			if ( filesMap[md5] == null ){
				filesMap[md5] = new DuplicateEntry(hash: md5)
			}
		}
		filesMap[md5].files << file

		processedFilesQty++
		processedFileSize += file.size
	}
	
	def percentDone(){
		double dnow = processedFileSize
		double dtotal = totalFileSize
		dnow/dtotal
	}
	
	def finishedFindingDuplicates(){
		updateEntries()
		finishedFindingDuplicates = true
	}
	
	def updateEntries(){
		duplicatedFiles.clear()
		duplicatedEntries.clear()
		filesMap.each {
			def duplicateEntry = it.value
			boolean hasDuplicates = duplicateEntry.hasDuplicates()
			if ( hasDuplicates ){
				duplicatedEntries << duplicateEntry
			}
		}
		duplicatedFiles.addAll(duplicatedEntries.collect { it.files }.flatten())
	}
}
