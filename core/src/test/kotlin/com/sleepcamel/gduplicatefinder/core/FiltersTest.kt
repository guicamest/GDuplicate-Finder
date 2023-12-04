package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.io.path.absolute
import kotlin.io.path.createDirectory
import kotlin.io.path.createTempDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.writeText

@DisplayName("DuplicateFinder should")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FiltersTest {
    @DisplayName("detect when content size is 12")
    @ParameterizedTest(name = "{1} duplicate groups with filter {0}")
    @MethodSource("sizeFilters")
    fun `parametrized size filter test`(
        filter: PathFilter,
        expectedDuplicateGroups: Int,
    ) {
        val directory = createDuplicates(content = "some content")

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

    @DisplayName("detect when filename is somefile*.txt")
    @ParameterizedTest(name = "{1} duplicate groups with filter {0}")
    @MethodSource("nameFilters")
    fun `parametrized file name with extension filter test`(
        filter: PathFilter,
        expectedDuplicateGroups: Int,
    ) {
        val directory = createDuplicates(filenamePrefixAndSuffix = "somefile" to ".txt")

        runTest {
            val execution =
                findDuplicates(
                    parentScope = this,
                    directory = directory,
                    filter = filter,
                )

            val duplicateEntries = execution.duplicateEntries()
            assertThat(duplicateEntries)
                .withRepresentation {
                    "$it\nFiles in directory: ${directory.listDirectoryEntries().joinToString()}"
                }
                .hasSize(expectedDuplicateGroups)
        }
    }

    private fun nameFilters() =
        Stream.of(
            arguments(FilenameFilter(name = "somefile", exact = false), 1),
            arguments(FilenameFilter(name = "donotfind", exact = false), 0),
            arguments(FilenameFilter(name = "somefile.txt", exact = false), 0),
            arguments(FilenameFilter(name = "somefile", exact = true), 0),
            arguments(ExtensionFilter(extension = "txt", exact = false), 1),
            arguments(ExtensionFilter(extension = "(exe|txt|bin)", exact = false), 1),
            arguments(ExtensionFilter(extension = "tx", exact = false), 1),
            arguments(ExtensionFilter(extension = "(exe|bin)", exact = false), 0),
            arguments(ExtensionFilter(extension = "txtx", exact = false), 0),
            arguments(ExtensionFilter(extension = "txt", exact = true), 1),
            arguments(ExtensionFilter(extension = "tx", exact = true), 0),
            arguments(FullFilenameFilter(name = "somefile", exact = false), 1),
            arguments(FullFilenameFilter(name = "donotfind", exact = false), 0),
            arguments(FullFilenameFilter(name = "somefile.txt", exact = false), 0),
            arguments(FullFilenameFilter(name = "somefile.*TXT", exact = false), 0),
            arguments(FullFilenameFilter(name = "somefile.*(TXT|txt)", exact = false), 1),
            arguments(FullFilenameFilter(name = "somefile.*.txt", exact = false), 1),
            arguments(FullFilenameFilter(name = "somefile.*\\.txt", exact = false), 1),
            arguments(FullFilenameFilter(name = "somefile.*.txt", exact = true), 1),
            arguments(FullFilenameFilter(name = "somefile.*\\.txt", exact = true), 1),
            arguments(FullFilenameFilter(name = "e.*\\.t", exact = false), 1),
            arguments(FullFilenameFilter(name = "e.*\\.t", exact = true), 0),
        )

    @DisplayName("detect when files are inside directory named paramDir")
    @ParameterizedTest(name = "{1} duplicate groups with filter {0}")
    @MethodSource("directoryFilters")
    fun `parametrized directory name filter test`(
        filter: PathFilter,
        expectedDuplicateGroups: Int,
    ) {
        val temp = createTempDirectory()
        val directory = createDuplicates(directory = temp.resolve("paramDir").createDirectory())

        runTest {
            val execution =
                findDuplicates(
                    parentScope = this,
                    directory = directory,
                    filter = filter,
                )

            val duplicateEntries = execution.duplicateEntries()
            assertThat(duplicateEntries)
                .withRepresentation {
                    "$it\nFiles in directory: ${directory.listDirectoryEntries().joinToString()}"
                }
                .hasSize(expectedDuplicateGroups)
        }
    }

    private fun directoryFilters() =
        Stream.of(
            arguments(DirectoryFilter(name = "somefile", exact = false), 0),
            arguments(DirectoryFilter(name = "paramDir", exact = true), 1),
            arguments(DirectoryFilter(name = "ramDi", exact = false), 1),
            arguments(DirectoryFilter(name = "(build|paramDir|test)", exact = false), 1),
            arguments(DirectoryFilter(name = "(?!paramDir)", exact = true), 0),
        )

    @DisplayName("detect when file paths are **/someDirectory/atextfile.txt and aclassfile.class")
    @ParameterizedTest(name = "{1} duplicate groups with filter {0}")
    @MethodSource("pathMatcherFilters")
    fun `parametrized pathmatcher filter test`(
        filter: PathFilter,
        expectedDuplicateGroups: Int,
    ) {
        val directory = createTempDirectory().resolve("someDirectory").createDirectory()
        listOf("atextfile.txt", "aclassfile.class").map {
            directory.resolve(it).apply {
                writeText("content of the file")
            }.absolute()
        }

        runTest {
            val execution =
                findDuplicates(
                    parentScope = this,
                    directory = directory,
                    filter = filter,
                )

            val duplicateEntries = execution.duplicateEntries()
            assertThat(duplicateEntries)
                .withRepresentation {
                    "$it\nFiles in directory: ${directory.listDirectoryEntries().joinToString()}"
                }
                .hasSize(expectedDuplicateGroups)
        }
    }

    private fun pathMatcherFilters() =
        Stream.of(
            arguments(PathMatcherFilter("glob:**/*.{txt,class}"), 1),
            arguments(PathMatcherFilter("glob:*.{txt,class}"), 0),
            arguments(PathMatcherFilter("glob:**/b*.{txt,class}"), 0),
            arguments(PathMatcherFilter("glob:**/*.{class}"), 0),
            arguments(PathMatcherFilter("glob:**/*.{txt}"), 0),
            arguments(PathMatcherFilter("regex:.*(txt|class)"), 1),
            arguments(PathMatcherFilter("regex:.*a\\w+file\\..*"), 1),
            arguments(PathMatcherFilter("regex:.*someDirectory.*"), 1),
            arguments(PathMatcherFilter("regex:.*anotherdir.*"), 0),
        )
}
