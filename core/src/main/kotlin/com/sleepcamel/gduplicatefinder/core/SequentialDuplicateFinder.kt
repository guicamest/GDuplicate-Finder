package com.sleepcamel.gduplicatefinder.core

import java.nio.file.Path

class SequentialDuplicateFinder: DuplicateFinder {
    override fun find(directories: Collection<Path>): FindDuplicatesExecution {
        return CoroutinesFindDuplicatesExecution()
    }

}
