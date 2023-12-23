package com.sleepcamel.gduplicatefinder.core

fun Set<PathWithAttributesAndContent>.duplicateGroups(): Collection<DuplicateGroup> =
    groupBy { it.contentHash }.map { (hash, paths) ->
        DuplicateGroup(hash = hash, paths = paths)
    }
