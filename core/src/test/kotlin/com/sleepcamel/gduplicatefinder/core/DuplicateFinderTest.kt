package com.sleepcamel.gduplicatefinder.core

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.*
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
        fun `directory exists and has no files`(){
            val emptyDirectory = createTempDirectory("emptyDirectory")
            assertThat(emptyDirectory).isEmptyDirectory()

            val execution = duplicateFinder.find(listOf(emptyDirectory))
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and has only one file`(){
            val directoryWithOneFile = createTempDirectory("directoryWithOneFile")
            assertThat(createTempFile(directory = directoryWithOneFile)).exists().isRegularFile()

            val execution = duplicateFinder.find(listOf(directoryWithOneFile))
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and has two different files`(){
            val directoryWith2DifferentFiles = createTempDirectory("directoryWith2DifferentFiles")
            createTempFile(directory = directoryWith2DifferentFiles).writeText("hi")
            createTempFile(directory = directoryWith2DifferentFiles).writeText("bye")

            val execution = duplicateFinder.find(listOf(directoryWith2DifferentFiles))
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and has two files with same size and different content`(){
            val directoryWith2DifferentFiles = createTempDirectory("directoryWith2DifferentFiles")
            val hiFile = createTempFile(directory = directoryWith2DifferentFiles).apply { writeText("hi") }
            val byFile = createTempFile(directory = directoryWith2DifferentFiles).apply { writeText("by") }
            assertThat(hiFile).hasSize(byFile.fileSize())

            val execution = duplicateFinder.find(listOf(directoryWith2DifferentFiles))
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and is not readable`() {
            MemoryFileSystemBuilder.newLinux().build().use { fs ->
                val notReadableDirectory = fs.getPath("/notReadable").createDirectory()
                repeat(2) {
                    createTempFile(directory = notReadableDirectory).writeText("hi")
                }
                val notReadablePermissions = PosixFilePermissions.fromString("-".repeat(9))
                notReadableDirectory.setPosixFilePermissions(notReadablePermissions)
                assertThat(notReadableDirectory).isNot(
                    Condition({ file -> file.isReadable() }, "not readable")
                )

                val execution = duplicateFinder.find(listOf(notReadableDirectory))
                runTest {
                    assertThat(execution.duplicateEntries()).isEmpty()
                }
            }
        }
    }

}