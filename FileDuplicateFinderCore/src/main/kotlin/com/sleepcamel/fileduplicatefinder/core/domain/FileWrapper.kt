package com.sleepcamel.fileduplicatefinder.core.domain

import com.sleepcamel.fileduplicatefinder.core.domain.fileadapter.*
import com.sleepcamel.fileduplicatefinder.core.domain.filefilters.DirectoryFilter
import com.sleepcamel.fileduplicatefinder.core.domain.filefilters.FileWrapperFilter
import com.sleepcamel.fileduplicatefinder.core.domain.s3.S3ObjectWrapper
import jcifs.smb.SmbFile
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.apache.commons.lang3.time.StopWatch
import org.apache.commons.vfs.FileObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.beans.PropertyChangeListener
import java.io.*

open class FileWrapper<E: Any> @JvmOverloads constructor(@Transient val file: E, @Transient val parent: E? = null) : Serializable {

    val log: Logger = LoggerFactory.getLogger(javaClass)

    var uiRoot: Boolean? = false
    val adapterToUse: FileAdapter<E>? = getAdapterToUse(if (file == File("")) null else file::class.java)?.let { it as FileAdapter<E> }
    val files: MutableList<FileWrapper<E>> = mutableListOf() // TODO tokolin synchronized
    val dirs: MutableList<FileWrapper<E>> = mutableListOf() // TODO tokolin synchronized

    var filesLoaded = false

    val size: Int by lazy { adapterToUse?.size(file)?.toInt() ?: 0 }
    val md5: String by lazy { if (isDir) "" else adapterToUse!!.md5(file) }
    val depth by lazy { adapterToUse?.depth(file) ?: -1 }

    val path: String? by lazy {adapterToUse?.getAbsolutePath(file)}
    val name: String? by lazy {adapterToUse?.getName(file) ?: path}
    val friendlyPath: String? by lazy { adapterToUse?.getFriendlyPath(file) }
    val lastModified: Long? by lazy { adapterToUse?.lastModifiedDate(file) }

    val isLocal: Boolean
        get() {
            val use = adapterToUse
            return use?.javaClass == LocalPathAdapter::class.java || use?.javaClass == LocalFileAdapter::class.java
        }

    init {
        if (adapterToUse == null) {
            log.warn("No adapter found for file " + file.toString())
        }
    }

    fun delete(): Boolean {
        return adapterToUse!!.delete(file)
    }

    val isDir: Boolean
        get() = adapterToUse!!.isDir(file)

    val parentWrapper: FileWrapper<E>? by lazy { parentFile?.let { FileWrapper(it) } }

    private val parentFile: E? by lazy { parent?.let { adapterToUse!!.getParentFile(file) } }

    fun exists(): Boolean {
        return adapterToUse!!.exists(file)
    }

    fun loadFiles(forceUpdate: Boolean = false) {
        if ((!filesLoaded || forceUpdate) && adapterToUse != null) {
            files.clear()

            val asWrappers = adapterToUse.files(file).map { async { FileWrapper(it as E, file) } }
            runBlocking {
                asWrappers.forEach {
                    files.add(it.await())
                }
            }

            // https://stackoverflow.com/a/5700969
            filesLoaded = true
        }
    }

    fun inputStream(): BufferedInputStream {
        return BufferedInputStream(adapterToUse!!.inputStream(file))
    }

    fun isFatherOrGrandFatherOf(possibleSon: FileWrapper<*>): Boolean {
        if (depth >= possibleSon.depth) return false

        val sonsParentWrapper = possibleSon.parentWrapper
        val b = sonsParentWrapper == this
        return if (b) b else sonsParentWrapper?.let { isFatherOrGrandFatherOf(it) }?: false
    }

    fun filterFiles(fileWrapperFilter: FileWrapperFilter?): List<FileWrapper<E>> {
        loadFiles(false)
        val view = files.toList()

        return fileWrapperFilter?.let { filter ->
            // TODO Parallelize?
            return view.filter(filter::accept)
        } ?: view
    }

    fun dirs(): List<FileWrapper<E>> {
        dirs.clear()
        dirs.addAll(filterFiles(DirectoryFilter()))
        return dirs
    }

    override fun toString(): String {
        return "$name ($path)"
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (javaClass != obj.javaClass) return false
        val other = obj as FileWrapper<*>?
        if (file == null) {
            if (other!!.file != null) return false
        }

        if (adapterToUse == null) {
            if (other!!.adapterToUse != null) return false
        } else if (other!!.adapterToUse == null || adapterToUse.javaClass != other.adapterToUse!!.javaClass) {
            return false
        }

        return path == other.path
    }

    @Throws(IOException::class)
    private fun writeObject(oos: ObjectOutputStream) {
        oos.defaultWriteObject()
        adapterToUse!!.write(file, oos)
        parent?.apply { adapterToUse.write(this, oos) }
    }

    @Throws(IOException::class)
    private fun readObject(ois: ObjectInputStream) {
        ois.defaultReadObject()
        // TODO tokt file = adapterToUse!!.read(ois)
        // TODO tokt parent = adapterToUse.read(ois)
    }

    fun addPropertyChangeListener(listener: PropertyChangeListener) {//todo
    }

    fun addPropertyChangeListener(name: String, listener: PropertyChangeListener) {//todo
    }

    fun removePropertyChangeListener(listener: PropertyChangeListener) {//todo
    }

    fun removePropertyChangeListener(name: String, listener: PropertyChangeListener) {//todo
    }

    fun firePropertyChange(name: String, oldValue: Any, newValue: Any) {//todo
    }

    /*
    //todo
    val propertyChangeListeners: Array<PropertyChangeListener>
        get() {}

    fun getPropertyChangeListeners(name: String): Array<PropertyChangeListener> {//todo
    }*/

    companion object {

        fun <T> getAdapterToUse(fileClass: Class<T>?): FileAdapter<T>? {
            // Try direct match
            val computed = fileClass?.let {
                adapters.computeIfAbsent(it){ c ->
                    adapters[adapters.keys.first { it.isAssignableFrom(c) }]!!
                }
            }
            return computed as FileAdapter<T>?
        }

        private val adapters: MutableMap<Class<*>, FileAdapter<*>> = mutableMapOf(
                LocalPathWithAttributes::class.java to LocalPathAdapter(),
                FileObject::class.java to FileObjectAdapter(),
                SmbFile::class.java to SmbFileAdapter(),
                File::class.java to LocalFileAdapter(),
                S3ObjectWrapper::class.java to S3FileAdapter()
        )

    }
}
