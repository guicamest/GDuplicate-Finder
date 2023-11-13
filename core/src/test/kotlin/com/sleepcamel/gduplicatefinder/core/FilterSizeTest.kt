package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.Test

@DisplayName("DuplicateFinder should")
class FilterSizeTest {
    @DisplayName("not detect duplicates when")
    @Nested
    inner class NoDuplicates {
        @Test
        fun `duplicate files size is less than minSize`() {
            val (directory, _) = createDuplicates(content = "some content")

            runTest {
                val execution =
                    findDuplicates(
                        parentScope = this,
                        directory = directory,
                        filter = MinSizeFilter(100L),
                    )

                val duplicateEntries = execution.duplicateEntries()
                assertThat(duplicateEntries).isEmpty()
            }
        }

        @Test
        fun `duplicate files size is more than maxSize`() {
            val (directory, _) = createDuplicates(content = "some content")

            runTest {
                val execution =
                    findDuplicates(
                        parentScope = this,
                        directory = directory,
                        filter = MaxSizeFilter(5L),
                    )

                val duplicateEntries = execution.duplicateEntries()
                assertThat(duplicateEntries).isEmpty()
            }
        }
    }

    @DisplayName("detect duplicates when")
    @Nested
    inner class Duplicates {
        @Test
        fun `duplicate files size is more than minSize`() {
            val (directory, _) = createDuplicates(content = "some content")

            runTest {
                val execution =
                    findDuplicates(
                        parentScope = this,
                        directory = directory,
                        filter = MinSizeFilter(1L),
                    )

                val duplicateEntries = execution.duplicateEntries()
                assertThat(duplicateEntries).hasSize(1)
            }
        }
    }
}
