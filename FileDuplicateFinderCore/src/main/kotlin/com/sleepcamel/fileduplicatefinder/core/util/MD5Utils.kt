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
    val HEX_CHARS = "0123456789ABCDEF"
    fun generateMD5(iss: InputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): String {
        val algo = MessageDigest.getInstance("MD5")
        iss.use {
            val buffer = ByteArray(bufferSize)
            var bytes = it.read(buffer)
            while (bytes >= 0) {
                algo.update(buffer, 0, bytes)
                bytes = it.read(buffer)
            }
        }

        return algo.digest().toHex()
    }

}
fun ByteArray.toHex() : String{
    val result = StringBuffer(size * 2)

    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(MD5Utils.HEX_CHARS[firstIndex])
        result.append(MD5Utils.HEX_CHARS[secondIndex])
    }

    return result.toString()
}