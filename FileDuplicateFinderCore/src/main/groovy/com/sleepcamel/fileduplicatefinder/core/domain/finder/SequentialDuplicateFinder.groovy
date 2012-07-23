package com.sleepcamel.fileduplicatefinder.core.domain.finder

import static groovyx.gpars.GParsPool.runForkJoin
import static groovyx.gpars.GParsPool.withPool
import static groovyx.gpars.GParsPool.withExistingPool
import groovy.beans.Bindable
import groovy.util.logging.Commons

import java.lang.Thread
import java.util.concurrent.atomic.AtomicBoolean

import com.sleepcamel.fileduplicatefinder.core.domain.DuplicateFinderProgress
import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper
import com.sleepcamel.fileduplicatefinder.core.domain.filefilters.DefaultFilter


@Bindable
@Commons
class SequentialDuplicateFinder implements DuplicateFinder {

	List directories
	def filter = new DefaultFilter()
	def poolSize = Runtime.getRuntime().availableProcessors()
	
	DuplicateFinderProgress findProgress = new DuplicateFinderProgress()

	def scan(){
		filteredDirectories()
		findProgress.startScan()
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
				while( !pool.isQuiescent() ){}
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
		log.info('Shutting down pool')
		duplicatesThread.interrupt()
	}
	
	def duplicatesThread

	def resume(){
		duplicatesThread = Thread.start{
			def finished = true
			try{
				while( findProgress.hasPhasesLeft() ){
					DuplicateFinderPhase current = findProgress.currentPhase
					def files
					synchronized(current.initialFiles){files = new ArrayList(current.initialFiles)}
					files.each {
						if ( Thread.interrupted() ){
							throw new InterruptedException()
						}
						findProgress.processFile(it)
					}
					findProgress.nextPhase()
				}
			}catch(InterruptedException e){
//				findProgress.currentPhase.syncProgress()
				finished = false
				log.info('Suspended search')
			}

			if ( finished ){
				findProgress.finishedFindingDuplicates()
			}
		}
	}
	
}
