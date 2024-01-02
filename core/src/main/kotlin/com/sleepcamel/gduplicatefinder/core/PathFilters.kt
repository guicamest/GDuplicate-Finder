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

import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

interface PathFilter {
    @Throws(IOException::class)
    fun accept(
        entry: Path,
        attributes: BasicFileAttributes,
    ): Boolean
}

data class MinSizeFilter(private val size: Long) : PathFilter {
    @Throws(IOException::class)
    override fun accept(
        entry: Path,
        attributes: BasicFileAttributes,
    ) = attributes.size() > size
}

data class MaxSizeFilter(private val size: Long) : PathFilter {
    @Throws(IOException::class)
    override fun accept(
        entry: Path,
        attributes: BasicFileAttributes,
    ) = attributes.size() < size
}

/**
 * Filename without extension filter
 * If file is /some/path/somefile.txt => applies only to "somefile"
 * If file is /some/path/somefile.txt.doc => applies only to "somefile.txt"
 * If file is /some/path/somefile => applies only to "somefile"
 */
data class FilenameFilter(
    private val name: String,
    private val exact: Boolean,
) : PathFilter by StringFilter(name, exact, { p -> p.nameWithoutExtension })

/**
 * Full filename filter
 * If file is /some/path/somefile.txt => applies to "somefile.txt"
 */
data class FullFilenameFilter(
    private val name: String,
    private val exact: Boolean,
) : PathFilter by StringFilter(name, exact, { p -> p.name })

/**
 * Extension only filter
 * If file is /some/path/somefile.txt => applies only to "txt"
 * If file is /some/path/somefile.txt.doc => applies only to "doc"
 * If file doesn't have extension => applies to "" (empty string)
 */
data class ExtensionFilter(
    private val extension: String,
    private val exact: Boolean,
) : PathFilter by StringFilter(extension, exact, { p -> p.extension })

private class StringFilter(
    needle: String,
    exact: Boolean,
    private val stringProperty: (Path) -> String,
) : PathFilter {
    private val regex = Regex(if (exact) "^$needle\$" else ".*$needle.*")

    override fun accept(
        entry: Path,
        attributes: BasicFileAttributes,
    ): Boolean = stringProperty(entry).matches(regex)
}

data class DirectoryFilter(
    private val name: String,
    private val exact: Boolean,
) : PathFilter by StringFilter(name, exact, { p -> p.name })

data class PathMatcherFilter(val pattern: String) : PathFilter {
    override fun accept(
        entry: Path,
        attributes: BasicFileAttributes,
    ): Boolean = entry.fileSystem.getPathMatcher(pattern).matches(entry)
}
