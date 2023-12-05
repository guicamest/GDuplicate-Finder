package com.sleepcamel.gduplicatefinder.core

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import java.nio.file.Path
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
        group.paths.map { it.path }.realPaths(),
    ).containsExactlyInAnyOrderElementsOf(paths.realPaths())

    if (checkAttributes) {
        group.paths.forEach {
            assertThat(it.isDirectory).isFalse()
            assertThat(it.isSymbolicLink).isFalse()
            assertThat(it.isRegularFile).isTrue()
            assertThat(it.creationTime().toInstant()).isCloseTo(Instant.now(), within(30, SECONDS))
            assertThat(it.lastAccessTime().toInstant()).isCloseTo(Instant.now(), within(30, SECONDS))
            assertThat(it.lastModifiedTime().toInstant()).isCloseTo(Instant.now(), within(30, SECONDS))
            assertThat(it.fileKey()).isNotNull
            assertThat(it.size()).isEqualTo(content.length.toLong())
        }
    }
}

private fun Collection<Path>.realPaths() = map { it.toRealPath() }
