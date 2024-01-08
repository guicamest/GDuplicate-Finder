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

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.createDirectories
import kotlin.io.path.createSymbolicLinkPointingTo
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.setPosixFilePermissions
import kotlin.io.path.writeText

fun directory(
    name: String,
    nameAsPrefix: Boolean = true,
    fs: FileSystem = FileSystems.getDefault(),
    configure: TestDirectory.() -> Unit,
): TestDirectory = TestDirectory(fs.tempDirectory, name, nameAsPrefix).apply(configure)

fun directory(
    fs: FileSystem = FileSystems.getDefault(),
    posixPermissions: Set<PosixFilePermission>? = null,
    configure: TestDirectory.() -> Unit,
): TestDirectory =
    TestDirectory(fs.tempDirectory, null, false).apply {
        configure(this)
        if (posixPermissions != null) path.setPosixFilePermissions(posixPermissions)
    }

fun directory(
    path: Path,
    configure: TestDirectory.() -> Unit,
): TestDirectory = TestDirectory(path).apply(configure)

class TestDirectory(
    val path: Path,
    private val files: MutableList<Path> = mutableListOf(),
    private val directories: MutableList<TestDirectory> = mutableListOf(),
) {
    init {
        path.createDirectories()
    }
    internal constructor(
        parentPath: Path,
        name: String?,
        nameAsPrefix: Boolean,
    ) : this(createDirectory(parentPath, name, nameAsPrefix))

    fun directory(
        name: String,
        nameAsPrefix: Boolean = true,
        configure: TestDirectory.() -> Unit,
    ): TestDirectory = TestDirectory(path, name, nameAsPrefix).apply(configure).also { directories.add(it) }

    fun directory(configure: TestDirectory.() -> Unit): TestDirectory =
        TestDirectory(path, null, false).apply(configure).also { directories.add(it) }

    fun file(
        name: String? = null,
        size: Int,
    ): Path =
        file(
            name = name,
            content = generateContentOfSize(size),
        )

    fun file(
        name: String? = null,
        content: String,
        posixPermissions: Set<PosixFilePermission>? = null,
    ): Path =
        if (name == null) {
            createTempFile(directory = path)
        } else {
            path / name
        }.apply {
            files.add(this)
            writeText(content)
            if (posixPermissions != null) setPosixFilePermissions(posixPermissions)
        }

    fun symlink(
        name: String? = null,
        to: Path,
    ): Path =
        if (name == null) {
            createTempFile(directory = path).also { it.deleteExisting() }
        } else {
            path / name
        }.createSymbolicLinkPointingTo(to)

    val allFiles: List<Path> get() = files + directories.flatMap { it.allFiles }
    val dirs: List<Path> get() = directories.map { it.path }
}

private fun generateContentOfSize(size: Int): String {
    val seq: Sequence<Int> =
        sequence {
            var i = 0
            while (true) {
                yield(i++)
                if (i == 10) i = 0
            }
        }
    return seq.take(size).joinToString(separator = "")
}

private fun createDirectory(
    parentPath: Path,
    name: String?,
    nameAsPrefix: Boolean,
): Path =
    when {
        name == null -> createTempDirectory(directory = parentPath)
        nameAsPrefix -> createTempDirectory(directory = parentPath, prefix = name)
        else -> parentPath.resolve(name).createDirectories()
    }

val FileSystem.tempDirectory: Path get() {
    return if (this == FileSystems.getDefault()) {
        createTempDirectory().also { it.deleteIfExists() }.parent
    } else {
        rootDirectories.first().resolve("tmpdir").createDirectories()
    }
}
