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
@file:OptIn(ExperimentalSerializationApi::class)

package com.sleepcamel.gduplicatefinder.core

import com.sleepcamel.gduplicatefinder.core.serialization.PathSerializer
import com.sleepcamel.gduplicatefinder.core.serialization.StateToPathSerializer
import com.sleepcamel.gduplicatefinder.core.serialization.nioSerializersModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.createTempFile
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SaveLoadStatesTest {
    @ExperimentalCoroutinesApi
    @Test
    @DisplayName("ScanDirectoriesStateImpl should be serializable")
    fun canSerializeScanState() {
        val (directory, files) =
            directory {
                file("somefile", size = 10)
            }.let { it.path to it.allFiles }

        val scanDirectoriesState =
            ScanDirectoriesStateImpl(
                initialDirectories = setOf(directory),
                filter = MinSizeFilter(0),
                filesToProcess = files.addAttributes(),
                visitedDirectories = setOf(directory),
            )
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val file = createTempFile()

            JsonKSStateSerializer(dispatcher).serialize(scanDirectoriesState, file)
            assertThat(file).isNotEmptyFile()
        }
    }

    @Test
    @DisplayName("Path should be de/serializable")
    fun canSerializePath() {
        val file = createTempFile()
        file.outputStream().use {
            Json.encodeToStream(PathSerializer, file, it)
        }
        file.inputStream().use {
            val value = Json.decodeFromStream(PathSerializer, it)
            assertThat(value).isEqualTo(file)
        }
    }

    class JsonKSStateSerializer(
        override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : StateToPathSerializer<FindDuplicatesExecutionState> {
        override val encodeToStream: (
            FindDuplicatesExecutionState,
            OutputStream,
        ) -> Unit = json::encodeToStream

        companion object {
            private val json = Json { serializersModule = nioSerializersModule }
        }
    }
}

private fun List<Path>.addAttributes(): Set<PathWithAttributes> =
    mapTo(hashSetOf()) {
        PathWithAttributes(it, Files.readAttributes(it, BasicFileAttributes::class.java))
    }
