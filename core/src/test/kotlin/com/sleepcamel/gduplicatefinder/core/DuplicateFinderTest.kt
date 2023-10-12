package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.nio.file.Paths
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
            val execution = duplicateFinder.find(listOf(nonExistentDirectory))

            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }
    }

}