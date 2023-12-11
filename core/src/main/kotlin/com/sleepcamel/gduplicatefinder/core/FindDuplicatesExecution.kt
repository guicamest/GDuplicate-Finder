package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.visitFileTree

data class DuplicateGroup(val hash: String, val paths: Collection<PathWithAttributes>)

interface FindDuplicatesExecution {
    suspend fun duplicateEntries(): Collection<DuplicateGroup>

    suspend fun stop(): FindDuplicatesExecutionState

    val state: StateFlow<FindDuplicatesExecutionState>
}

@OptIn(ExperimentalPathApi::class)
class CoroutinesFindDuplicatesExecution(
    scope: CoroutineScope,
    state: FindDuplicatesExecutionState,
) : FindDuplicatesExecution {
    private val stateHolder: FindProgressStateHolder = FindProgressStateHolder(state)
    private val result = CompletableDeferred<Collection<DuplicateGroup>>()
    private val job: Job

    constructor(
        scope: CoroutineScope,
        directories: Collection<Path>,
        filter: PathFilter,
    ) : this(
        scope = scope,
        state = ScanExecutionStateImpl.empty(initialDirectories = directories, filter = filter),
    )

    init {
        job =
            scope.launch(CoroutineName("findDuplicatesExecution")) {
                if (state is ScanExecutionState) {
                    collectFiles(stateHolder)
                    updateStateToSizeFilter()
                }
                val allFiles = stateHolder.stateAs<SizeFilterExecutionState>().filesToProcess

                delay(1L)

                val withSameSize = allFiles.withSameSize()

                stateHolder.update(ContentFilterExecutionStateImpl())
                delay(1)
                val withSameContent =
                    withSameSize.mapNotNull { (_, groupWithSameSize) ->
                        groupWithSameSize.withSameContent().duplicateGroups()
                    }
                result.complete(withSameContent.flatten())
            }
    }

    override suspend fun duplicateEntries(): Collection<DuplicateGroup> = result.await()

    override suspend fun stop(): FindDuplicatesExecutionState {
        job.cancel(FindExecutionStopRequested())
        job.join()
        return stateHolder.state.value
    }

    override val state get() = stateHolder.state

    private fun collectFiles(stateHolder: FindProgressStateHolder) {
        val (initialDirectories, filter) =
            stateHolder.state.value.run {
                check(this is ScanExecutionState)
                initialDirectories to filter
            }
        val visitor = ScanFileVisitor(filter, stateHolder)
        initialDirectories.uniqueAndReal.forEach { directory ->
            directory.visitFileTree(visitor = visitor, followLinks = true)
        }
    }

    private fun updateStateToSizeFilter() {
        stateHolder.update { currentState ->
            check(currentState is ScanExecutionState)
            SizeFilterExecutionStateImpl(
                filesToProcess = currentState.filesToProcess,
            )
        }
    }

    companion object {
        private val Collection<Path>.uniqueAndReal: Collection<Path>
            get() =
                mapNotNull {
                    try {
                        it.toRealPath()
                    } catch (e: NoSuchFileException) {
                        null
                    }
                }.distinct()
    }
}

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

internal class FindExecutionStopRequested : CancellationException()
