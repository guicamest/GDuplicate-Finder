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
            assertThat(state).isInstanceOf(ScanExecutionState::class.java)
        }

    @ExperimentalCoroutinesApi
    @Test
    @DisplayName("return size filter state when `stop` is called")
    fun stopReturnsStateAfterScan() =
        runTest {
            val state =
                findDuplicates(this, directory = searchDirectory).run {
                    advanceTimeBy(1)
                    stop()
                }
            assertThat(state).isInstanceOf(SizeFilterExecutionState::class.java)
        }

    @ExperimentalCoroutinesApi
    @Test
    @DisplayName("return content filter state when `stop` is called")
    fun stopReturnsStateAfterSizeFilter() =
        runTest {
            val state =
                findDuplicates(this, directory = searchDirectory).run {
                    advanceTimeBy(1)
                    advanceTimeBy(1)
                    stop()
                }
            assertThat(state).isInstanceOf(ContentFilterExecutionState::class.java)
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

            assertThat(allStates.first()).isInstanceOf(ScanExecutionState::class.java)

            val withoutScans = allStates.dropWhile { it is ScanExecutionState }
            assertThat(withoutScans.first()).isInstanceOf(SizeFilterExecutionState::class.java)

            val withoutSizeFilter = withoutScans.dropWhile { it is SizeFilterExecutionState }
            assertThat(withoutSizeFilter).isNotEmpty.allMatch { it is ContentFilterExecutionState }
        }

    @DisplayName("update scan state as it visits files/directories")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ScanStateTest {
        private lateinit var scanStates: List<ScanExecutionState>

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
                scanStates = allStates.takeWhile { it is ScanExecutionState }.map { it as ScanExecutionState }
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
                val withoutScans = allStates.dropWhile { it is ScanExecutionState }

                val execution = resumeFindDuplicates(this, fromState = withoutScans.first())
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
