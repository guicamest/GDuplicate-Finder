package com.sleepcamel.gduplicatefinder.core

import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

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
