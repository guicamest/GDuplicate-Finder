package com.sleepcamel.gduplicatefinder.core

import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.absolute
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText

private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

fun String.contentHash(type: String = "MD5"): String {
    val bytes = MessageDigest.getInstance(type).digest(toByteArray())

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

fun createDuplicates(
    directoryPrefix: String = "test",
    directory: Path = createTempDirectory(directoryPrefix),
    content: String = "hi",
): Pair<Path, DuplicateGroup> {
    val fileNames =
        (0..1).map {
            kotlin.io.path.createTempFile(directory = directory).apply {
                writeText(content)
            }.absolute()
        }
    val group = DuplicateGroup(hash = content.contentHash(), paths = fileNames)
    return Pair(directory, group)
}

fun findDuplicates(
    finder: DuplicateFinder,
    directories: Collection<Path>,
    filter: PathFilter = MinSizeFilter(0),
) = finder.find(directories, filter)

fun findDuplicates(
    parentScope: CoroutineScope,
    newFinderInstance: (CoroutineScope) -> DuplicateFinder = { scope -> SequentialDuplicateFinder(scope) },
    directories: Collection<Path>,
    filter: PathFilter = MinSizeFilter(0),
) = findDuplicates(newFinderInstance(parentScope), directories, filter)

fun findDuplicates(
    finder: DuplicateFinder,
    directory: Path,
    filter: PathFilter = MinSizeFilter(0),
) = findDuplicates(finder, listOf(directory), filter)

fun findDuplicates(
    parentScope: CoroutineScope,
    newFinderInstance: (CoroutineScope) -> DuplicateFinder = { scope -> SequentialDuplicateFinder(scope) },
    directory: Path,
    filter: PathFilter = MinSizeFilter(0),
) = findDuplicates(parentScope, newFinderInstance, listOf(directory), filter)
