package com.sleepcamel.gduplicatefinder.core

import org.assertj.core.api.Assertions.assertThat
import java.nio.file.Path

fun Collection<DuplicateGroup>.assertOneDuplicateGroupWith(
    paths: List<Path>,
    content: String,
) {
    assertThat(this).withFailMessage { "Expected 1 duplicate group" }.hasSize(1)
    first().also { group ->
        assertThat(group.hash).isEqualTo(content.contentHash())
        assertThat(
            group.paths.map { it.path }.realPaths(),
        ).containsExactlyInAnyOrderElementsOf(paths.realPaths())
    }
}

private fun Collection<Path>.realPaths() = map { it.toRealPath() }
