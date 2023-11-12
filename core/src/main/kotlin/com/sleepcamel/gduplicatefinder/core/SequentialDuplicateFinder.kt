package com.sleepcamel.gduplicatefinder.core

import com.sleepcamel.gduplicatefinder.core.pathfilter.MinSizeFilter
import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path

class SequentialDuplicateFinder(private val parentScope: CoroutineScope) : DuplicateFinder {
    override fun find(
        directories: Collection<Path>,
        filter: MinSizeFilter,
    ): FindDuplicatesExecution = CoroutinesFindDuplicatesExecution(directories, filter, parentScope)
}
