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

import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.absolute
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

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

fun createDuplicates(
    directoryPrefix: String = "test",
    directory: Path = createTempDirectory(directoryPrefix),
    content: String = "hi",
    filenamePrefixAndSuffix: Pair<String, String>? = null,
): Path =
    directory.also { dir ->
        repeat(2) {
            createTempFile(
                directory = dir,
                prefix = filenamePrefixAndSuffix?.first,
                suffix = filenamePrefixAndSuffix?.second,
            ).apply {
                writeText(content)
            }.absolute()
        }
    }

fun findDuplicates(
    finder: DuplicateFinder,
    directories: Collection<Path>,
    filter: PathFilter = MinSizeFilter(0),
): FindDuplicatesExecution = finder.find(directories, filter)

fun findDuplicates(
    parentScope: CoroutineScope,
    newFinderInstance: (CoroutineScope) -> DuplicateFinder = { scope -> SequentialDuplicateFinder(scope) },
    directories: Collection<Path>,
    filter: PathFilter = MinSizeFilter(0),
) = findDuplicates(newFinderInstance(parentScope), directories, filter)

fun findDuplicates(
    finder: DuplicateFinder,
    directory: Path,
    filter: PathFilter = MinSizeFilter(0),
) = findDuplicates(finder, listOf(directory), filter)

fun findDuplicates(
    parentScope: CoroutineScope,
    newFinderInstance: (CoroutineScope) -> DuplicateFinder = { scope -> SequentialDuplicateFinder(scope) },
    directory: Path,
    filter: PathFilter = MinSizeFilter(0),
) = findDuplicates(parentScope, newFinderInstance, listOf(directory), filter)

fun resumeFindDuplicates(
    parentScope: CoroutineScope,
    newFinderInstance: (CoroutineScope) -> DuplicateFinder = { scope -> SequentialDuplicateFinder(scope) },
    fromState: FindDuplicatesExecutionState,
): FindDuplicatesExecution = newFinderInstance(parentScope).find(fromState = fromState)
