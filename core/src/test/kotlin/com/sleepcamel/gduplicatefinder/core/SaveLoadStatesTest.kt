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
import com.sleepcamel.gduplicatefinder.core.serialization.toDeserializedAttributes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.recursive.comparison.RecursiveComparator
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream.Stream
import kotlin.io.path.createTempFile
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.readText

@DisplayName("State classes de/serialization")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SaveLoadStatesTest {
    @ExperimentalCoroutinesApi
    @Test
    @DisplayName("ScanDirectoriesStateImpl should be de/serializable")
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

            val state: FindDuplicatesExecutionState = JsonKSStateSerializer(dispatcher).deserialize(file)

            assertThat(state)
                .usingComparatorForType(AttributesComparator, BasicFileAttributes::class.java)
                .usingRecursiveComparison()
                .isEqualTo(scanDirectoriesState)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    @DisplayName("SizeCompareStateImpl should be de/serializable")
    fun canSerializeSizeCompareState() {
        val files =
            directory {
                file("somefile", size = 0)
            }.allFiles

        val sizeCompareState =
            SizeCompareStateImpl(
                filesToProcess = files.addAttributes(),
                processedFiles = files.addAttributes(),
            )
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val file = createTempFile()

            JsonKSStateSerializer(dispatcher).serialize(sizeCompareState, file)
            assertThat(file).isNotEmptyFile()
            println(file.readText())

            val state: FindDuplicatesExecutionState = JsonKSStateSerializer(dispatcher).deserialize(file)

            assertThat(state)
                .usingComparatorForType(AttributesComparator, BasicFileAttributes::class.java)
                .usingRecursiveComparison()
                .isEqualTo(sizeCompareState)
        }
    }

    @Test
    @DisplayName("Should de/serialize Path")
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

    @DisplayName("Should de/serialize PathFilter subclass")
    @ParameterizedTest(name = "{0}")
    @MethodSource("pathFilters")
    fun canSerializeFilter(instance: PathFilter) {
        val file = createTempFile()
        file.outputStream().use {
            Json.encodeToStream(instance, it)
        }
        file.inputStream().use {
            val value: PathFilter = Json.decodeFromStream(it)
            assertThat(value).isEqualTo(instance)
        }
    }

    private fun pathFilters() =
        Stream.of(
            arguments(MinSizeFilter(100), 0),
            arguments(MaxSizeFilter(100), 1),
            arguments(FilenameFilter(name = "somefile", exact = true), 0),
            arguments(ExtensionFilter(extension = "txt", exact = false), 1),
            arguments(FullFilenameFilter(name = "somefile", exact = false), 1),
            arguments(DirectoryFilter(name = "ramDi", exact = false), 1),
            arguments(PathMatcherFilter("glob:**/*.{txt}"), 0),
            arguments(PathMatcherFilter("regex:.*a\\w+file\\..*"), 1),
        )

    class JsonKSStateSerializer(
        override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : StateToPathSerializer<FindDuplicatesExecutionState> {
        override val encodeToStream: (
            FindDuplicatesExecutionState,
            OutputStream,
        ) -> Unit = json::encodeToStream

        suspend inline fun <reified T : FindDuplicatesExecutionState> deserialize(from: Path): T =
            withContext(dispatcher) {
                from.inputStream().use {
                    json.decodeFromStream<T>(it)
                }
            }

        companion object {
            val json = Json { serializersModule = nioSerializersModule }
        }
    }
}

private object AttributesComparator : Comparator<BasicFileAttributes> {
    private val recursiveComparator =
        RecursiveComparator(
            RecursiveComparisonConfiguration().apply {
                ignoreFields("fileKey")
            },
        )

    override fun compare(
        o1: BasicFileAttributes,
        o2: BasicFileAttributes,
    ): Int = recursiveComparator.compare(o1.toDeserializedAttributes(), o2.toDeserializedAttributes())
}

private fun List<Path>.addAttributes(): Set<PathWithAttributes> =
    mapTo(hashSetOf()) {
        PathWithAttributes(it, Files.readAttributes(it, BasicFileAttributes::class.java))
    }
