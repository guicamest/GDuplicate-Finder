package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.runningFold
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
    initialState: FindDuplicatesExecutionState,
) : FindDuplicatesExecution {
    private val stateHolder: FindProgressStateHolder = FindProgressStateHolder(initialState)
    private val result = CompletableDeferred<Collection<DuplicateGroup>>()
    private val job: Job

    constructor(
        scope: CoroutineScope,
        directories: Collection<Path>,
        filter: PathFilter,
    ) : this(
        scope = scope,
        initialState = ScanExecutionStateImpl.empty(initialDirectories = directories, filter = filter),
    )

    init {
        job =
            scope.launch(CoroutineName("findDuplicatesExecution")) {
                if (stateHolder stateIs ScanExecutionState::class) {
                    collectFiles(stateHolder)
                    updateStateToSizeFilter()
                }

                delay(1L)
                if (stateHolder stateIs SizeFilterExecutionState::class) {
                    groupFilesBySize(stateHolder)
                    updateStateToContentFilter()
                }

                delay(1)
                val withSameContent = groupFilesByContent()
                result.complete(withSameContent.flatten())
            }
    }

    private suspend fun groupFilesBySize(stateHolder: FindProgressStateHolder) {
        groupBySize(stateHolder)
            .filter { (_, paths) -> paths.size > 1 }.also {
                stateHolder.update { currentState ->
                    check(currentState is SizeFilterExecutionStateImpl)
                    currentState.copy(processedFiles = it)
                }
            }
    }

    private suspend inline fun groupBySize(
        stateHolder: FindProgressStateHolder,
    ): Map<Long, List<PathWithAttributes>> {
        val filesToProcess = stateHolder.stateAs<SizeFilterExecutionState>().filesToProcess

        filesToProcess.asFlow()
            .runningFold<PathWithAttributes, Pair<Map<Long, List<PathWithAttributes>>, PathWithAttributes?>>(
                emptyMap<Long, List<PathWithAttributes>>() to null,
            ) { (map, _), file ->
                val key = file.size()
                val entriesWithSameSize = map.getOrDefault(key, listOf())

                (map + (key to (entriesWithSameSize + file))) to file
            }.collect { (map, element) ->
                stateHolder.update { currentState ->
                    check(currentState is SizeFilterExecutionStateImpl)

                    val leftToProcess =
                        if (element != null) {
                            (currentState.filesToProcess - element)
                        } else {
                            currentState.filesToProcess
                        }

                    currentState.copy(
                        filesToProcess = leftToProcess,
                        processedFiles = map,
                    )
                }
            }
        return stateHolder.stateAs<SizeFilterExecutionStateImpl>().processedFiles
    }

    private fun groupFilesByContent(): List<Collection<DuplicateGroup>> {
        val groupsToProcess = stateHolder.stateAs<ContentFilterExecutionState>().groupsToProcess
        return groupsToProcess.map { groupWithSameSize ->
            groupWithSameSize.withSameContent().duplicateGroups()
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
                processedFiles = emptyMap(),
            )
        }
    }

    private fun updateStateToContentFilter() {
        stateHolder.update { currentState ->
            check(currentState is SizeFilterExecutionState)
            ContentFilterExecutionStateImpl(
                groupsToProcess = currentState.processedFiles.values,
                processedFiles = emptyMap(),
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
