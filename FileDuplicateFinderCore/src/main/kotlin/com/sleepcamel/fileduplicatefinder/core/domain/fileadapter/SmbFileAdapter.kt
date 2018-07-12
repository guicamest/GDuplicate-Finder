package com.sleepcamel.fileduplicatefinder.core.domain.fileadapter


import com.sleepcamel.fileduplicatefinder.core.util.MD5Utils
import jcifs.smb.SmbFile
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class SmbFileAdapter : FileAdapter<SmbFile> {
    override fun lastModifiedDate(file: SmbFile): Long = file.lastModified()

    override fun getName(file: SmbFile): String {
        return if (file.name.endsWith("/")) file.name.substring(0, file.name.length - 1) else file.name
    }

    override fun getAbsolutePath(file: SmbFile): String {
        //file.lastModified() =  file.date
        //file.createTime()
        return file.path
    }

    override fun getFriendlyPath(file: SmbFile): String {
        return getAbsolutePath(file)
    }

    override fun exists(file: SmbFile): Boolean {
        return file.exists()
    }

    override fun delete(file: SmbFile): Boolean {
        try {
            file.delete()
            return true
        } catch (e: Exception) {
        }

        return false
    }

    override fun isDir(file: SmbFile): Boolean {
        return file.isDirectory
    }

    override fun size(file: SmbFile): Long? {
        return file.length()
    }

    override fun inputStream(file: SmbFile): InputStream {
        return file.inputStream
    }

    override fun depth(file: SmbFile): Int {
        return getAbsolutePath(file).count { it == '/' }
    }

    override fun files(file: SmbFile): List<Any> {
        // isDir necessary, listFiles on file causes Exception to be thrown
        return if (isDir(file)) file.listFiles().map { it as Any } else emptyList()
    }

    override fun getParentFile(file: SmbFile): SmbFile {
        return SmbFile(file.parent)
    }

    override fun write(file: SmbFile, oos: ObjectOutputStream) {
        throw RuntimeException("Not implemented yet")
    }

    override fun read(ois: ObjectInputStream): SmbFile {
        throw RuntimeException("Not implemented yet")
    }

    override fun md5(file: SmbFile): String {
        return MD5Utils.generateMD5(BufferedInputStream(inputStream(file)))
    }

}
