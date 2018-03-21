package com.sleepcamel.fileduplicatefinder.core.domain.finder

import com.sleepcamel.fileduplicatefinder.core.domain.DuplicateFinderProgress
import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper
import com.sleepcamel.fileduplicatefinder.core.domain.filefilters.FileWrapperFilter
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.beans.PropertyChangeListener

//@Bindable
class SequentialDuplicateFinder(var directories: MutableList<FileWrapper<*>>, val filter: FileWrapperFilter? = null) : DuplicateFinder {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    var poolSize = Runtime.getRuntime().availableProcessors()
    var findProgress = DuplicateFinderProgress()
    var duplicatesThread: Deferred<Any>? = null

    private fun pd(dirs: List<FileWrapper<*>>){
        runBlocking {
            dirs.forEach { async(coroutineContext) {
                if (it.isDir) {
                    pd(it.filterFiles(filter))
                } else {
                    findProgress.fileFound(it)
                }
            }}
        }
    }

    override fun scan() {
        filteredDirectories()
        findProgress.startScan()
        pd(directories)
    }

    fun filteredDirectories() {
        val backup = directories.toList()
        for (directory in backup) {
            directories.removeAll { directory.isFatherOrGrandFatherOf(it) }
        }
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
        duplicatesThread = async {
            while (findProgress.hasPhasesLeft() && isActive ) {
                val current = findProgress.currentPhase as DuplicateFinderPhase
                val dfs = current.initialFiles.map { f ->
                    async(parent = duplicatesThread){
                        if ( isActive ){ findProgress.processFile(f) }
                    }
                }

                /*
                // TODO group files by disk? that way we can parallelize
                files.each {
                    if ( Thread.interrupted() ){
                        throw new InterruptedException()
                    }
                    findProgress.processFile(it)
                }*/
                findProgress.nextPhase()
            }
            if ( !isActive ) log.info("Suspended search")

            if (!findProgress.hasPhasesLeft()) {
                findProgress.finishedFindingDuplicates()
            }
        }
    }

    fun addPropertyChangeListener(listener: PropertyChangeListener) {//todo
    }

    fun addPropertyChangeListener(name: String, listener: PropertyChangeListener) {//todo
    }

    fun removePropertyChangeListener(listener: PropertyChangeListener) {//todo
    }

    fun removePropertyChangeListener(name: String, listener: PropertyChangeListener) {//todo
    }

    fun firePropertyChange(name: String, oldValue: Any, newValue: Any) {//todo
    }

    /*
    //todo
    val propertyChangeListeners: Array<PropertyChangeListener>
        get() {}

    fun getPropertyChangeListeners(name: String): Array<PropertyChangeListener> {//todo
    }*/

}