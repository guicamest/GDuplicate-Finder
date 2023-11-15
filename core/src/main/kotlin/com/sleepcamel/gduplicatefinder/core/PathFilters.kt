package com.sleepcamel.gduplicatefinder.core

import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

interface PathFilter {
    @Throws(IOException::class)
    fun accept(
        entry: Path,
        attributes: BasicFileAttributes,
    ): Boolean
}

data class MinSizeFilter(private val size: Long) : PathFilter {
    @Throws(IOException::class)
    override fun accept(
        entry: Path,
        attributes: BasicFileAttributes,
    ) = attributes.size() > size
}

data class MaxSizeFilter(private val size: Long) : PathFilter {
    @Throws(IOException::class)
    override fun accept(
        entry: Path,
        attributes: BasicFileAttributes,
    ) = attributes.size() < size
}

/**
 * Filename without extension filter
 * If file is /some/path/somefile.txt => applies only to "somefile"
 */
data class FilenameFilter(private val name: String, private val exact: Boolean) : PathFilter {
    private val regex = Regex(if (exact) name else ".*$name.*$")

    override fun accept(
        entry: Path,
        attributes: BasicFileAttributes,
    ): Boolean = entry.nameWithoutExtension.matches(regex)
}

/**
 * Extension only filter
 * If file is /some/path/somefile.txt => applies only to "txt"
 * If file doesn't have extension => applies to "" (empty string)
 */
data class ExtensionFilter(private val extension: String, private val exact: Boolean) : PathFilter {
    private val regex = Regex(if (exact) extension else ".*$extension.*$")

    override fun accept(
        entry: Path,
        attributes: BasicFileAttributes,
    ): Boolean = entry.extension.matches(regex)
}
