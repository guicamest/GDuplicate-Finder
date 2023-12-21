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
                stateHolder.stateAs<ScanExecutionState>().visitedDirectories,
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
        stateHolder.update { currentState: ScanExecutionStateImpl ->
            currentState.copy(
                filesToProcess = currentState.filesToProcess + pathWithAttributes,
            )
        }
    }

    private fun addVisitedDirectoryToState(directory: Path) {
        stateHolder.update { currentState: ScanExecutionStateImpl ->
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
            directory !in visitedDirectories && (
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
