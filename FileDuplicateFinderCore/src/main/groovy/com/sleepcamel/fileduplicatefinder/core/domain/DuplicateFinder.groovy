package com.sleepcamel.fileduplicatefinder.core.domain

import static groovyx.gpars.GParsPool.runForkJoin
import static groovyx.gpars.GParsPool.withPool
import static groovyx.gpars.GParsPool.withExistingPool
import groovy.beans.Bindable;
import groovy.util.logging.Commons;

import java.lang.Thread;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sleepcamel.fileduplicatefinder.core.domain.filefilters.DefaultFilter


@Bindable
@Commons
class DuplicateFinder {

	List directories
	def filter = new DefaultFilter()
	def poolSize = Runtime.getRuntime().availableProcessors()
	def currentPool
	
	def findProgress = new DuplicateFinderProgress()

	def duplicatedEntries = []
	def duplicatedFiles = []

	AtomicBoolean suspending = new AtomicBoolean(false);
	
	def scan(){
		filteredDirectories()
		findProgress.startScan();
		Thread.start {
			withPool(poolSize) {pool ->
				runForkJoin(directories) { files ->
					files.each{ file ->
						if (file.isDir()) {
							forkOffChild(file.filterFiles(filter))
						} else {
							findProgress.fileFound(file)
						}
					}
				}
				while( !pool.isQuiescent() );
				findProgress.scanFinished()
			}
		}
	}
	
	def filteredDirectories(){
		def backup = directories.clone()
		for(FileWrapper directory:backup){
			directories.removeAll { directory.isFatherOrGrandFatherOf(it) }
		}
		directories
	}
	
	def findDuplicates(){
		findProgress.startFindingDuplicates()
		resume()
	}
	
	def suspend(){
		currentPool.shutdownNow()
		currentPool.workers*.interrupt()
		currentPool.workers*.stop()
		log.info("Shutting down pool")
		suspending.compareAndSet(false, true)
		synchronized(suspending){
			suspending.wait()
		}
		currentPool = null
	}
	
	def resume(){
		suspending.set(false)
		Thread.start{
			withPool(poolSize) {pool ->
				currentPool = pool
				def finished = true
				try{
				findProgress.foundFiles.eachParallel {
					if( suspending.get() ){
						Thread.currentThread().interrupt()
					}
					addToHash(it)
				}
				}catch(Exception e){
					finished = false
					log.info("Suspended search")
				}
				while( !pool.isQuiescent() );
				if( suspending.get() ){
					synchronized(suspending){
						suspending.notify()
					}
					log.info("Pool has been successfully shut down")
					suspending.compareAndSet(true, false)
				}
				
				if ( finished ){
					updateEntries()
					findProgress.finishedFindingDuplicates()
				}
			}
		}
	}
	
	def addToHash(file){
		findProgress.fileProcessed(file)
	}
	
	def updateEntries(){
		duplicatedFiles.clear()
		duplicatedEntries.clear()
		findProgress.filesMap.each {
			def duplicateEntry = it.value
			boolean hasDuplicates = duplicateEntry.hasDuplicates()
			if ( hasDuplicates ){
				duplicatedEntries << it.value
			}
		}
		duplicatedFiles.addAll(duplicatedEntries.collect { it.files }.flatten())
	}
	
}
