package com.sleepcamel.fileduplicatefinder.core.domain.fileadapter

import com.sleepcamel.fileduplicatefinder.core.util.MD5Utils

import java.io.*

class LocalFileAdapter : FileAdapter<File> {

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

    override fun files(file: File): Array<Any> {
        return if (isDir(file)) file.listFiles()?.map { it as Any }?.toTypedArray() ?:emptyArray() else emptyArray()
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
