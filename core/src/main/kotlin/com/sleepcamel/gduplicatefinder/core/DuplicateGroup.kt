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

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

data class DuplicateGroup(val hash: String, val paths: List<PathWithAttributesAndContent>)

@Serializable
data class PathWithAttributes(
    @Contextual val path: Path,
    @Contextual val attributes: BasicFileAttributes,
) : BasicFileAttributes by attributes {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PathWithAttributes

        return path == other.path
    }

    override fun hashCode(): Int = path.hashCode()

    fun contentHash(type: String = "MD5"): String = path.contentHash(type)
}

@Serializable
data class PathWithAttributesAndContent(
    @Contextual val path: Path,
    @Contextual val attributes: BasicFileAttributes,
    val contentHash: String,
) : BasicFileAttributes by attributes {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PathWithAttributesAndContent

        return path == other.path
    }

    override fun hashCode(): Int = path.hashCode()
}
