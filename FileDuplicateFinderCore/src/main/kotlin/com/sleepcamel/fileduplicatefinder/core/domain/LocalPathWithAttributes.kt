package com.sleepcamel.fileduplicatefinder.core.domain

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

class LocalPathWithAttributes(val path: Path, attrs: BasicFileAttributes) : BasicFileAttributes by attrs {
    fun exists() = Files.exists(path)
    init {
        //java.nio.file.attribute.FileTime ft = attrs.creationTime()
        //java.nio.file.attribute.FileTime ft = attrs.lastAccessTime()
        //java.nio.file.attribute.FileTime ft = attrs.lastModifiedTime()
        // lastModified date, long, FileTime
    }
}