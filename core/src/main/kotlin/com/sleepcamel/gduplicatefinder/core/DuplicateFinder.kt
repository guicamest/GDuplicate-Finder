package com.sleepcamel.gduplicatefinder.core

import java.nio.file.Path

interface DuplicateFinder {
    fun find(
        directories: Collection<Path>,
        filter: PathFilter,
    ): FindDuplicatesExecution

    fun find(fromState: FindDuplicatesExecutionState): FindDuplicatesExecution
}
