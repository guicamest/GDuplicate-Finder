package com.sleepcamel.gduplicatefinder.core

fun Map<String, Collection<PathWithAttributes>>.duplicateGroups(): Collection<DuplicateGroup> =
    map { (hash, paths) ->
        DuplicateGroup(hash = hash, paths = paths)
    }
