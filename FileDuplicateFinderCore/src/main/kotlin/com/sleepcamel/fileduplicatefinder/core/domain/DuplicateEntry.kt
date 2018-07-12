package com.sleepcamel.fileduplicatefinder.core.domain

import java.io.Serializable
import java.util.ArrayList

class DuplicateEntry(val hash: Any, val first: FileWrapper<*>) : Serializable {

    val files: MutableList<FileWrapper<*>> = mutableListOf(first)

    fun hasDuplicates(): Boolean = files.size > 1

    fun addFile(file: FileWrapper<*>) {
        if (!files.contains(file)) {
            files.add(file)
        }
    }

    fun removeNonExistingFiles(): List<FileWrapper<*>> {
        val nonExisting = files.filter { it.exists() }
        files.removeAll(nonExisting)
        return nonExisting
    }

    override fun toString(): String {
        return hash as String
    }

}
