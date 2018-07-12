package com.sleepcamel.fileduplicatefinder.core.domain.fileadapter

import com.sleepcamel.fileduplicatefinder.core.util.MD5Utils

import java.io.*
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

@Deprecated("2.1.0", ReplaceWith("LocalFileAdapter", "LocalPathAdapter"), DeprecationLevel.WARNING)
class LocalFileAdapter : FileAdapter<File> {
    override fun lastModifiedDate(file: File): Long = file.lastModified()

    override fun getName(file: File): String {
        return file.name
    }

    override fun getAbsolutePath(file: File): String {
        return file.absolutePath
    }

    override fun getFriendlyPath(file: File): String {
        return getAbsolutePath(file)
    }

    override fun exists(file: File): Boolean {
        return file.exists()
    }

    override fun delete(file: File) = file.delete()

    override fun isDir(file: File): Boolean = file.isDirectory

    override fun size(file: File): Long = file.length()

    override fun inputStream(file: File): InputStream = FileInputStream(file)

    override fun depth(file: File): Int = file.absolutePath.count { "$it" == File.separator }

    override fun files(file: File): List<Any> {
        //https//groups.google.com/forum/#!topic/javaposse/5rB_mfVpu9U JAVA 7 NIO
        // Avoid isDir call (list returns null in that case)
        return file.list()?.map { File(file, it) as Any } ?: emptyList()
        //if (isDir(file)) file.listFiles()?.map { it as Any }?.toTypedArray() ?:emptyArray() else emptyArray()
    }

    override fun getParentFile(file: File): File = file.parentFile

    override fun write(file: File, oos: ObjectOutputStream) {
        oos.writeObject(file)
    }

    override fun read(ois: ObjectInputStream): File {
        return ois.readObject() as File
    }

    override fun md5(file: File): String {
        return MD5Utils.generateMD5(BufferedInputStream(inputStream(file)))
    }

}
