package com.sleepcamel.fileduplicatefinder.core.domain

import static groovyx.gpars.GParsPool.runForkJoin
import static groovyx.gpars.GParsPool.withPool

import java.util.concurrent.ConcurrentHashMap


import com.sleepcamel.fileduplicatefinder.core.domain.finder.DuplicateFinderPhase

import groovy.beans.Bindable
import groovy.transform.Synchronized


@Bindable
class DuplicateFinderProgress implements Serializable {

	synchronized def totalFiles
	synchronized def totalFileSize
	synchronized def finishedScanning
	synchronized def finishedFindingDuplicates = false

	List foundFiles
	
	def phases = []
	synchronized def currentPhase
	synchronized def progressData = [phaseNumber: 0, percentDone: 0, processedFilesQty: 0, processedFileSize: 0,
			                totalFiles: 0, totalFileSize: 0] as ConcurrentHashMap
	
	def duplicatedEntries
	
	def initPhases(){
		def sizePhase = new DuplicateFinderPhase(name:'Size Phase')
		sizePhase.hashClosure = { file ->
			file.size
		}

		phases.add(sizePhase)
		def md5Phase = new DuplicateFinderPhase(name:'Hash Phase')
		md5Phase.hashClosure = { file ->
			file.md5()
		}

		phases.add(md5Phase)
	}

	DuplicateFinderProgress(){
		initPhases()
		startScan()
		startFindingDuplicates()
	}
	
	def startScan(){
		totalFiles = 0
		totalFileSize = 0
		foundFiles = [].asSynchronized()
		finishedScanning = false
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
		currentPhase = phases.get(0)
		currentPhase.start(foundFiles)
		updateProgressData()
		finishedFindingDuplicates = false
	}

	def finishedFindingDuplicates(){
		finishedFindingDuplicates = true
	}
	
	def nextPhase(){
		currentPhase.finished()
		def currentPhaseIdx = phaseNumber()
		if ( currentPhaseIdx == phases.size() - 1 ){
			duplicatedEntries = currentPhase.duplicatedEntries
			currentPhase = null
		}else{
			def lastPhaseOutput = currentPhase.possibleDuplicateFiles
			currentPhase = phases.get(currentPhaseIdx + 1)
			currentPhase.start(lastPhaseOutput)
		}
	}
	
	def processFile(file){
		currentPhase.processFile(file)
		updateProgressData()
	}
	
	def updateProgressData(){
		def cPhase = currentPhase
		synchronized (cPhase) {
			progressData['phaseNumber'] = phaseNumber()
			progressData['percentDone'] = cPhase.percentDone()
			progressData['processedFilesQty'] = cPhase.processedFilesQty
			progressData['processedFileSize'] = cPhase.processedFileSize
			progressData['totalFiles'] = cPhase.totalFiles
			progressData['totalFileSize'] = cPhase.totalFileSize
		}
	}
	
	def hasPhasesLeft(){
		currentPhase != null
	}

	def phaseNumber(){
		phases.indexOf(currentPhase)
	}

	def duplicatedEntries(){
		duplicatedEntries
	}
}
