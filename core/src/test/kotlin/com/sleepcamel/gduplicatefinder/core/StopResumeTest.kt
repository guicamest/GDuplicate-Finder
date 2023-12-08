package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes.withDebugProbes
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
    @DisplayName("stop should stop the finding execution")
    fun executionCanBeStopped() =
        withDebugProbes {
            runTest {
                findDuplicates(this, directory = searchDirectory).stop()
                assertThat(findCoroutine("findDuplicatesExecution"))
                    .withFailMessage { "Coroutine shouldn't be running" }
                    .isNull()
            }
        }
}
