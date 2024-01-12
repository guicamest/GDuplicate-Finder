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
package com.sleepcamel.gduplicatefinder.core.serialization

import com.sleepcamel.gduplicatefinder.core.FindDuplicatesExecutionState
import com.sleepcamel.gduplicatefinder.core.ScanDirectoriesStateImpl
import com.sleepcamel.gduplicatefinder.core.SizeCompareStateImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.io.OutputStream
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.time.Instant
import kotlin.io.path.outputStream

interface StateSerializer<T> {
    suspend fun serialize(
        state: T,
        to: Path,
    )
}

interface StateToPathSerializer<T> : StateSerializer<T> {
    val dispatcher: CoroutineDispatcher
    val encodeToStream: (T, OutputStream) -> Unit

    override suspend fun serialize(
        state: T,
        to: Path,
    ) = withContext(dispatcher) { to.outputStream().use { encodeToStream(state, it) } }
}

object PathSerializer : KSerializer<Path> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Pathv0", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Path,
    ) = encoder.encodeString(value.toUri().toString())

    override fun deserialize(decoder: Decoder): Path = Paths.get(URI.create(decoder.decodeString()))
}

object BasicAttributesSerializer : KSerializer<BasicFileAttributes> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BasicFileAttributesv0", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: BasicFileAttributes,
    ) = encoder.encodeSerializableValue(
        DeserializedFileAttributes.serializer(),
        value.toDeserializedAttributes(),
    )

    override fun deserialize(decoder: Decoder): BasicFileAttributes =
        decoder.decodeSerializableValue(
            DeserializedFileAttributes.serializer(),
        )
}

@Serializable
internal data class DeserializedFileAttributes(
    private val lastModifiedTime: @Contextual FileTime,
    private val lastAccessTime: @Contextual FileTime,
    private val creationTime: @Contextual FileTime,
    private val isRegularFile: Boolean,
    private val isDirectory: Boolean,
    private val isSymbolicLink: Boolean,
    private val isOther: Boolean,
    private val size: Long,
) : BasicFileAttributes {
    override fun lastModifiedTime(): FileTime = lastModifiedTime

    override fun lastAccessTime(): FileTime = lastAccessTime

    override fun creationTime(): FileTime = creationTime

    override fun isRegularFile(): Boolean = isRegularFile

    override fun isDirectory(): Boolean = isDirectory

    override fun isSymbolicLink(): Boolean = isSymbolicLink

    override fun isOther(): Boolean = isOther

    override fun size(): Long = size

    override fun fileKey(): Any? = null
}

internal fun BasicFileAttributes.toDeserializedAttributes(): DeserializedFileAttributes =
    DeserializedFileAttributes(
        lastModifiedTime = lastModifiedTime(),
        lastAccessTime = lastAccessTime(),
        creationTime = creationTime(),
        isRegularFile = isRegularFile,
        isDirectory = isDirectory,
        isSymbolicLink = isSymbolicLink,
        isOther = isOther,
        size = size(),
    )

object FileTimeSerializer : KSerializer<FileTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FileTime", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: FileTime,
    ) = encoder.encodeString(
        value.toInstant().toString(),
    )

    override fun deserialize(decoder: Decoder): FileTime =
        FileTime.from(
            Instant.parse(decoder.decodeString()),
        )
}

val nioSerializersModule =
    SerializersModule {
        contextual(PathSerializer)
        contextual(BasicAttributesSerializer)
        contextual(FileTimeSerializer)
        polymorphic(
            FindDuplicatesExecutionState::class,
        ) {
            subclass(ScanDirectoriesStateImpl.serializer())
            subclass(SizeCompareStateImpl.serializer())
        }
    }
