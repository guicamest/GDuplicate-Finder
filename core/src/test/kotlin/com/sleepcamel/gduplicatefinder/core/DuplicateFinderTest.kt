package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.nio.file.Paths
import kotlin.io.path.createTempDirectory
import kotlin.test.Test

@DisplayName("DuplicateFinder should")
class DuplicateFinderTest {

    val duplicateFinder = SequentialDuplicateFinder()

    @DisplayName("not detect duplicates when")
    @Nested
    inner class NoDuplicates {

        @Test
        fun `directory does not exist`(){
            val nonExistentDirectory = Paths.get("nonExistentDirectory")
            assertThat(nonExistentDirectory).doesNotExist()

            val execution = duplicateFinder.find(listOf(nonExistentDirectory))
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists but has no files`(){
            val emptyDirectory = createTempDirectory("emptyDirectory")
            assertThat(emptyDirectory).isEmptyDirectory()

            val execution = duplicateFinder.find(listOf(emptyDirectory))
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }
    }

}