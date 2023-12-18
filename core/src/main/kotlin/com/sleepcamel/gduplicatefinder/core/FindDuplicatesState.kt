package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.file.Path
import kotlin.reflect.KClass

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

interface ContentFilterExecutionState : FindDuplicatesExecutionState {
    val groupsToProcess: Map<Long, List<PathWithAttributes>>
    val processedFiles: Map<String, List<PathWithAttributes>>
}

data class ContentFilterExecutionStateImpl(
    override val groupsToProcess: Map<Long, List<PathWithAttributes>>,
    override val processedFiles: Map<String, List<PathWithAttributes>>,
) : ContentFilterExecutionState

internal class FindProgressStateHolder(initial: FindDuplicatesExecutionState) {
    private val _state = MutableStateFlow(initial)
    val state = _state.asStateFlow() // read-only public view

    internal fun update(function: (FindDuplicatesExecutionState) -> FindDuplicatesExecutionState) {
        _state.update(function)
    }

    inline fun <reified T : FindDuplicatesExecutionState> stateAs(): T =
        with(state.value) {
            check(this is T) { "Expected ${T::class.java} but got ${this::class.java}" }
            this
        }

    infix fun <T : FindDuplicatesExecutionState> stateIs(c: KClass<T>): Boolean = c.isInstance(state.value)
}
