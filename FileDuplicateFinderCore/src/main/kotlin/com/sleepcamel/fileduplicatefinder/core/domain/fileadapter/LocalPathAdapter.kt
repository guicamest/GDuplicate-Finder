package com.sleepcamel.fileduplicatefinder.core.domain.fileadapter

import com.sleepcamel.fileduplicatefinder.core.domain.LocalPathWithAttributes
import com.sleepcamel.fileduplicatefinder.core.util.MD5Utils
import org.apache.commons.vfs.FileObject

import java.io.*
import java.net.URI
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

class LocalPathAdapter : FileAdapter<LocalPathWithAttributes> {

    override fun lastModifiedDate(file: LocalPathWithAttributes): Long = file.lastModifiedTime().toMillis()

    override fun getName(file: LocalPathWithAttributes): String {
        val asString = file.path.toString()
        return if ( asString == file.path.toAbsolutePath().root.toString()) asString else file.path.fileName.toString()
    }

    override fun getAbsolutePath(file: LocalPathWithAttributes): String = file.path.toAbsolutePath().toString()

    override fun getFriendlyPath(file: LocalPathWithAttributes): String = getAbsolutePath(file)

    override fun exists(file: LocalPathWithAttributes): Boolean = file.exists()

    override fun delete(file: LocalPathWithAttributes): Boolean {
        TODO("not implemented")
    }

    override fun isDir(file: LocalPathWithAttributes): Boolean = file.isDirectory

    override fun size(file: LocalPathWithAttributes): Long? = file.size()

    override fun files(file: LocalPathWithAttributes): List<Any> {
        val value = LocalFileVisitor()
        // https://docs.oracle.com/javase/tutorial/essential/io/walk.html
        Files.walkFileTree(file.path, EnumSet.noneOf<FileVisitOption>(FileVisitOption::class.java), 1, value)
        return value.all
    }

    override fun inputStream(file: LocalPathWithAttributes): InputStream = Files.newInputStream(file.path, StandardOpenOption.READ)

    override fun md5(file: LocalPathWithAttributes): String = MD5Utils.generateMD5(inputStream(file))

    override fun depth(file: LocalPathWithAttributes): Int = file.path.toAbsolutePath().nameCount

    override fun getParentFile(file: LocalPathWithAttributes): LocalPathWithAttributes? = file.path.parent?.let {
        LocalPathWithAttributes(it, Files.readAttributes(it, BasicFileAttributes::class.java))
    }

    override fun write(file: LocalPathWithAttributes, oos: ObjectOutputStream) {
        oos.writeObject(file.path.toUri())
    }

    override fun read(ois: ObjectInputStream): LocalPathWithAttributes {
        val path = Paths.get(ois.readObject() as URI)
        return LocalPathWithAttributes(path, Files.readAttributes(path, BasicFileAttributes::class.java))
    }

}

class LocalFileVisitor: SimpleFileVisitor<Path>() {
    val all: MutableList<LocalPathWithAttributes> = mutableListOf()
    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
        super.preVisitDirectory(dir, attrs)
        //all.add(LocalPathWithAttributes(dir, attrs))
        return FileVisitResult.CONTINUE
    }

    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        super.visitFile(file, attrs)
        all.add(LocalPathWithAttributes(file, attrs))
        return FileVisitResult.CONTINUE
    }

    override fun visitFileFailed(file: Path?, exc: IOException?): FileVisitResult {
        // TODO, analyze
        return FileVisitResult.CONTINUE
    }

    override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
        // TODO, analyze
        return FileVisitResult.CONTINUE
    }
}