package com.sleepcamel.fileduplicatefinder.core.domain.fileadapter

import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

interface FileAdapter<E> : Serializable {

    fun getName(file: E): String

    fun getAbsolutePath(file: E): String

    fun getFriendlyPath(file: E): String

    fun exists(file: E): Boolean

    fun delete(file: E): Boolean

    fun isDir(file: E): Boolean

    fun size(file: E): Long?

    fun md5(file: E): String

    fun files(file: E): Array<Any>

    fun inputStream(file: E): InputStream

    fun depth(file: E): Int

    fun getParentFile(file: E): E?

    fun write(file: E, oos: ObjectOutputStream)

    fun read(ois: ObjectInputStream): E
}
