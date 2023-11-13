package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path

class SequentialDuplicateFinder(private val parentScope: CoroutineScope) : DuplicateFinder {
    override fun find(
        directories: Collection<Path>,
        filter: PathFilter,
    ): FindDuplicatesExecution = CoroutinesFindDuplicatesExecution(directories, filter, parentScope)
}
