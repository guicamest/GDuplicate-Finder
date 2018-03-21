package com.sleepcamel.fileduplicatefinder.core.domain

import org.apache.commons.lang3.time.StopWatch

import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class DuplicateFinderReporter {
    var percentFormatter = NumberFormat.getPercentInstance(Locale.getDefault())
    var progress: DuplicateFinderProgress? = null
    var reportScanInterval: Int? = 5
    var reportDuplicatesInterval: Int? = 1000
    var finishedScan: Boolean = false
    var finishedDuplicates: Boolean = false
    var ab = AtomicBoolean(false)
    var reportingThread: Any? = null

    fun startReportingScan() {
        /*return DefaultGroovyStaticMethods.start(null, object : Closure<Boolean>(this, this) {
            @JvmOverloads fun doCall(it: Any? = null): Boolean? {
                while (!DefaultGroovyMethods.asBoolean(progress!!.finishedScanning)) {
                    val lastFoundFile = if (DefaultGroovyMethods.asBoolean(progress!!.foundFiles)) progress!!.foundFiles[-1] else null
                    if (DefaultGroovyMethods.asBoolean(lastFoundFile)) {
                        println("Found file " + String.valueOf(lastFoundFile!!.path))
                        println("Files found: " + progress!!.totalFiles.toString() + " - Size: " + progress!!.totalFileSize.toString())
                    }

                    Thread.sleep(reportScanInterval!!.toLong())
                }

                return finishedScan = true
            }

        })
        */
        println("scan started")
    }

    @Synchronized
    fun finishedReportingScan(): Boolean {
        return finishedScan
    }

    fun suspendReporting() {
        ab.set(true)
    }

    fun resumeReporting() {
        ab.set(false)
        /*synchronized(ab) {
            ab.notify()
        }
*/
    }

    fun startReportingDuplicates() {
        /*
        return reportingThread = DefaultGroovyStaticMethods.start(null, object : Closure<Boolean>(this, this) {
            @JvmOverloads fun doCall(it: Any? = null): Boolean? {
                val stopWatch = StopWatch()
                stopWatch.start()

                while (!progress!!.finishedFindingDuplicates) {
                    Thread.sleep(reportDuplicatesInterval!!.toLong())
                    if (ab.get()) {
                        synchronized(ab) {
                            ab.wait()
                        }

                    }


                    val percentDone = DefaultGroovyMethods.invokeMethod(progress, "percentDone", arrayOfNulls<Any>(0))
                    val elapsedTime = stopWatch.time
                    val timeLeft = DefaultGroovyMethods.asType<Long>(Math.floor((elapsedTime * (1 - percentDone) / percentDone).toDouble()), Long::class.java)

                    println("\nProgress " + percentString(percentDone))
                    println("Processed " + String.valueOf(progress!!.processedFiles.invokeMethod("size", arrayOfNulls<Any>(0))) + " - Left " + progress!!.foundFiles.size.toString() + " files")
                    println("Processed " + String.valueOf(progress!!.processedFilesQty) + " of " + progress!!.totalFiles.toString() + " files")
                    println("Processed " + String.valueOf(progress!!.processedFileSize) + " of " + progress!!.totalFileSize.toString() + " bytes")
                    println("Elapsed time: " + DuplicateFinderReporter.formatInterval(elapsedTime) + " - Remaining time: " + DuplicateFinderReporter.formatInterval(timeLeft!!))
                }

                return finishedDuplicates = true
            }

        })
        */
    }

    private fun formatInterval(millis: Long): String {
        val hr = TimeUnit.MILLISECONDS.toHours(millis)
        val min = TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.HOURS.toMillis(hr))
        val sec = TimeUnit.MILLISECONDS.toSeconds(millis - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min))
        return String.format("%02d:%02d:%02d", hr, min, sec)
    }

    fun percentString(percent: Any): String {
        return percentFormatter.format(percent)
    }

    fun finishedReportingDuplicates(): Boolean {
        return finishedDuplicates
    }

}
