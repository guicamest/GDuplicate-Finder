package com.sleepcamel.fileduplicatefinder.core.util

/** Copyleft (C) 2016 by Aaron Digulla. Use as you wish. This copyright notice can be removed. */
// Borrowed from https://gist.github.com/digulla/31eed31c7ead29ffc7a30aaf87131def

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Arrays

/**
 * Check whether a file is on a network drive.
 *
 *
 * Based on ideas from
 * [How to detect if a drive is network drive?](https://sites.google.com/site/baohuagu/how-to-detect-if-a-drive-is-network-drive)
 * and [Java: how to determine the type of drive a file is located on?](http://stackoverflow.com/questions/9163707/java-how-to-determine-the-type-of-drive-a-file-is-located-on)
 */
class DangerousPathChecker {

    fun isDangerous(dfile: File): Boolean {
        if (!IS_WINDOWS) {
            return false
        }

        // Make sure the file is absolute
        val file = dfile.absoluteFile
        val path = file.path
        //        System.out.println("Checking [" + path + "]");

        // UNC paths are dangerous
        if (path.startsWith("//") || path.startsWith("\\\\")) {
            // We might want to check for \\localhost or \\127.0.0.1 which would be OK, too
            return true
        }

        val driveLetter = path.substring(0, 1)
        val colon = path.substring(1, 2)
        if (":" != colon) {
            throw IllegalArgumentException("Expected 'X:': $path")
        }

        return isNetworkDrive(driveLetter)
    }

    /** Use the command `net` to determine what this drive is.
     * `net use` will return an error for anything which isn't a share.
     *
     *
     * Another option would be `fsinfo` but my gut feeling is that
     * `net` should be available and on the path on every installation
     * of Windows.
     */
    private fun isNetworkDrive(driveLetter: String): Boolean {
        val cmd = Arrays.asList("cmd", "/c", "net", "use", "$driveLetter:")

        try {
            val p = ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start()

            p.outputStream.close()

            val consoleOutput = StringBuilder()

            BufferedReader(InputStreamReader(p.inputStream)).use { `in` ->
                var line = `in`.readLine()
                while (line != null) {
                    consoleOutput.append(line).append("\r\n")
                    line = `in`.readLine()
                }
            }

            val rc = p.waitFor()
            //            System.out.println(consoleOutput);
            //            System.out.println("rc=" + rc);
            return rc == 0
        } catch (e: Exception) {
            throw IllegalStateException("Unable to run 'net use' on $driveLetter", e)
        }

    }

    private fun check(path: String) {
        val file = File(path).absoluteFile
        println(isDangerous(file).toString() + " - " + file)
    }

    companion object {
        private val IS_WINDOWS = isWindows

        // Along the lines of org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS
        private val isWindows: Boolean
            get() {
                val os = System.getProperty("os.name").toLowerCase()
                return os.startsWith("Windows", ignoreCase = true)
            }

        @JvmStatic
        fun main(args: Array<String>) {
            val checker = DangerousPathChecker()

            checker.check("")
            checker.check("//server/path")
            checker.check("\\\\server\\path")
            checker.check("c:")
            checker.check("c:/windows")
            checker.check("c:\\windows")
            // On my computer, Z: is a mapped network drive
            checker.check("z:")
            checker.check("z:/folder")
            checker.check("z:\\folder")
        }
    }
}