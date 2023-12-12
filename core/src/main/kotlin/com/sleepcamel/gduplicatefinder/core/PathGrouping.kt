package com.sleepcamel.gduplicatefinder.core

fun List<PathWithAttributes>.withSameContent() =
    groupBy {
        it.contentHash()
    }.filter { (_, paths) -> paths.size > 1 }

fun Map<String, Collection<PathWithAttributes>>.duplicateGroups(): Collection<DuplicateGroup> =
    map { (hash, paths) ->
        DuplicateGroup(hash = hash, paths = paths)
    }
