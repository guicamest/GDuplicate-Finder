package com.sleepcamel.fileduplicatefinder.core.domain

import com.sleepcamel.fileduplicatefinder.core.domain.finder.DuplicateFinderPhase
import com.sleepcamel.fileduplicatefinder.core.util.MD5Utils
import com.sleepcamel.fileduplicatefinder.core.util.toHex
import java.io.Serializable
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class DuplicateFinderProgress : Serializable {
    val phases: MutableList<DuplicateFinderPhase> = ArrayList()

    var totalFiles = AtomicInteger()
    var totalFileSize = AtomicLong()
    var finishedFindingDuplicates = false

    lateinit var foundFiles: List<FileWrapper<*>>
    /*@Synchronized*/ var currentPhase: DuplicateFinderPhase? = null
    /*@Synchronized*/ var progressData = ConcurrentHashMap<String, Any>()
    private var duplicatedEntries: MutableList<DuplicateEntry> = mutableListOf()

    // TODO Read from config(?)
    // TODO FileSize util here...
    val MIN_SIZE_FOR_SHORT_COMPARE: Int = 1024 * 1024// 1M
    val SHORT_COMPARE_BYTES: Int = 1024 // 1K

    fun initPhases() {
        phases.add(DuplicateFinderPhase("Size Phase", { it.size }))
        phases.add(DuplicateFinderPhase("Size/Hash Phase", {
            if ( it.size > MIN_SIZE_FOR_SHORT_COMPARE ){
                it.inputStream().use { iss ->
                    val buffer = ByteArray(SHORT_COMPARE_BYTES)
                    iss.read(buffer)
                    buffer.toHex()
                }
            } else null
        }, true))
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
        finishedFindingDuplicates = false
    }

    @Synchronized
    fun fileFound(file: FileWrapper<*>) {
        foundFiles += file
        totalFiles.incrementAndGet()
        totalFileSize.addAndGet(file.size.toLong())
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

    fun processFile(file: FileWrapper<*>) {
        currentPhase!!.processFile(file)
        updateProgressData()
    }

    fun updateProgressData(){
        val cPhase = currentPhase
        synchronized(cPhase!!) {
            progressData["phaseNumber"] = phaseNumber()
            progressData["percentDone"] = cPhase.percentDone()
            progressData["processedFilesQty"] = cPhase.processedFilesQty.toInt()
            progressData["processedFileSize"] = cPhase.processedFileSize.toLong()
            progressData["totalFiles"] = cPhase.totalFiles.toInt()
            progressData["totalFileSize"] = cPhase.totalFileSize.toLong()
        }
    }

    fun hasPhasesLeft(): Boolean = currentPhase != null

    fun phaseNumber(): Int = phases.indexOf(currentPhase)

    fun duplicatedEntries()= duplicatedEntries

    companion object {
        private const val serialVersionUID = 6444577992643698844L
    }
}
