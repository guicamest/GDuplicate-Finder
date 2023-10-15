package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.fileSize
import kotlin.io.path.readBytes
import kotlin.io.path.visitFileTree

data class DuplicateGroup(val hash: String, val paths: Collection<Path>)

interface FindDuplicatesExecution {
    suspend fun duplicateEntries(): Collection<DuplicateGroup>
}

@OptIn(ExperimentalPathApi::class)
class CoroutinesFindDuplicatesExecution(
    private val directories: Collection<Path>,
    coroutineScope: CoroutineScope,
) : FindDuplicatesExecution {
    private val result = CompletableDeferred<Collection<DuplicateGroup>>()
    private val job: Job

    init {
        job =
            coroutineScope.launch {
                val allFiles =
                    directories.flatMap { directory ->
                        buildList {
                            directory.visitFileTree {
                                onVisitFile { file, _ ->
                                    add(file)
                                    FileVisitResult.CONTINUE
                                }
                                onVisitFileFailed { _, _ -> FileVisitResult.CONTINUE }
                            }
                        }
                    }
                val withSameSize = allFiles.withSameSize()
                val withSameContent =
                    withSameSize.mapNotNull { (_, groupWithSameSize) ->
                        groupWithSameSize.withSameContent().map { (hash, paths) ->
                            DuplicateGroup(hash = hash, paths = paths)
                        }
                    }
                result.complete(withSameContent.flatten())
            }
    }

    override suspend fun duplicateEntries(): Collection<DuplicateGroup> = result.await()

    private fun List<Path>.withSameContent() = groupBy { it.contentHash() }.filter { (_, paths) -> paths.size > 1 }

    private fun List<Path>.withSameSize(): Map<Long, List<Path>> = groupBy { it.fileSize() }.filter { (_, paths) -> paths.size > 1 }
}

private fun Path.contentHash(type: String = "MD5"): String = readBytes().contentHash(type)

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
