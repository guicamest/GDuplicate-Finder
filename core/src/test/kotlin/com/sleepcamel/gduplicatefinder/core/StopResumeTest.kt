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

    @BeforeAll
    fun setupFiles() {
        (0..1).forEach {
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
}
