package com.sleepcamel.gduplicatefinder.core

import com.sleepcamel.gduplicatefinder.core.pathfilter.MinSizeFilter
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.io.path.absolute
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import kotlin.test.Test

@DisplayName("DuplicateFinder should")
class FilterSizeTest {
    @DisplayName("not detect duplicates when")
    @Nested
    inner class NoDuplicates {
        @Test
        fun `duplicate files size is less than minSize`() {
            val directoryWith2FilesSameHash = createTempDirectory("directoryWith2FilesSameHash")
            repeat(2) {
                createTempFile(directory = directoryWith2FilesSameHash).apply {
                    writeText("hi")
                }
            }

            runTest {
                val execution =
                    findDuplicates(
                        parentScope = this,
                        directories = listOf(directoryWith2FilesSameHash),
                        filter = MinSizeFilter(3L),
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
            val directoryWith2FilesSameHash = createTempDirectory("directoryWith2FilesSameHash")
            val fileNames =
                (0..1).map {
                    createTempFile(directory = directoryWith2FilesSameHash).apply {
                        writeText("hi")
                    }.absolute()
                }
            val expectedDuplicateGroup =
                DuplicateGroup(
                    hash = "hi".contentHash(),
                    paths = fileNames,
                )

            runTest {
                val execution =
                    findDuplicates(
                        parentScope = this,
                        directories = listOf(directoryWith2FilesSameHash),
                        filter = MinSizeFilter(1L),
                    )

                val duplicateEntries = execution.duplicateEntries()
                assertThat(duplicateEntries).hasSize(1)
                duplicateEntries.first().also { group ->
                    assertThat(group.hash).isEqualTo(expectedDuplicateGroup.hash)
                    assertThat(group.paths).containsExactlyInAnyOrderElementsOf(expectedDuplicateGroup.paths)
                }
            }
        }
    }
}