package com.sleepcamel.fileduplicatefinder.core.domain.finder

import com.sleepcamel.fileduplicatefinder.core.domain.DuplicateEntry
import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper

import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/*
https://github.com/Kotlin/kotlinx.coroutines/issues/7
http://bytes.schibsted.com/async-patterns-on-android/
https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md#children-of-a-coroutine
https://kotlinlang.org/docs/tutorials/coroutines-basic-jvm.html
https://blog.simon-wirtz.de/kotlin-coroutines-guide/
https://kotlinlang.org/docs/reference/coroutines.html
 */

class DuplicateFinderPhase(val name: String, val hashClosure: suspend (FileWrapper<*>) -> Any) : Serializable {
    lateinit var initialFiles: MutableList<FileWrapper<*>>
    private val processedFiles = mutableListOf<FileWrapper<*>>()
    private val filesMap: ConcurrentHashMap<Any, DuplicateEntry> = ConcurrentHashMap()

    val processedFilesQty = AtomicInteger()
    val processedFileSize = AtomicLong()
    val totalFiles = AtomicInteger()
    val totalFileSize = AtomicLong()
    var duplicatedEntries = mutableListOf<DuplicateEntry>()
    var possibleDuplicateFiles = mutableListOf<FileWrapper<*>>()

    fun start(firstFiles: List<FileWrapper<*>>) {
        synchronized(this) {
            initialFiles = firstFiles.toMutableList()
            processedFiles.clear()
            processedFilesQty.set(0)
            processedFileSize.set(0)
            filesMap.clear()
            synchronized(initialFiles) {
                totalFiles.set(initialFiles.size)
                totalFileSize.set(initialFiles.fold(0L) { ac , fw->  ac+fw.size })
            }
        }
    }

    suspend fun processFile(file: FileWrapper<*>) {
        val hash = hashClosure(file)
        synchronized(this) {
            val duplicateEntry = filesMap.getOrPut(hash){DuplicateEntry(hash, file)}
            duplicateEntry.addFile(file)
            fileProcessed(file)
        }
    }

    fun fileProcessed(file: FileWrapper<*>) {
        if (!processedFiles.contains(file)) {
            processedFiles += file
            processedFilesQty.incrementAndGet()
            processedFileSize.addAndGet(file.size.toLong())
        }

        initialFiles.remove(file)
    }

    fun syncProgress() {
        if (processedFilesQty.get() < processedFiles.size) {
            processedFilesQty.incrementAndGet() // tokot Why ++ only?
        }

        processedFileSize.set(processedFiles.fold(0L, { a, b -> a + b.size}))

        val iterator = processedFiles.asReversed().iterator()
        var matches: Boolean = true
        while (iterator.hasNext() && matches) {
            val next = iterator.next()
            matches = initialFiles.contains(next)
            if (matches) {
                initialFiles.remove(next)
            }
        }
    }

    fun percentDone(): Double = (processedFilesQty.get() * processedFileSize.toDouble())/( totalFiles.get() * totalFileSize.toDouble())

    fun finished() {
        possibleDuplicateFiles.clear()
        duplicatedEntries.clear()
        filesMap.forEach{ (_, duplicateEntry) ->
            if ( duplicateEntry.hasDuplicates() ) duplicatedEntries.add(duplicateEntry)
        }
        possibleDuplicateFiles.addAll(duplicatedEntries.flatMap { it.files })
    }

    override fun hashCode(): Int = name.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DuplicateFinderPhase

        return name == other.name
    }


    /*
    fun getProcessedFiles(): List<*> {
        return processedFiles
    }

    fun setProcessedFiles(processedFiles: MutableList<*>) {
        this.processedFiles = processedFiles
    }

    fun getDuplicatedEntries(): List<*> {
        return duplicatedEntries
    }

    fun setDuplicatedEntries(duplicatedEntries: MutableList<*>) {
        this.duplicatedEntries = duplicatedEntries
    }

    fun getPossibleDuplicateFiles(): List<*> {
        return possibleDuplicateFiles
    }

    fun setPossibleDuplicateFiles(possibleDuplicateFiles: MutableList<*>) {
        this.possibleDuplicateFiles = possibleDuplicateFiles
    }
    */

}
