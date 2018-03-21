package com.sleepcamel.fileduplicatefinder.core.util

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest

object MD5Utils {

    /*
    fun oldGenerateMD5(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        val `is` = BufferedInputStream(FileInputStream(file), 16384)
        val buffer = arrayOfNulls<Byte>(16384)
        var read = 0
        while ((read = `is`.read(buffer)) > 0) {
            digest.update(buffer, 0, read)
        }

        return BigInteger(1, digest.digest()).toString(16)
    }

    fun generateMD5(`is`: InputStream): String {
        var `is` = `is`
        `is` = BufferedInputStream(`is`, 16384)
        val digest = MessageDigest.getInstance("MD5")
        val buffer = arrayOfNulls<Byte>(16384)
        var read = 0
        while ((read = `is`.read(buffer)) > 0) {
            digest.update(buffer, 0, read)
        }

        DefaultGroovyMethodsSupport.closeQuietly(`is`)
        return BigInteger(1, digest.digest()).toString(16)
    }
    */
    fun generateMD5(`is`: InputStream): String = TODO("tokt")

}
