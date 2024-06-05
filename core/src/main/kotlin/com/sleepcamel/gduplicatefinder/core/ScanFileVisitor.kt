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
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

internal class ScanFileVisitor(
    private val filter: PathFilter,
    private val stateHolder: FindProgressStateHolder,
) : FileVisitor<Path> {
    override fun preVisitDirectory(
        directory: Path,
        attributes: BasicFileAttributes,
    ): FileVisitResult =
        if (shouldVisitDirectory(
                directory,
                filter,
                attributes,
                stateHolder.stateAs<ScanDirectoriesState>().visitedDirectories,
            )
        ) {
            FileVisitResult.CONTINUE
        } else {
            FileVisitResult.SKIP_SUBTREE
        }

    override fun visitFile(
        file: Path,
        attributes: BasicFileAttributes,
    ): FileVisitResult =
        FileVisitResult.CONTINUE.also {
            if (shouldAddFile(
                    file,
                    filter,
                    attributes,
                )
            ) {
                addFileToState(PathWithAttributes(file.toRealPath(), attributes))
            }
        }

    override fun visitFileFailed(
        file: Path,
        exc: IOException,
    ): FileVisitResult = FileVisitResult.CONTINUE

    override fun postVisitDirectory(
        directory: Path,
        exc: IOException?,
    ): FileVisitResult =
        FileVisitResult.CONTINUE.also {
            addVisitedDirectoryToState(directory)
        }

    private fun addFileToState(pathWithAttributes: PathWithAttributes) {
        stateHolder.update { currentState: ScanDirectoriesStateImpl ->
            currentState.copy(
                filesToProcess = currentState.filesToProcess + pathWithAttributes,
            )
        }
    }

    private fun addVisitedDirectoryToState(directory: Path) {
        stateHolder.update { currentState: ScanDirectoriesStateImpl ->
            currentState.copy(
                visitedDirectories = currentState.visitedDirectories.plusElement(directory),
            )
        }
    }

    companion object {
        internal fun shouldVisitDirectory(
            directory: Path,
            filter: PathFilter,
            attributes: BasicFileAttributes,
            visitedDirectories: Collection<Path>,
        ): Boolean =
            directory !in visitedDirectories &&
                (
                    filter !is DirectoryFilter ||
                        filter.accept(
                            directory,
                            attributes,
                        )
                )

        internal fun shouldAddFile(
            file: Path,
            filter: PathFilter,
            attributes: BasicFileAttributes,
        ) = (filter is DirectoryFilter || filter.accept(file, attributes)) && Files.isReadable(file)
    }
}
