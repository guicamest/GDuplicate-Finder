package com.sleepcamel.fileduplicatefinder.core.domain.filefilters

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper
import java.util.regex.Pattern

interface FileWrapperFilter {
    fun accept(arg0: FileWrapper<*>): Boolean
}

open class NameFilter : FileWrapperFilter {
    private val regex: Pattern
    @JvmOverloads constructor(name: String, caseInsensitive: Boolean = true) {
        regex = if (caseInsensitive) {
            Pattern.compile(".*$name.*$", Pattern.CASE_INSENSITIVE)
        } else {
            Pattern.compile(".*$name.*$")
        }

    }

    constructor(pattern: Pattern) {
        regex = pattern
    }

    override fun accept(arg0: FileWrapper<*>): Boolean {
        if (arg0.isDir) return true
        return regex.matcher(arg0.name).find()
    }
}
class ExtensionFilter(extension: String) : NameFilter(Pattern.compile(".*\\.$extension$"))

class OrWrapperFilter : FileWrapperFilter {
    override fun accept(arg0: FileWrapper<*>): Boolean {
        for (fileFilter in filters!!) {
            if (fileFilter.accept(arg0)) {
                return true
            }
        }

        return false
    }

    var filters: Array<FileWrapperFilter>? = null
}

class AndWrapperFilter : FileWrapperFilter {
    override fun accept(arg0: FileWrapper<*>): Boolean {
        for (fileFilter in filters!!) {
            if (!fileFilter.accept(arg0)) {
                return false
            }
        }

        return true
    }

    var filters: Array<FileWrapperFilter>? = null
}

class SizeFilter : FileWrapperFilter {
    override fun accept(arg0: FileWrapper<*>): Boolean {
        if (arg0.isDir) return true
        val fileSize = arg0.size

        return fileSize >= minSize && fileSize <= maxSize
    }

    var minSize: Long = -1
    var maxSize = java.lang.Long.MAX_VALUE
}

class DirectoryFilter : FileWrapperFilter {
    override fun accept(arg0: FileWrapper<*>): Boolean {
        return arg0.isDir
    }
}
