package com.sleepcamel.gduplicatefinder.core

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions
import java.security.MessageDigest
import kotlin.io.path.absolute
import kotlin.io.path.createDirectory
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.div
import kotlin.io.path.fileSize
import kotlin.io.path.isReadable
import kotlin.io.path.setPosixFilePermissions
import kotlin.io.path.writeText
import kotlin.test.Test

@DisplayName("DuplicateFinder should")
class DuplicateFinderTest {
    val duplicateFinder: DuplicateFinder = SequentialDuplicateFinder(GlobalScope)

    @DisplayName("not detect duplicates when")
    @Nested
    inner class NoDuplicates {
        @Test
        fun `directory does not exist`() {
            val nonExistentDirectory = Paths.get("nonExistentDirectory")
            assertThat(nonExistentDirectory).doesNotExist()

            val execution = duplicateFinder.find(listOf(nonExistentDirectory))
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and has no files`() {
            val emptyDirectory = createTempDirectory("emptyDirectory")
            assertThat(emptyDirectory).isEmptyDirectory()

            val execution = duplicateFinder.find(listOf(emptyDirectory))
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and has only one file`() {
            val directoryWithOneFile = createTempDirectory("directoryWithOneFile")
            assertThat(createTempFile(directory = directoryWithOneFile)).exists().isRegularFile()

            val execution = duplicateFinder.find(listOf(directoryWithOneFile))
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and has two different files`() {
            val directoryWith2DifferentFiles = createTempDirectory("directoryWith2DifferentFiles")
            createTempFile(directory = directoryWith2DifferentFiles).writeText("hi")
            createTempFile(directory = directoryWith2DifferentFiles).writeText("bye")

            val execution = duplicateFinder.find(listOf(directoryWith2DifferentFiles))
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and has two files with same size and different content`() {
            val directoryWith2DifferentFiles = createTempDirectory("directoryWith2FilesSameSizeDifContent")
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
                val notReadableDirectory = createTempDirectory(fs.rootDirectories.first())
                repeat(2) {
                    createTempFile(directory = notReadableDirectory).writeText("hi")
                }
                val notReadablePermissions = PosixFilePermissions.fromString("-".repeat(9))
                notReadableDirectory.setPosixFilePermissions(notReadablePermissions)
                assertThat(notReadableDirectory).isNot(
                    Condition({ file -> file.isReadable() }, "not readable"),
                )

                val execution = duplicateFinder.find(listOf(notReadableDirectory))
                runTest {
                    assertThat(execution.duplicateEntries()).isEmpty()
                }
            }
        }
    }

    @DisplayName("detect duplicates when")
    @Nested
    inner class Duplicates {
        @Test
        fun `directory has two files with same content hash`() {
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
                val duplicateFinder = SequentialDuplicateFinder(this)
                val execution = duplicateFinder.find(listOf(directoryWith2FilesSameHash))

                val duplicateEntries = execution.duplicateEntries()
                assertThat(duplicateEntries).hasSize(1)
                duplicateEntries.first().also { group ->
                    assertThat(group.hash).isEqualTo(expectedDuplicateGroup.hash)
                    assertThat(group.paths).containsExactlyInAnyOrderElementsOf(expectedDuplicateGroup.paths)
                }
            }
        }

        @Test
        @DisplayName(
            """
            finding in tempdir and tempdir/somedir/somefile and tempdir/otherdir/otherfile have same content
        """,
        )
        fun `two files in different subdirectories have same content hash`() {
            val root = createTempDirectory()
            val oneFile =
                ((root / Paths.get("somedir")).createDirectory() / Paths.get("somefile")).apply {
                    writeText("hi")
                }
            val otherFile =
                ((root / Paths.get("otherdir")).createDirectory() / Paths.get("otherfile")).apply {
                    writeText("hi")
                }

            val expectedDuplicateGroup =
                DuplicateGroup(
                    hash = "hi".contentHash(),
                    paths = listOf(oneFile, otherFile).map { it.absolute() },
                )

            runTest {
                val duplicateFinder = SequentialDuplicateFinder(this)
                val execution = duplicateFinder.find(listOf(root))

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

private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

fun String.contentHash(type: String = "MD5"): String {
    val bytes = MessageDigest.getInstance(type).digest(toByteArray())

    fun printHexBinary(data: ByteArray): String =
        buildString(data.size * 2) {
            data.forEach { b ->
                val i = b.toInt()
                append(HEX_CHARS[i shr 4 and 0xF])
                append(HEX_CHARS[i and 0xF])
            }
        }
    return printHexBinary(bytes)
}
