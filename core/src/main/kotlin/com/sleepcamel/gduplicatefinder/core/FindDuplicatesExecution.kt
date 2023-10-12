package com.sleepcamel.gduplicatefinder.core

import java.nio.file.Path

data class DuplicateGroup(val hash: String, val paths: Collection<Path>)

interface FindDuplicatesExecution {
    suspend fun duplicateEntries(): Collection<DuplicateGroup>
}

class CoroutinesFindDuplicatesExecution: FindDuplicatesExecution {
    override suspend fun duplicateEntries(): Collection<DuplicateGroup> {
        TODO("Not yet implemented")
    }


}
