package com.sleepcamel.fileduplicatefinder.core.domain.finder

interface DuplicateFinder {
    fun scan(): Any

    fun findDuplicates(): Any

    fun suspend(): Any

    fun resume(): Any
}
