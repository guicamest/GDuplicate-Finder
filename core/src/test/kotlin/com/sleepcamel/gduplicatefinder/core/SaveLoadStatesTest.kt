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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SaveLoadStatesTest {
    private lateinit var searchDirectory: Path
    private lateinit var paths: List<Path>
    private lateinit var dirsOnRoot: List<Path>
    private lateinit var fs: FileSystem

    @BeforeAll
    fun setupFiles() {
        fs = MemoryFileSystemBuilder.newLinux().build("saveload")
        val testDirectory =
            directory(fs = fs, name = "saveload") {
                repeat(1000) {
                    directory {
                        repeat(20) {
                            directory {
                                file(size = 3)
                            }
                            file(size = 4)
                            file(size = 5)
                        }
                    }
                    repeat(20) {
                        file(size = 1)
                    }
                    repeat(10) {
                        file(size = 2)
                    }
                }
            }
        searchDirectory = testDirectory.path
        paths = testDirectory.allFiles
        dirsOnRoot = testDirectory.dirs
    }

    @AfterAll
    fun closeFs() {
        fs.close()
    }

    @ExperimentalCoroutinesApi
    @Test
    @DisplayName("ScanDirectoriesStateImpl should be serializable")
    fun canSerializeScanState() {
        val scanDirectoriesState =
            ScanDirectoriesStateImpl(
                initialDirectories = emptySet(),
                filter = MinSizeFilter(0),
                filesToProcess = paths.addAttributes(),
                visitedDirectories = dirsOnRoot.toSet(),
            )
        val file = kotlin.io.path.createTempFile()
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            JsonKSStateSerializer(dispatcher).serialize(scanDirectoriesState, file)
            assertThat(file).isNotEmptyFile()
        }
    }

    interface StateSerializer<T> {
        suspend fun serialize(
            state: T,
            to: Path,
        )
    }

    class JsonKSStateSerializer(
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : StateSerializer<FindDuplicatesExecutionState> {
        override suspend fun serialize(
            state: FindDuplicatesExecutionState,
            to: Path,
        ) {
            TODO("Not yet implemented")
        }
    }
}

private fun List<Path>.addAttributes(): Set<PathWithAttributes> =
    mapTo(hashSetOf()) {
        PathWithAttributes(it, Files.readAttributes(it, BasicFileAttributes::class.java))
    }
