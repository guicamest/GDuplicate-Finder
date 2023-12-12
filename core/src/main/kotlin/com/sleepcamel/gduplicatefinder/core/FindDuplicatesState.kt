package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.file.Path

sealed interface FindDuplicatesExecutionState

interface ScanExecutionState : FindDuplicatesExecutionState {
    val initialDirectories: Collection<Path>
    val filter: PathFilter
    val visitedDirectories: Set<Path>
    val filesToProcess: Set<PathWithAttributes>
}

data class ScanExecutionStateImpl(
    override val initialDirectories: Collection<Path>,
    override val filter: PathFilter,
    override val visitedDirectories: Set<Path>,
    override val filesToProcess: Set<PathWithAttributes>,
) : ScanExecutionState {
    companion object {
        internal fun empty(
            initialDirectories: Collection<Path>,
            filter: PathFilter,
        ): ScanExecutionState =
            ScanExecutionStateImpl(
                initialDirectories = initialDirectories,
                filter = filter,
                visitedDirectories = emptySet(),
                filesToProcess = emptySet(),
            )
    }
}

interface SizeFilterExecutionState : FindDuplicatesExecutionState {
    val filesToProcess: Set<PathWithAttributes>
    val processedFiles: Map<Long, List<PathWithAttributes>>
}

data class SizeFilterExecutionStateImpl(
    override val filesToProcess: Set<PathWithAttributes>,
    override val processedFiles: Map<Long, List<PathWithAttributes>>,
) : SizeFilterExecutionState

interface ContentFilterExecutionState : FindDuplicatesExecutionState

class ContentFilterExecutionStateImpl : ContentFilterExecutionState

internal class FindProgressStateHolder(initial: FindDuplicatesExecutionState) {
    private val _state = MutableStateFlow(initial)
    val state = _state.asStateFlow() // read-only public view

    internal fun update(newValue: FindDuplicatesExecutionState) {
        _state.value = newValue // NOT suspending
    }

    internal fun update(function: (FindDuplicatesExecutionState) -> FindDuplicatesExecutionState) {
        _state.update(function)
    }

    inline fun <reified T : FindDuplicatesExecutionState> stateAs(): T =
        with(state.value) {
            check(this is T)
            this
        }
}
