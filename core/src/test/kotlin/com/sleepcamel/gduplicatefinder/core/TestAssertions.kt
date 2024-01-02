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

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.AbstractCollectionAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.temporal.ChronoUnit.SECONDS

fun Collection<DuplicateGroup>.assertOneDuplicateGroupWith(
    paths: List<Path>,
    content: String,
    checkAttributes: Boolean = false,
) {
    assertThat(this).withFailMessage { "Expected 1 duplicate group" }.hasSize(1)

    val group = first()
    assertThat(group.hash).isEqualTo(content.contentHash())
    assertThat(
        group.paths.map { it.path },
    ).withPathComparator().containsExactlyInAnyOrderElementsOf(paths)

    if (checkAttributes) {
        val startOfAssertions = Instant.now()
        group.paths.forEach {
            it.assertAttributes(timeReference = startOfAssertions, expectedSize = content.length.toLong())
        }
    }
}

private fun BasicFileAttributes.assertAttributes(
    timeReference: Instant,
    expectedSize: Long,
) {
    assertThat(isDirectory).isFalse()
    assertThat(isSymbolicLink).isFalse()
    assertThat(isRegularFile).isTrue()
    assertThat(creationTime().toInstant()).isCloseTo(timeReference, within(30, SECONDS))
    assertThat(lastAccessTime().toInstant()).isCloseTo(timeReference, within(30, SECONDS))
    assertThat(lastModifiedTime().toInstant()).isCloseTo(timeReference, within(30, SECONDS))
    assertThat(size()).isEqualTo(expectedSize)
}

@ExperimentalCoroutinesApi
fun findCoroutine(name: String) =
    DebugProbes.dumpCoroutinesInfo().find { it.context[CoroutineName]?.name.equals(name) }

fun <
    S : AbstractCollectionAssert<S, A, E, EA>,
    A : Collection<E>,
    E,
    EA : AbstractAssert<EA, E>,
    > AbstractCollectionAssert<S, A, E, EA>.withPathComparator() =
    usingComparatorForType(
        PathComparator(),
        java.nio.file.Path::class.java,
    )

private class PathComparator : Comparator<Path> {
    override fun compare(
        o1: Path,
        o2: Path,
    ): Int = o1.toRealPath().compareTo(o2.toRealPath())
}

fun Collection<Path>.without(leftToProcess: List<Path>): Collection<Path> =
    map { it.toRealPath() } - leftToProcess.toSet()
