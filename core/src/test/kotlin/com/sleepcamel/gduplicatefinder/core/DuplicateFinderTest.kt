/*
 * Copyright 2012-2024 guicamest
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import kotlin.io.path.div
import kotlin.io.path.fileSize
import kotlin.io.path.isReadable
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
            val emptyDirectory = directory("emptyDirectory") {}.path
            assertThat(emptyDirectory).isEmptyDirectory()

            val execution = findDuplicates(duplicateFinder, directory = emptyDirectory)
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and has only one file`() {
            val directoryWithOneFile =
                directory("directoryWithOneFile") {
                    assertThat(file(size = 2)).exists().isRegularFile()
                }.path

            val execution = findDuplicates(duplicateFinder, directory = directoryWithOneFile)
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and has two different files`() {
            val directoryWith2DifferentFiles =
                directory("directoryWith2DifferentFiles") {
                    file(content = "hi")
                    file(content = "bye")
                }.path

            val execution = findDuplicates(duplicateFinder, directory = directoryWith2DifferentFiles)
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        @Test
        fun `directory exists and has two files with same size and different content`() {
            val directoryWith2DifferentFiles =
                directory("directoryWith2FilesSameSizeDifContent") {
                    val hiFile = file(content = "hi")
                    val byFile = file(content = "by")
                    assertThat(hiFile).hasSize(byFile.fileSize())
                }.path

            val execution = findDuplicates(duplicateFinder, directory = directoryWith2DifferentFiles)
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }

        private val noPermissions = PosixFilePermissions.fromString("-".repeat(9))

        @Test
        fun `directory exists and is not readable`() {
            inLinux { fs ->
                val notReadableDirectory =
                    directory(fs = fs, posixPermissions = noPermissions) {
                        repeat(2) { file(content = "hi") }
                    }.path
                assertThat(notReadableDirectory).isNot(
                    Condition({ file -> file.isReadable() }, "not readable"),
                )

                val execution = findDuplicates(duplicateFinder, directory = notReadableDirectory)
                runTest {
                    assertThat(execution.duplicateEntries()).isEmpty()
                }
            }
        }

        @Test
        fun `files are not readable`() {
            inLinux { fs ->
                val directory =
                    directory(fs = fs) {
                        repeat(2) { file(content = "hi", posixPermissions = noPermissions) }
                    }.path
                val execution = findDuplicates(duplicateFinder, directory = directory)
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

        @Test
        fun `there is a symbolic link to a file`() {
            val testDirectory =
                directory("withSymlinks") {
                    symlink(to = file(content = "hi"))
                }.path

            val execution = findDuplicates(duplicateFinder, directory = testDirectory)
            runTest {
                assertThat(execution.duplicateEntries()).isEmpty()
            }
        }
    }

    data class SymbolicTestDirectories(val root: Path, val volumes: Path, val link: Path)

    @DisplayName("detect duplicates when")
    @Nested
    inner class Duplicates {
        @Test
        fun `directory has two files with same content hash`() {
            val (directoryWith2FilesSameHash, fileNames) =
                directory("directoryWith2FilesSameHash") {
                    repeat(2) { file(content = "hi") }
                }.let { it.path to it.allFiles }

            runTest {
                val execution = findDuplicates(this, directory = directoryWith2FilesSameHash)

                val duplicateEntries = execution.duplicateEntries()
                duplicateEntries.assertOneDuplicateGroupWith(
                    paths = fileNames,
                    content = "hi",
                    checkAttributes = true,
                )
            }
        }

        @Test
        @DisplayName(
            """
            finding in tempdir and tempdir/somedir/somefile and tempdir/otherdir/otherfile have same content
        """,
        )
        fun `two files in different subdirectories have same content hash`() {
            val (root, files) =
                directory {
                    directory("somedir", nameAsPrefix = false) {
                        file("somefile", content = "hi")
                    }
                    directory("otherdir", nameAsPrefix = false) {
                        file("otherfile", content = "hi")
                    }
                }.let { it.path to it.allFiles }

            runTest {
                val execution = findDuplicates(this, directory = root)

                val duplicateEntries = execution.duplicateEntries()
                duplicateEntries.assertOneDuplicateGroupWith(
                    paths = files,
                    content = "hi",
                )
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
            val (subdirectories, files) =
                directory {
                    directory("somedir", nameAsPrefix = false) {
                        file("somefile", content = "hi")
                    }
                    directory("otherdir", nameAsPrefix = false) {
                        file("otherfile", content = "hi")
                    }
                }.let { it.dirs to it.allFiles }

            runTest {
                val execution = findDuplicates(this, directories = subdirectories)

                val duplicateEntries = execution.duplicateEntries()
                duplicateEntries.assertOneDuplicateGroupWith(
                    paths = files,
                    content = "hi",
                )
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
            val (root, files) =
                directory {
                    directory("somedir", nameAsPrefix = false) {
                        file("somefile", content = "hi")
                    }
                    directory("otherdir", nameAsPrefix = false) {
                        file("somefile", content = "hi")
                    }
                }.let { it.path to it.allFiles }

            runTest {
                val execution =
                    findDuplicates(
                        parentScope = this,
                        directory = root,
                    )

                val duplicateEntries = execution.duplicateEntries()
                duplicateEntries.assertOneDuplicateGroupWith(paths = files, content = "hi")
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
                    val oneFile = lfs.getPath("somefile").apply { writeText("hi") }.absolute()
                    val otherFile = wfs.getPath("otherfile").apply { writeText("hi") }.absolute()

                    runTest {
                        val execution =
                            findDuplicates(
                                this,
                                directories = listOf(lfs, wfs).map { it.getPath(".").toRealPath() },
                            )

                        val duplicateEntries = execution.duplicateEntries()
                        duplicateEntries.assertOneDuplicateGroupWith(
                            paths = listOf(oneFile, otherFile),
                            content = "hi",
                        )
                    }
                }
            }

        @Test
        fun `two files in directories of symbolic link have same content hash`(): Unit =
            inLinux { fs ->
                val (volumes, files) =
                    directory(path = fs.rootDirectories.first()) {
                        val root = path
                        file("abc.txt", content = "hi")
                        directory("Volumes", nameAsPrefix = false) {
                            file("def.txt", content = "hi")
                            symlink("Macintosh HD", to = root)
                        }
                    }.let { it.dirs.first() to it.allFiles }

                runTest {
                    val execution =
                        findDuplicates(
                            parentScope = this,
                            directory = volumes,
                        )

                    val duplicateEntries = execution.duplicateEntries()
                    duplicateEntries.assertOneDuplicateGroupWith(paths = files, content = "hi")
                }
            }

        private fun <R> inWindows(block: (FileSystem) -> R): R =
            MemoryFileSystemBuilder.newWindows().build().use(block)
    }

    private fun <R> inLinux(block: (FileSystem) -> R): R =
        MemoryFileSystemBuilder.newLinux().build().use(block)
}
