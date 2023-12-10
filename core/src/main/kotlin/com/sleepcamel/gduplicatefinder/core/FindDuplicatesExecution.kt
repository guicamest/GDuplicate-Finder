package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readBytes
import kotlin.io.path.visitFileTree

data class DuplicateGroup(val hash: String, val paths: Collection<PathWithAttributes>)

interface FindDuplicatesExecution {
    suspend fun duplicateEntries(): Collection<DuplicateGroup>

    suspend fun stop(): FindDuplicatesExecutionState

    val state: StateFlow<FindDuplicatesExecutionState>
}

@OptIn(ExperimentalPathApi::class)
class CoroutinesFindDuplicatesExecution(
    scope: CoroutineScope,
    state: FindDuplicatesExecutionState,
) : FindDuplicatesExecution {
    private val stateHolder: FindProgressStateHolder = FindProgressStateHolder(state)
    private val result = CompletableDeferred<Collection<DuplicateGroup>>()
    private val job: Job

    constructor(
        scope: CoroutineScope,
        directories: Collection<Path>,
        filter: PathFilter,
    ) : this(
        scope = scope,
        state = ScanExecutionStateImpl.empty(initialDirectories = directories, filter = filter),
    )

    init {
        job =
            scope.launch(CoroutineName("findDuplicatesExecution")) {
                check(state is ScanExecutionState)
                val allFiles = collectFiles(stateHolder)

                stateHolder.update(SizeFilterExecutionStateImpl())
                delay(1L)

                val withSameSize = allFiles.withSameSize()

                stateHolder.update(ContentFilterExecutionStateImpl())
                delay(1)
                val withSameContent =
                    withSameSize.mapNotNull { (_, groupWithSameSize) ->
                        groupWithSameSize.withSameContent().duplicateGroups()
                    }
                result.complete(withSameContent.flatten())
            }
    }

    override suspend fun duplicateEntries(): Collection<DuplicateGroup> = result.await()

    override suspend fun stop(): FindDuplicatesExecutionState {
        job.cancel(FindExecutionStopRequested())
        job.join()
        return stateHolder.state.value
    }

    override val state get() = stateHolder.state

    private fun collectFiles(stateHolder: FindProgressStateHolder): Collection<PathWithAttributes> =
        buildSet {
            val (initialDirectories, filter) =
                stateHolder.state.value.run {
                    check(this is ScanExecutionState)
                    initialDirectories to filter
                }
            initialDirectories.uniqueAndReal.forEach { directory ->
                scanDirectoryRecursively(directory, filter, stateHolder)
            }
        }

    private fun MutableSet<PathWithAttributes>.scanDirectoryRecursively(
        directory: Path,
        filter: PathFilter,
        stateHolder: FindProgressStateHolder,
    ) {
        directory.visitFileTree(followLinks = true) {
            onPreVisitDirectory { directory, attributes ->
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
            }
            onVisitFile { file, attributes ->
                if (shouldAddFile(
                        file,
                        filter,
                        attributes,
                    )
                ) {
                    add(PathWithAttributes(file.toRealPath(), attributes))
                }
                FileVisitResult.CONTINUE
            }
            onVisitFileFailed { _, _ -> FileVisitResult.CONTINUE }
            onPostVisitDirectory { directory, _ ->
                stateHolder.update { currentState ->
                    check(currentState is ScanExecutionStateImpl)
                    currentState.copy(
                        visitedDirectories = currentState.visitedDirectories + directory,
                    )
                }
                FileVisitResult.CONTINUE
            }
        }
    }

    private fun Map<String, Collection<PathWithAttributes>>.duplicateGroups(): Collection<DuplicateGroup> =
        map { (hash, paths) ->
            DuplicateGroup(hash = hash, paths = paths)
        }

    private fun List<PathWithAttributes>.withSameContent() =
        groupBy {
            it.contentHash()
        }.filter { (_, paths) -> paths.size > 1 }

    private fun Collection<PathWithAttributes>.withSameSize() =
        groupBy { it.size() }.filter { (_, paths) -> paths.size > 1 }

    private fun shouldVisitDirectory(
        directory: Path,
        filter: PathFilter,
        attributes: BasicFileAttributes,
        visitedDirectories: List<Path>,
    ): Boolean =
        directory !in visitedDirectories &&
            (filter !is DirectoryFilter || filter.accept(directory, attributes))

    private fun shouldAddFile(
        file: Path,
        filter: PathFilter,
        attributes: BasicFileAttributes,
    ) = (filter is DirectoryFilter || filter.accept(file, attributes)) && Files.isReadable(file)
}

private fun Path.contentHash(type: String = "MD5"): String = readBytes().contentHash(type)

private val Collection<Path>.uniqueAndReal: Collection<Path>
    get() =
        mapNotNull {
            try {
                it.toRealPath()
            } catch (e: NoSuchFileException) {
                null
            }
        }.distinct()

private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

private fun ByteArray.contentHash(type: String = "MD5"): String {
    val bytes = MessageDigest.getInstance(type).digest(this)

    fun printHexBinary(data: ByteArray): String =
        buildString(data.size * 2) {
            data.forEach { b ->
                val i = b.toInt()
                append(HEX_CHARS[i shr 4 and 0xF])
                append(HEX_CHARS[i and 0xF])
            }
        }
    return printHexBinary(bytes)
}

data class PathWithAttributes(
    val path: Path,
    val attributes: BasicFileAttributes,
) : BasicFileAttributes by attributes {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PathWithAttributes

        return path == other.path
    }

    override fun hashCode(): Int = path.hashCode()

    fun contentHash(type: String = "MD5"): String = path.contentHash(type)
}

internal class FindExecutionStopRequested : CancellationException()
