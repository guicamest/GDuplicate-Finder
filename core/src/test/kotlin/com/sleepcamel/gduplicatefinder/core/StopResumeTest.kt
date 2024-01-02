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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes.withDebugProbes
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.writeText

@DisplayName("DuplicateFinder should")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StopResumeTest {
    companion object {
        @TempDir
        lateinit var searchDirectory: Path
    }

    lateinit var paths: List<Path>

    @BeforeAll
    fun setupFiles() {
        paths =
            (0..1).map {
                (searchDirectory / Path.of("file $it.txt")).apply {
                    writeText("hi")
                }
            }
    }

    @ExperimentalCoroutinesApi
    @Test
    @DisplayName("stop the finding execution when `stop` is called")
    fun executionCanBeStopped() =
        withDebugProbes {
            runTest {
                findDuplicates(this, directory = searchDirectory).stop()
                assertThat(findCoroutine("findDuplicatesExecution"))
                    .withFailMessage { "Coroutine shouldn't be running" }
                    .isNull()
            }
        }

    @Test
    @DisplayName("return scan state when `stop` is called")
    fun stopReturnsStateWhileScanning() =
        runTest {
            val state = findDuplicates(this, directory = searchDirectory).stop()
            assertThat(state).isInstanceOf(ScanDirectoriesState::class.java)
        }

    @ExperimentalCoroutinesApi
    @Test
    @DisplayName("return size compare state when `stop` is called")
    fun stopReturnsStateAfterScan() =
        runTest {
            val state =
                findDuplicates(this, directory = searchDirectory).run {
                    advanceTimeBy(1)
                    stop()
                }
            assertThat(state).isInstanceOf(SizeCompareState::class.java)
        }

    @ExperimentalCoroutinesApi
    @Test
    @DisplayName("return content compare state when `stop` is called")
    fun stopReturnsStateAfterSizeCompare() =
        runTest {
            val state =
                findDuplicates(this, directory = searchDirectory).run {
                    advanceTimeBy(1)
                    advanceTimeBy(1)
                    stop()
                }
            assertThat(state).isInstanceOf(ContentCompareState::class.java)
        }

    @ExperimentalCoroutinesApi
    @Test
    @DisplayName("return states in order scan / size / content")
    fun statesAreInOrder() =
        runTest {
            val state = findDuplicates(this, directory = searchDirectory).state
            val allStates = mutableListOf<FindDuplicatesExecutionState>()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                state.toList(allStates)
            }
            advanceUntilIdle()

            assertThat(allStates.first()).isInstanceOf(ScanDirectoriesState::class.java)

            val withoutScans = allStates.dropWhile { it is ScanDirectoriesState }
            assertThat(withoutScans.first()).isInstanceOf(SizeCompareState::class.java)

            val withoutSizeStates = withoutScans.dropWhile { it is SizeCompareState }
            assertThat(withoutSizeStates).isNotEmpty.allMatch { it is ContentCompareState }
        }

    @DisplayName("update scan state as it visits files/directories")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ScanStateTest {
        private lateinit var scanStates: List<ScanDirectoriesState>

        @BeforeAll
        @ExperimentalCoroutinesApi
        fun findDuplicates() {
            runTest {
                val state = findDuplicates(this, directory = searchDirectory).state
                val allStates = mutableListOf<FindDuplicatesExecutionState>()

                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    state.toList(allStates)
                }
                advanceUntilIdle()
                scanStates = allStates.filterIsInstance<ScanDirectoriesState>()
            }
        }

        @Test
        fun thereShouldBeFourScanStates() {
            assertThat(scanStates).hasSize(4)
        }

        @Test
        fun allStatesShouldHaveCorrectInitialDirectoriesAndFilter() {
            assertThat(scanStates).allSatisfy { state ->
                assertThat(state.initialDirectories).isEqualTo(listOf(searchDirectory))
                assertThat(state.filter).isEqualTo(MinSizeFilter(0))
            }
        }

        @Test
        fun checkFirstState() {
            assertThat(scanStates[0].visitedDirectories).isEmpty()
            assertThat(scanStates[0].filesToProcess).isEmpty()
        }

        @Test
        fun checkSecondState() {
            assertThat(scanStates[1].visitedDirectories).isEmpty()
            assertThat(scanStates[1].filesToProcess).hasSize(1)
            assertThat(scanStates[1].filesToProcess.map { it.path })
                .withPathComparator()
                .containsAnyElementsOf(paths)
        }

        @Test
        fun checkThirdState() {
            assertThat(scanStates[2].visitedDirectories).isEmpty()
            assertThat(scanStates[2].filesToProcess).hasSize(2)
            assertThat(scanStates[2].filesToProcess.map { it.path })
                .withPathComparator()
                .containsExactlyInAnyOrderElementsOf(paths)
        }

        @Test
        fun checkLastState() {
            assertThat(scanStates.last().visitedDirectories)
                .withPathComparator()
                .containsExactly(searchDirectory)
            assertThat(scanStates[2].filesToProcess).hasSize(2)
            assertThat(scanStates[2].filesToProcess.map { it.path })
                .withPathComparator()
                .containsExactlyInAnyOrderElementsOf(paths)
        }
    }

    @DisplayName("update size state as it visits files")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SizeStateTest {
        private lateinit var sizeCompareStates: List<SizeCompareState>

        @BeforeAll
        @ExperimentalCoroutinesApi
        fun findDuplicates() {
            runTest {
                val state = findDuplicates(this, directory = searchDirectory).state
                val allStates = mutableListOf<FindDuplicatesExecutionState>()

                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    state.toList(allStates)
                }
                advanceUntilIdle()
                sizeCompareStates =
                    allStates.dropWhile {
                        it is ScanDirectoriesState
                    }.takeWhile { it is SizeCompareState }.map { it as SizeCompareState }
            }
        }

        @Test
        fun thereShouldBeTwoSizeStates() {
            assertThat(sizeCompareStates).hasSize(2)
        }

        @Test
        fun checkFirstState() {
            assertThat(sizeCompareStates[0].filesToProcess.map { it.path })
                .withPathComparator()
                .containsExactlyInAnyOrderElementsOf(paths)
            assertThat(sizeCompareStates[0].processedFiles).isEmpty()
        }

        @Test
        fun checkSecondState() {
            assertThat(sizeCompareStates[1].filesToProcess).isEmpty()
            assertThat(sizeCompareStates[1].processedFiles.map { it.path })
                .withPathComparator()
                .containsExactlyInAnyOrderElementsOf(paths)
        }
    }

    @DisplayName("update content state as it visits files")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ContentStateTest {
        private lateinit var contentStates: List<ContentCompareState>

        @BeforeAll
        @ExperimentalCoroutinesApi
        fun findDuplicates() {
            runTest {
                val state = findDuplicates(this, directory = searchDirectory).state
                val allStates = mutableListOf<FindDuplicatesExecutionState>()

                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    state.toList(allStates)
                }
                advanceUntilIdle()
                contentStates =
                    allStates.dropWhile {
                        it !is ContentCompareState
                    }.takeWhile {
                        it is ContentCompareState
                    }.map { it as ContentCompareState }
            }
        }

        @Test
        fun thereShouldBeThreeContentStates() {
            assertThat(contentStates).hasSize(3)
        }

        @Test
        fun checkFirstState() {
            assertThat(contentStates[0].filesToProcess.map { it.path })
                .withPathComparator()
                .containsExactlyInAnyOrderElementsOf(paths)
            assertThat(contentStates[0].processedFiles).isEmpty()
        }

        @Test
        fun checkSecondState() {
            val leftToProcess = contentStates[1].filesToProcess.map { it.path }
            assertThat(leftToProcess)
                .withPathComparator()
                .containsAnyElementsOf(paths)
                .hasSize(1)
            assertThat(contentStates[1].processedFiles.map { it.path })
                .withPathComparator()
                .containsExactlyInAnyOrderElementsOf(paths.without(leftToProcess))
                .hasSize(1)
        }

        @Test
        fun checkThirdSecondState() {
            assertThat(contentStates[2].filesToProcess).isEmpty()
            assertThat(contentStates[2].processedFiles.map { it.path })
                .withPathComparator()
                .containsExactlyInAnyOrderElementsOf(paths)
        }
    }

    @DisplayName("be able to resume")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ResumeTest {
        @ExperimentalCoroutinesApi
        @Test
        @DisplayName("from scan state")
        fun canResumeFromScanState() {
            runTest {
                val state = findDuplicates(this, directory = searchDirectory).stop()

                val execution = resumeFindDuplicates(this, fromState = state)
                val duplicateEntries = execution.duplicateEntries()
                duplicateEntries.assertOneDuplicateGroupWith(
                    paths = paths,
                    content = "hi",
                    checkAttributes = false,
                )
            }
        }

        @ExperimentalCoroutinesApi
        @Test
        @DisplayName("from size compare state")
        fun canResumeFromSizeCompareState() {
            runTest {
                val state = findDuplicates(this, directory = searchDirectory).state
                val allStates = mutableListOf<FindDuplicatesExecutionState>()
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    state.toList(allStates)
                }
                advanceUntilIdle()
                val withoutScans = allStates.dropWhile { it is ScanDirectoriesState }

                val execution = resumeFindDuplicates(this, fromState = withoutScans.first())
                val duplicateEntries = execution.duplicateEntries()
                duplicateEntries.assertOneDuplicateGroupWith(
                    paths = paths,
                    content = "hi",
                    checkAttributes = false,
                )
            }
        }

        @ExperimentalCoroutinesApi
        @Test
        @DisplayName("from content compare state")
        fun canResumeFromContentCompareState() {
            runTest {
                val state = findDuplicates(this, directory = searchDirectory).state
                val allStates = mutableListOf<FindDuplicatesExecutionState>()
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    state.toList(allStates)
                }
                advanceUntilIdle()
                val contentStates = allStates.dropWhile { it !is ContentCompareState }

                val execution = resumeFindDuplicates(this, fromState = contentStates.first())
                val duplicateEntries = execution.duplicateEntries()
                duplicateEntries.assertOneDuplicateGroupWith(
                    paths = paths,
                    content = "hi",
                    checkAttributes = false,
                )
            }
        }
    }
}
