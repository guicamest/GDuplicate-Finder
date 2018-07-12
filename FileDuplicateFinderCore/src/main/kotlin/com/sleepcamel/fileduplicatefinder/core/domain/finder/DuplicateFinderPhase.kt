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

class DuplicateFinderPhase(val name: String, val hashClosure: (FileWrapper<*>) -> Any?, val byPassNullHash: Boolean = false) : Serializable {
    lateinit var initialFiles: MutableList<FileWrapper<*>>
    private val processedFiles = mutableListOf<FileWrapper<*>>()
    private val filesMap: ConcurrentHashMap<Any, DuplicateEntry> = ConcurrentHashMap()

    val processedFilesQty = AtomicInteger()
    val processedFileSize = AtomicLong()
    val totalFiles = AtomicInteger()
    val totalFileSize = AtomicLong()
    var duplicatedEntries = mutableListOf<DuplicateEntry>()
    var possibleDuplicateFiles = mutableListOf<FileWrapper<*>>()
    var bypassed = mutableListOf<FileWrapper<*>>()

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

    fun processFile(file: FileWrapper<*>) {
        val hash = hashClosure(file)
        synchronized(this) {
            if ( hash != null ){
                val duplicateEntry = filesMap.getOrPut(hash){DuplicateEntry(hash, file)}
                duplicateEntry.addFile(file)
            }else if ( byPassNullHash ){
                bypassed.add(file)
            }
            fileProcessed(file)
        }
    }

    private fun fileProcessed(file: FileWrapper<*>) {
        if (!processedFiles.contains(file)) {
            processedFiles += file
            processedFilesQty.incrementAndGet()
            processedFileSize.addAndGet(file.size.toLong())
        }

        initialFiles.remove(file)
    }

    fun percentDone(): Double {
        val all = totalFiles.get() * totalFileSize.toDouble()
        return if (all != 0.0) (processedFilesQty.get() * processedFileSize.toDouble()) / all else 0.0
    }

    fun finished() {
        possibleDuplicateFiles.clear()
        duplicatedEntries.clear()
        filesMap.forEach{ (_, duplicateEntry) ->
            if ( duplicateEntry.hasDuplicates() ) duplicatedEntries.add(duplicateEntry)
        }
        if ( byPassNullHash ) {
            possibleDuplicateFiles.addAll(bypassed)
            println("${filesMap.filter { !it.value.hasDuplicates() }.flatMap { it.value.files }.fold(0L) { s, f -> s+(f.size-1024L) }} bytes saved")
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

}
