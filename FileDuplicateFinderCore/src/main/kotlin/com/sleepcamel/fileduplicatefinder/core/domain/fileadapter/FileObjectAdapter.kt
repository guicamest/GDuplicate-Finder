package com.sleepcamel.fileduplicatefinder.core.domain.fileadapter

import com.sleepcamel.fileduplicatefinder.core.util.MD5Utils
import org.apache.commons.vfs.*
import org.apache.commons.vfs.impl.StandardFileSystemManager
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.regex.Pattern

class FileObjectAdapter : FileAdapter<FileObject> {

    override fun getName(file: FileObject): String = file.name.baseName ?: file.name.friendlyURI

    override fun getAbsolutePath(file: FileObject): String {
        return file.url.toString()
    }

    override fun getFriendlyPath(file: FileObject): String {
        val m = friendlyNamePattern.matcher(file.url.toString())
        m.find()
        return m.group(1)
    }

    override fun exists(file: FileObject): Boolean {
        return file.exists()
    }

    override fun delete(file: FileObject): Boolean {
        return file.delete()
    }

    override fun isDir(file: FileObject): Boolean {
        return file.type == FileType.FOLDER
    }

    override fun size(file: FileObject): Long? {
        return if (isDir(file)) 0L else file.content.size
    }

    override fun md5(file: FileObject): String {
        return MD5Utils.generateMD5(BufferedInputStream(inputStream(file)))
    }

    override fun files(file: FileObject): Array<Any> = file.children.map { it as Any }.toTypedArray()

    override fun inputStream(file: FileObject): InputStream {
        return file.content.inputStream
    }

    override fun depth(file: FileObject): Int {
        var depth = 0
        var parent: FileObject? = getParentFile(file)
        while (parent != null) {
            depth++
            parent = getParentFile(parent)
        }

        return depth
    }

    override fun getParentFile(file: FileObject): FileObject {
        return file.parent
    }

    override fun write(file: FileObject, oos: ObjectOutputStream) {
        oos.writeObject(file.url.toString())
    }

    override fun read(ois: ObjectInputStream): FileObject {
        return resolveFileObject(ois.readObject() as String)
    }

    companion object {
        private val fileSystemManager: FileSystemManager by lazy {
            val fm = StandardFileSystemManager()
            fm.cacheStrategy = CacheStrategy.MANUAL
            fm.init()
            fm
        }
        val friendlyNamePattern = Pattern.compile("@[^/]+/(.*)")
        private val opts = FileSystemOptions()

        fun resolveFileObject(fileUri: String): FileObject {
            if (fileUri.startsWith("sftp://")) {
                SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no")
            }

            return fileSystemManager.resolveFile(fileUri, opts)
        }

    }
}
