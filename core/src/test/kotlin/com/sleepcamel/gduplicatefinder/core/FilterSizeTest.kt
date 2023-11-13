package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@DisplayName("DuplicateFinder should")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FilterSizeTest {
    @DisplayName("detect when content size is 12")
    @ParameterizedTest(name = "{1} duplicate groups with filter {0}")
    @MethodSource("sizeFilters")
    fun `parametrized filter test`(
        filter: PathFilter,
        expectedDuplicateGroups: Int,
    ) {
        val (directory, _) = createDuplicates(content = "some content")

        runTest {
            val execution =
                findDuplicates(
                    parentScope = this,
                    directory = directory,
                    filter = filter,
                )

            val duplicateEntries = execution.duplicateEntries()
            assertThat(duplicateEntries).hasSize(expectedDuplicateGroups)
        }
    }

    private fun sizeFilters() =
        Stream.of(
            arguments(MinSizeFilter(100), 0),
            arguments(MinSizeFilter(8), 1),
            arguments(MaxSizeFilter(100), 1),
            arguments(MaxSizeFilter(8), 0),
        )
}
