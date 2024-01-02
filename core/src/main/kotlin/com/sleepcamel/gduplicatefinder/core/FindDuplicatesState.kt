/*
 * Copyright 2012-2024 guicamest
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.file.Path

sealed interface FindDuplicatesExecutionState

interface ScanDirectoriesState : FindDuplicatesExecutionState {
    val initialDirectories: Collection<Path>
    val filter: PathFilter
    val visitedDirectories: Set<Path>
    val filesToProcess: Set<PathWithAttributes>
}

data class ScanDirectoriesStateImpl(
    override val initialDirectories: Collection<Path>,
    override val filter: PathFilter,
    override val visitedDirectories: Set<Path>,
    override val filesToProcess: Set<PathWithAttributes>,
) : ScanDirectoriesState {
    companion object {
        internal fun empty(
            initialDirectories: Collection<Path>,
            filter: PathFilter,
        ): ScanDirectoriesState =
            ScanDirectoriesStateImpl(
                initialDirectories = initialDirectories,
                filter = filter,
                visitedDirectories = emptySet(),
                filesToProcess = emptySet(),
            )
    }
}

interface SizeCompareState : FindDuplicatesExecutionState {
    val filesToProcess: Set<PathWithAttributes>
    val processedFiles: Set<PathWithAttributes>
}

data class SizeCompareStateImpl(
    override val filesToProcess: Set<PathWithAttributes>,
    override val processedFiles: Set<PathWithAttributes>,
) : SizeCompareState

interface ContentCompareState : FindDuplicatesExecutionState {
    val filesToProcess: Set<PathWithAttributes>
    val processedFiles: Set<PathWithAttributesAndContent>
}

data class ContentCompareStateImpl(
    override val filesToProcess: Set<PathWithAttributes>,
    override val processedFiles: Set<PathWithAttributesAndContent>,
) : ContentCompareState

internal class FindProgressStateHolder(initial: FindDuplicatesExecutionState) {
    private val _state = MutableStateFlow(initial)
    val state = _state.asStateFlow() // read-only public view

    internal inline fun <reified A : FindDuplicatesExecutionState, O : FindDuplicatesExecutionState> update(
        function: (A) -> O,
    ) {
        _state.update { currentState ->
            check(currentState is A)
            function(currentState)
        }
    }

    inline fun <reified T : FindDuplicatesExecutionState> stateAs(): T =
        with(state.value) {
            check(this is T) { "Expected ${T::class.java} but got ${this::class.java}" }
            this
        }
}

internal fun FindProgressStateHolder.updateStateToSizeCompare() =
    update { currentState: ScanDirectoriesState ->
        SizeCompareStateImpl(
            filesToProcess = currentState.filesToProcess,
            processedFiles = emptySet(),
        )
    }

internal fun FindProgressStateHolder.updateStateToContentCompare() =
    update { currentState: SizeCompareState ->
        ContentCompareStateImpl(
            filesToProcess = currentState.processedFiles,
            processedFiles = emptySet(),
        )
    }
