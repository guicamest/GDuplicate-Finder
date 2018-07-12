package com.sleepcamel.fileduplicatefinder.core.domain.fileadapter

import com.amazonaws.services.s3.model.ListObjectsRequest
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.sleepcamel.fileduplicatefinder.core.domain.s3.S3ObjectWrapper
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.ArrayList

class S3FileAdapter : FileAdapter<S3ObjectWrapper> {
    override fun lastModifiedDate(file: S3ObjectWrapper): Long = file.metadata.lastModified.time

    val log: Logger = LoggerFactory.getLogger(javaClass)

    fun isRoot(file: S3ObjectWrapper): Boolean {
        //file.metadata.lastModified
        return file.key.isEmpty()
    }

    override fun getName(file: S3ObjectWrapper): String {
        if ( isRoot(file)) return file.bucketName

        return file.key.split(FOLDER_DELIMITER.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[-1]
    }

    override fun getAbsolutePath(file: S3ObjectWrapper): String {
        return file.bucketName + "://" + file.key
    }

    override fun getFriendlyPath(file: S3ObjectWrapper): String {
        return file.bucketName + "://" + file.key
    }

    override fun exists(file: S3ObjectWrapper): Boolean {
        return file.exists()!!
    }

    override fun delete(file: S3ObjectWrapper): Boolean {
        file.s3.deleteObject(file.bucketName, file.key)
        return true
    }

    override fun isDir(file: S3ObjectWrapper): Boolean {
        return (file.key).endsWith("/") || (file.key).isEmpty()
    }

    override fun size(file: S3ObjectWrapper): Long? {
        return file.summary.size
    }

    override fun md5(file: S3ObjectWrapper): String {
        return StringUtils.leftPad(file.summary.eTag, MD5_STRING_LENGTH, "0")
    }

    override fun files(file: S3ObjectWrapper): List<Any> = getFiles(file,
                file.s3.listObjects(
                        ListObjectsRequest().withBucketName(file.bucketName).withPrefix(file.key)
                                .withDelimiter(FOLDER_DELIMITER).withMaxKeys(MAX_KEYS_PER_REQUEST)
                )
        ).map { it as Any }

    private fun getFiles(file: S3ObjectWrapper, objectListing: ObjectListing): List<S3ObjectWrapper> {
        val files = ArrayList<S3ObjectWrapper>()
        for (objectSummary in objectListing.objectSummaries) {
            if (objectSummary.key != file.key) {
                val wrapper = S3ObjectWrapper()
                wrapper.s3 = file.s3
                wrapper.summary = objectSummary
                files.add(wrapper)
            }
        }

        for (prefix in objectListing.commonPrefixes) {
            val wrapper = S3ObjectWrapper()

            wrapper.s3 = file.s3
            wrapper.summary = S3ObjectSummary().apply{
                bucketName = file.bucketName
                key = prefix
            }

            files.add(wrapper)
        }

        log.info("Fetched " + files.size.toString() + " files from s3")
        if (objectListing.isTruncated) {
            files.addAll(getFiles(file, file.s3.listNextBatchOfObjects(objectListing)))
        }

        return files
    }

    override fun inputStream(file: S3ObjectWrapper): InputStream {
        return file.`object`.objectContent
    }

    override fun depth(file: S3ObjectWrapper): Int {
        return (file.key).split(FOLDER_DELIMITER.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray().size
    }

    override fun getParentFile(file: S3ObjectWrapper): S3ObjectWrapper? {
        if ( isRoot(file) ) return null

        var parentKey = ""
        val paths = file.key.split(FOLDER_DELIMITER)
        if (paths.size > 1) {
            parentKey = "${paths.dropLast(1).joinToString(separator = FOLDER_DELIMITER)}$FOLDER_DELIMITER"
        }

        return S3ObjectWrapper.fromMetadata(file.s3, file.bucketName, parentKey, file.s3.getObjectMetadata(file.bucketName, parentKey))
    }

    override fun write(file: S3ObjectWrapper, oos: ObjectOutputStream) {
        oos.writeObject(file)
    }

    override fun read(ois: ObjectInputStream): S3ObjectWrapper {
        return ois.readObject() as S3ObjectWrapper
    }

    companion object {
        private val MD5_STRING_LENGTH = 32
        private val MAX_KEYS_PER_REQUEST = 2000
        private val FOLDER_DELIMITER = "/"
    }
}
