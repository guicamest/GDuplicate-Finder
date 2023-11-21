package com.sleepcamel.gduplicatefinder.core

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions
import java.util.stream.Stream
import kotlin.io.path.absolute
import kotlin.io.path.createDirectory
import kotlin.io.path.createSymbolicLinkPointingTo
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
    @DisplayName("not detect duplicates when")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class NoDuplicates {
        private val duplicateFinder: DuplicateFinder = SequentialDuplicateFinder(GlobalScope)

        @Test
        fun `collection of directories is empty`() {
            val execution = findDuplicates(duplicateFinder, directories = emptyList())
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory does not exist`() {
            val nonExistentDirectory = Paths.get("nonExistentDirectory")
            assertThat(nonExistentDirectory).doesNotExist()

            val execution = findDuplicates(duplicateFinder, directory = nonExistentDirectory)
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and has no files`() {
            val emptyDirectory = createTempDirectory("emptyDirectory")
            assertThat(emptyDirectory).isEmptyDirectory()

            val execution = findDuplicates(duplicateFinder, directory = emptyDirectory)
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and has only one file`() {
            val directoryWithOneFile = createTempDirectory("directoryWithOneFile")
            assertThat(createTempFile(directory = directoryWithOneFile)).exists().isRegularFile()

            val execution = findDuplicates(duplicateFinder, directory = directoryWithOneFile)
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and has two different files`() {
            val directoryWith2DifferentFiles = createTempDirectory("directoryWith2DifferentFiles")
            createTempFile(directory = directoryWith2DifferentFiles).writeText("hi")
            createTempFile(directory = directoryWith2DifferentFiles).writeText("bye")

            val execution = findDuplicates(duplicateFinder, directory = directoryWith2DifferentFiles)
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

            val execution = findDuplicates(duplicateFinder, directory = directoryWith2DifferentFiles)
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and is not readable`() {
            inLinux { fs ->
                val notReadableDirectory = createTempDirectory(fs.rootDirectories.first())
                repeat(2) {
                    createTempFile(directory = notReadableDirectory).writeText("hi")
                }
                val notReadablePermissions = PosixFilePermissions.fromString("-".repeat(9))
                notReadableDirectory.setPosixFilePermissions(notReadablePermissions)
                assertThat(notReadableDirectory).isNot(
                    Condition({ file -> file.isReadable() }, "not readable"),
                )

                val execution = findDuplicates(duplicateFinder, directory = notReadableDirectory)
                runTest {
                    assertThat(execution.duplicateEntries()).isEmpty()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("symbolicLinkTests")
        fun `same file is detected as one is found via symbolic link`(
            directoriesFn: (SymbolicTestDirectories) -> List<Path>,
        ): Unit =
            inLinux { fs ->
                val root = fs.rootDirectories.first()
                val volumes = (root / fs.getPath("Volumes")).createDirectory()

                val linkedToRoot = (volumes / fs.getPath("Macintosh HD")).createSymbolicLinkPointingTo(root)
                (root / fs.getPath("abc.txt")).writeText("hi")

                runTest {
                    val execution =
                        findDuplicates(
                            parentScope = this,
                            directories = directoriesFn(SymbolicTestDirectories(root, volumes, linkedToRoot)),
                        )

                    val duplicateEntries = execution.duplicateEntries()
                    assertThat(duplicateEntries).isEmpty()
                }
            }

        private fun symbolicLinkTests(): Stream<Named<(SymbolicTestDirectories) -> List<Path>>> =
            Stream.of(
                named("root / link") { d: SymbolicTestDirectories -> listOf(d.root, d.link) },
                named("duplicate directory") { d: SymbolicTestDirectories -> listOf(d.root, d.root) },
                named("root / volumes") { d: SymbolicTestDirectories -> listOf(d.root, d.volumes) },
                named("volumes / link") { d: SymbolicTestDirectories -> listOf(d.volumes, d.link) },
            )
    }

    data class SymbolicTestDirectories(val root: Path, val volumes: Path, val link: Path)

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
                    }.toRealPath()
                }
            val expectedDuplicateGroup =
                DuplicateGroup(
                    hash = "hi".contentHash(),
                    paths = fileNames,
                )

            runTest {
                val execution = findDuplicates(this, directory = directoryWith2FilesSameHash)

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
                    paths = listOf(oneFile, otherFile).map { it.toRealPath() },
                )

            runTest {
                val execution = findDuplicates(this, directory = root)

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
            finding in somedir and otherdir and tempdir/somedir/somefile and tempdir/otherdir/otherfile
            have the same content
        """,
        )
        fun `two files in different directories have same content hash`() {
            val subdirectories =
                createTempDirectory().let { root ->
                    listOf("somedir", "otherdir").map { dirname ->
                        (root / Paths.get(dirname)).createDirectory()
                    }
                }
            val oneFile = (subdirectories[0] / Paths.get("somefile")).apply { writeText("hi") }
            val otherFile = (subdirectories[1] / Paths.get("otherfile")).apply { writeText("hi") }

            val expectedDuplicateGroup =
                DuplicateGroup(
                    hash = "hi".contentHash(),
                    paths = listOf(oneFile, otherFile).map { it.toRealPath() },
                )

            runTest {
                val execution = findDuplicates(this, directories = subdirectories)

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
            finding in tempdir and tempdir/somedir/somefile and tempdir/otherdir/somefile
            have the same content
        """,
        )
        fun `two files with same name in different directories have same content hash`() {
            val subdirectories =
                createTempDirectory().let { root ->
                    listOf("somedir", "otherdir").map { dirname ->
                        (root / Paths.get(dirname)).createDirectory()
                    }
                }
            val files =
                subdirectories.map {
                    (it / Paths.get("somefile")).apply { writeText("hi") }
                }

            val expectedDuplicateGroup =
                DuplicateGroup(
                    hash = "hi".contentHash(),
                    paths = files.map { it.toRealPath() },
                )

            runTest {
                val execution =
                    findDuplicates(
                        parentScope = this,
                        directory = subdirectories.first().parent,
                    )

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
            finding in two different filesystems and somefile and otherfile
            have the same content
        """,
        )
        fun `two files in different filesystems have same content hash`() =
            inLinux { lfs ->
                inWindows { wfs ->
                    val oneFile = lfs.getPath("somefile").apply { writeText("hi") }
                    val otherFile = wfs.getPath("otherfile").apply { writeText("hi") }

                    val expectedDuplicateGroup =
                        DuplicateGroup(
                            hash = "hi".contentHash(),
                            paths = listOf(oneFile, otherFile).map { it.absolute() },
                        )

                    runTest {
                        val execution =
                            findDuplicates(
                                this,
                                directories = listOf(lfs, wfs).map { it.getPath(".").toRealPath() },
                            )

                        val duplicateEntries = execution.duplicateEntries()
                        assertThat(duplicateEntries).hasSize(1)
                        duplicateEntries.first().also { group ->
                            assertThat(group.hash).isEqualTo(expectedDuplicateGroup.hash)
                            assertThat(
                                group.paths,
                            ).containsExactlyInAnyOrderElementsOf(expectedDuplicateGroup.paths)
                        }
                    }
                }
            }

        @Test
        fun `two files in directories of symbolic link have same content hash`(): Unit =
            inLinux { fs ->
                val root = fs.rootDirectories.first()
                val volumes = (root / fs.getPath("Volumes")).createDirectory()

                (volumes / fs.getPath("Macintosh HD")).createSymbolicLinkPointingTo(root)
                val files =
                    listOf(
                        root / fs.getPath("abc.txt"),
                        volumes / fs.getPath("def.txt"),
                    ).map { it.apply { writeText("hi") } }

                val expectedDuplicateGroup =
                    DuplicateGroup(
                        hash = "hi".contentHash(),
                        paths = files.map { it.toRealPath() },
                    )

                runTest {
                    val execution =
                        findDuplicates(
                            parentScope = this,
                            directory = volumes,
                        )

                    val duplicateEntries = execution.duplicateEntries()
                    assertThat(duplicateEntries).withFailMessage { "Expected 1 duplicate group" }.hasSize(1)
                    duplicateEntries.first().also { group ->
                        assertThat(group.hash).isEqualTo(expectedDuplicateGroup.hash)
                        assertThat(
                            group.paths.map { it.toRealPath() },
                        ).containsExactlyInAnyOrderElementsOf(expectedDuplicateGroup.paths)
                    }
                }
            }

        private fun <R> inWindows(block: (FileSystem) -> R): R =
            MemoryFileSystemBuilder.newWindows().build().use(block)
    }

    private fun <R> inLinux(block: (FileSystem) -> R): R =
        MemoryFileSystemBuilder.newLinux().build().use(block)
}
