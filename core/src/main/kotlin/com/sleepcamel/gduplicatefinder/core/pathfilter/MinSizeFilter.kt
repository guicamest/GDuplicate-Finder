package com.sleepcamel.gduplicatefinder.core.pathfilter

import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class MinSizeFilter(private val size: Long) {
    @Throws(IOException::class)
    fun accept(
        entry: Path,
        attributes: BasicFileAttributes,
    ) = attributes.size() > size
}
