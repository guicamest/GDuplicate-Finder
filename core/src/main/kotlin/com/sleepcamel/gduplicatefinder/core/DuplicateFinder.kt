package com.sleepcamel.gduplicatefinder.core

import java.nio.file.Path

interface DuplicateFinder {
    fun find(directories: Collection<Path>): FindDuplicatesExecution
}