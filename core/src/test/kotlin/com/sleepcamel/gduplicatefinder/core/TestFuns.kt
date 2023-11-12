package com.sleepcamel.gduplicatefinder.core

import java.security.MessageDigest

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
