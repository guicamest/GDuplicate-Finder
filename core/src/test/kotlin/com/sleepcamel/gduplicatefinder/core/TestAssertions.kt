package com.sleepcamel.gduplicatefinder.core

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
        group.paths.map { it.path }.realPaths(),
    ).containsExactlyInAnyOrderElementsOf(paths.realPaths())

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
    assertThat(fileKey()).isNotNull
    assertThat(size()).isEqualTo(expectedSize)
}

private fun Collection<Path>.realPaths() = map { it.toRealPath() }
