package com.sleepcamel.fileduplicatefinder.core.domain.finder

import com.sleepcamel.fileduplicatefinder.core.domain.DuplicateFinderProgress
import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper
import com.sleepcamel.fileduplicatefinder.core.domain.filefilters.FileWrapperFilter
import kotlinx.coroutines.experimental.*
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.experimental.CoroutineContext

class SequentialDuplicateFinder(var directories: MutableList<FileWrapper<*>>, val filter: FileWrapperFilter? = null) : DuplicateFinder {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    var findProgress = DuplicateFinderProgress()
    var duplicatesThread: Job? = null

    override fun scan(): Job {
        filteredDirectories()
        findProgress.startScan()
        val context = newFixedThreadPoolContext(poolSize, "scanners")
        return launch(context) { pd(directories, context) }
    }

    override fun findDuplicates() {
        findProgress.startFindingDuplicates()
        resume()
    }

    override fun suspend() {
        log.info("Shutting down pool")
        duplicatesThread!!.cancel()
    }

    override fun resume() {
        duplicatesThread = launch(newSingleThreadContext("finder")) {
            while (findProgress.hasPhasesLeft() && isActive ) {
                val stopWatch = StopWatch()
                stopWatch.start()
                val current = findProgress.currentPhase as DuplicateFinderPhase
                // TODO group files by disk? that way we can parallelize
                // toList => prevent concurrent modification by iterating over an immutable copy
                current.initialFiles.toList().forEach { f ->
                    if ( isActive ){ findProgress.processFile(f) }
                }
                log.info("Phase ${current.name} took ${stopWatch.time}ms")
                stopWatch.stop()
                if ( isActive ) findProgress.nextPhase()
            }
            if ( !isActive ) log.info("Suspended search")

            if (!findProgress.hasPhasesLeft()) {
                findProgress.finishedFindingDuplicates()
            }
        }
    }

    private fun pd(dirs: List<FileWrapper<*>>, job: CoroutineContext){
        dirs.forEach { fod ->
            if (fod.isDir) {
                launch(job) {
                    pd(fod.filterFiles(filter), job)
                }
            } else {
                findProgress.fileFound(fod)
            }
        }
    }

    private fun filteredDirectories() {
        val backup = directories.toList()
        for (directory in backup) {
            directories.removeAll { directory.isFatherOrGrandFatherOf(it) }
        }
    }

    companion object {
        private val poolSize = Runtime.getRuntime().availableProcessors()
    }
}