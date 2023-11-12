package com.sleepcamel.gduplicatefinder.core

import com.sleepcamel.gduplicatefinder.core.pathfilter.MinSizeFilter
import java.nio.file.Path

interface DuplicateFinder {
    fun find(
        directories: Collection<Path>,
        filter: MinSizeFilter,
    ): FindDuplicatesExecution
}
