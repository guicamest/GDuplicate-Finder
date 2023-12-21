package com.sleepcamel.gduplicatefinder.core

import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

data class DuplicateGroup(val hash: String, val paths: Collection<PathWithAttributes>)

data class PathWithAttributes(
    val path: Path,
    val attributes: BasicFileAttributes,
) : BasicFileAttributes by attributes {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PathWithAttributes

        return path == other.path
    }

    override fun hashCode(): Int = path.hashCode()

    fun contentHash(type: String = "MD5"): String = path.contentHash(type)
}
