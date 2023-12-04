package com.sleepcamel.gduplicatefinder.core

import org.assertj.core.api.Assertions
import java.nio.file.Path

fun Collection<DuplicateGroup>.assertOneDuplicateGroupWith(
    paths: List<Path>,
    content: String,
) {
    Assertions.assertThat(this).withFailMessage { "Expected 1 duplicate group" }.hasSize(1)
    first().also { group ->
        Assertions.assertThat(group.hash).isEqualTo(content.contentHash())
        Assertions.assertThat(
            group.paths.map { it.path.toRealPath() },
        ).containsExactlyInAnyOrderElementsOf(paths)
    }
}
