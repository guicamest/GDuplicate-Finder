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
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.visitFileTree
import kotlin.reflect.KClass

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

    override suspend fun duplicateEntries(): Collection<DuplicateGroup> = result.await()

    override suspend fun stop(): FindDuplicatesExecutionState {
        job.cancel(FindExecutionStopRequested())
        job.join()
        return stateHolder.state.value
    }

    override val state get() = stateHolder.state

    init {
        job =
            scope.launch(CoroutineName("findDuplicatesExecution")) {
                if (ScanExecutionState::class in stateHolder) {
                    collectFiles(stateHolder)
                    stateHolder.updateStateToSizeFilter()
                }

                delay(1L)
                if (SizeFilterExecutionState::class in stateHolder) {
                    groupFilesBySize(stateHolder)
                    stateHolder.updateStateToContentFilter()
                }

                delay(1)
                groupFilesByContent(stateHolder)
                result.complete(
                    stateHolder.stateAs<ContentFilterExecutionState>().processedFiles.duplicateGroups(),
                )
            }
    }

    private fun collectFiles(stateHolder: FindProgressStateHolder) {
        val (initialDirectories, filter) =
            stateHolder.stateAs<ScanExecutionState>().run {
                initialDirectories to filter
            }
        val visitor = ScanFileVisitor(filter, stateHolder)
        initialDirectories.uniqueAndReal.forEach { directory ->
            directory.visitFileTree(visitor = visitor, followLinks = true)
        }
    }

    private fun groupFilesBySize(stateHolder: FindProgressStateHolder) {
        val filesToProcess = stateHolder.stateAs<SizeFilterExecutionState>().filesToProcess
        val destination = HashMap<Long, MutableList<PathWithAttributes>>(initialSize(filesToProcess))
        val filteredFiles =
            filesToProcess
                .groupByTo(destination) { it.size() }
                .entries
                .asSequence()
                .filter { it.value.size > 1 }
                .flatMap { it.value }
                .toSet()

        stateHolder.update { currentState: SizeFilterExecutionStateImpl ->
            currentState.copy(processedFiles = filteredFiles, filesToProcess = emptySet())
        }
    }

    private fun initialSize(filesToProcess: Set<Any>) = filesToProcess.size / 2

    private fun groupFilesByContent(stateHolder: FindProgressStateHolder) {
        val filesWithHashes =
            with(stateHolder.stateAs<ContentFilterExecutionState>()) {
                processedFiles + collectHashes(stateHolder, groupsToProcess)
            }

        val destination =
            HashMap<String, MutableList<PathWithAttributesAndContent>>(initialSize(filesWithHashes))

        val filteredFiles =
            filesWithHashes
                .groupByTo(destination) { it.contentHash }
                .entries
                .asSequence()
                .filter { it.value.size > 1 }
                .flatMap { it.value }
                .toSet()

        stateHolder.update { currentState: ContentFilterExecutionStateImpl ->
            currentState.copy(processedFiles = filteredFiles)
        }
    }

    private fun collectHashes(
        stateHolder: FindProgressStateHolder,
        filesToProcess: Set<PathWithAttributes>,
    ) = filesToProcess
        .map { pwa ->
            val pathWithAttributesAndContent =
                PathWithAttributesAndContent(
                    path = pwa.path,
                    attributes = pwa.attributes,
                    contentHash = pwa.contentHash(),
                )
            stateHolder.update { currentState: ContentFilterExecutionStateImpl ->
                currentState.copy(
                    groupsToProcess = currentState.groupsToProcess - pwa,
                    processedFiles = currentState.processedFiles + pathWithAttributesAndContent,
                )
            }
            pathWithAttributesAndContent
        }

    private operator fun <T : FindDuplicatesExecutionState> FindProgressStateHolder.contains(
        c: KClass<T>,
    ): Boolean =
        c.isInstance(
            state.value,
        )

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

internal class FindExecutionStopRequested : CancellationException()
