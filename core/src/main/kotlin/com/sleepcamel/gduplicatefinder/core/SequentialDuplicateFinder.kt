package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path

class SequentialDuplicateFinder(private val parentScope: CoroutineScope) : DuplicateFinder {
    override fun find(
        directories: Collection<Path>,
        filter: PathFilter,
    ): FindDuplicatesExecution = CoroutinesFindDuplicatesExecution(parentScope, directories, filter)

    override fun find(fromState: FindDuplicatesExecutionState): FindDuplicatesExecution =
        CoroutinesFindDuplicatesExecution(scope = parentScope, state = fromState)
}
