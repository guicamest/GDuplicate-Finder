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

package com.sleepcamel.gduplicatefinder.core.serialization

import com.sleepcamel.gduplicatefinder.core.DirectoryFilter
import com.sleepcamel.gduplicatefinder.core.ExtensionFilter
import com.sleepcamel.gduplicatefinder.core.FilenameFilter
import com.sleepcamel.gduplicatefinder.core.FullFilenameFilter
import com.sleepcamel.gduplicatefinder.core.MaxSizeFilter
import com.sleepcamel.gduplicatefinder.core.MinSizeFilter
import com.sleepcamel.gduplicatefinder.core.PathFilter
import com.sleepcamel.gduplicatefinder.core.PathMatcherFilter
import com.sleepcamel.gduplicatefinder.core.PathWithAttributes
import com.sleepcamel.gduplicatefinder.core.PathWithAttributesAndContent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream.Stream
import kotlin.io.path.createTempFile
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@DisplayName("State subclasses de/serialization")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SerializersTest {
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

    @Test
    @DisplayName("Should de/serialize PathWithAttributes")
    fun canSerializePathWithAttributes() {
        val jsonWithModules =
            Json {
                serializersModule =
                    SerializersModule {
                        contextual(PathSerializer)
                        contextual(BasicAttributesSerializer)
                        contextual(FileTimeSerializer)
                    }
            }

        val file = createTempFile()
        val withAttributes = file.withAttributes()

        file.outputStream().use {
            jsonWithModules.encodeToStream(withAttributes, it)
        }
        file.inputStream().use {
            val value = jsonWithModules.decodeFromStream<PathWithAttributes>(it)
            assertThat(value).isEqualTo(withAttributes)
        }
    }

    @Test
    @DisplayName("Should de/serialize PathWithAttributesAndContent")
    fun canSerializePathWithAttributesAndContent() {
        val jsonWithModules =
            Json {
                serializersModule =
                    SerializersModule {
                        contextual(PathSerializer)
                        contextual(BasicAttributesSerializer)
                        contextual(FileTimeSerializer)
                    }
            }

        val file = createTempFile()
        val withContent =
            file.withAttributes().run {
                PathWithAttributesAndContent(file, this, contentHash())
            }

        file.outputStream().use {
            jsonWithModules.encodeToStream(withContent, it)
        }

        file.inputStream().use {
            val value = jsonWithModules.decodeFromStream<PathWithAttributesAndContent>(it)
            assertThat(value).isEqualTo(withContent)
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
            Arguments.arguments(MinSizeFilter(100), 0),
            Arguments.arguments(MaxSizeFilter(100), 1),
            Arguments.arguments(FilenameFilter(name = "somefile", exact = true), 0),
            Arguments.arguments(ExtensionFilter(extension = "txt", exact = false), 1),
            Arguments.arguments(FullFilenameFilter(name = "somefile", exact = false), 1),
            Arguments.arguments(DirectoryFilter(name = "ramDi", exact = false), 1),
            Arguments.arguments(PathMatcherFilter("glob:**/*.{txt}"), 0),
            Arguments.arguments(PathMatcherFilter("regex:.*a\\w+file\\..*"), 1),
        )
}

private fun Path.withAttributes(): PathWithAttributes =
    PathWithAttributes(this, Files.readAttributes(this, BasicFileAttributes::class.java))
