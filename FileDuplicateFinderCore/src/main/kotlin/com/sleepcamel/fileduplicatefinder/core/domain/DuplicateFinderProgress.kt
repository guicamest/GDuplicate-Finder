package com.sleepcamel.fileduplicatefinder.core.domain

import com.sleepcamel.fileduplicatefinder.core.domain.finder.DuplicateFinderPhase
import java.beans.PropertyChangeListener
import java.io.Serializable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

//@Bindable
class DuplicateFinderProgress : Serializable {
    val phases: MutableList<DuplicateFinderPhase> = ArrayList()

    var totalFiles = AtomicInteger()
    var totalFileSize = AtomicLong()
    var finishedScanning = false
    var finishedFindingDuplicates = false

    lateinit var foundFiles: List<FileWrapper<*>>
    /*@Synchronized*/ var currentPhase: DuplicateFinderPhase? = null
    /*@Synchronized*/ var progressData = ConcurrentHashMap<String, Any>()
    var duplicatedEntries: MutableList<DuplicateEntry> = mutableListOf()

    fun initPhases() {
        phases.add(DuplicateFinderPhase("Size Phase", { it.size }))
        phases.add(DuplicateFinderPhase("Hash Phase", { it.md5 }))
    }

    init {
        initPhases()
        startScan()
        startFindingDuplicates()
    }

    fun startScan() {
        totalFiles.set(0)
        totalFileSize.set(0)
        foundFiles = mutableListOf()//DefaultGroovyMethods.asSynchronized(ArrayList())
        finishedScanning = false
        finishedFindingDuplicates = false
    }

    @Synchronized
    fun fileFound(file: FileWrapper<*>) {
        foundFiles += file
        totalFiles.incrementAndGet()
        totalFileSize.addAndGet(file.size.toLong())
    }

    fun scanFinished() {
        finishedScanning = true
    }

    fun startFindingDuplicates() {
        currentPhase = phases[0]
        currentPhase!!.start(foundFiles)
        updateProgressData()
        finishedFindingDuplicates = false
    }

    fun finishedFindingDuplicates() {
        finishedFindingDuplicates = true
    }

    fun nextPhase() {
        currentPhase!!.finished()
        val currentPhaseIdx = phaseNumber()
        if (currentPhaseIdx == phases.size - 1) {
            duplicatedEntries = currentPhase!!.duplicatedEntries
            currentPhase = null
        } else {
            val lastPhaseOutput = currentPhase!!.possibleDuplicateFiles
            currentPhase = phases[currentPhaseIdx + 1]
            currentPhase!!.start(lastPhaseOutput)
        }

    }

    suspend fun processFile(file: FileWrapper<*>) {
        currentPhase!!.processFile(file)
        updateProgressData()
    }

    fun updateProgressData(){
        val cPhase = currentPhase
        synchronized(cPhase!!) {
            progressData["phaseNumber"] = phaseNumber()
            progressData["percentDone"] = cPhase.percentDone()
            progressData["processedFilesQty"] = cPhase.processedFilesQty
            progressData["processedFileSize"] = cPhase.processedFileSize
            progressData["totalFiles"] = cPhase.totalFiles
            progressData["totalFileSize"] = cPhase.totalFileSize
        }
    }

    fun hasPhasesLeft(): Boolean = currentPhase != null

    fun phaseNumber(): Int = phases.indexOf(currentPhase)

    fun duplicatedEntries()= duplicatedEntries

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

    companion object {
        private const val serialVersionUID = 6444577992643698844L
    }
}
