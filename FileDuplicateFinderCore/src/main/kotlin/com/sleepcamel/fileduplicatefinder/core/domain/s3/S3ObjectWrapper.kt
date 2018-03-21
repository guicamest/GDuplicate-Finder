package com.sleepcamel.fileduplicatefinder.core.domain.s3

import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectSummary

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.net.URL
import java.util.Date

class S3ObjectWrapper : Serializable {

    lateinit var s3: AmazonS3Client
    lateinit var summary: S3ObjectSummary

    val `object`: S3Object by lazy { s3.getObject(bucketName, summary.key) }
    val metadata: ObjectMetadata by lazy { s3.getObjectMetadata(bucketName, summary.key) }

    fun exists(): Boolean? {
        try {
            metadata
        } catch (e: AmazonServiceException) {
            return e.statusCode != 404
        }

        return true
    }

    val bucketName: String
        get() = summary.bucketName

    val key: String
        get() = summary.key

    @Throws(IOException::class)
    private fun writeObject(oos: ObjectOutputStream) {
        oos.defaultWriteObject()
        // TODO We shouldn't do this for every file :S
        oos.writeObject(summary.bucketName)
        oos.writeObject(summary.eTag)
        oos.writeObject(summary.key)
        oos.writeObject(summary.lastModified)
        oos.writeObject(summary.size)
    }

    @Throws(IOException::class)
    private fun readObject(ois: ObjectInputStream) {
        ois.defaultReadObject()
        val access = ois.readObject() as String
        val secret = ois.readObject() as String
        s3 = AmazonS3Client(BasicAWSCredentials(access, secret))
        summary = S3ObjectSummary().apply {
            bucketName = ois.readObject() as String
            eTag = ois.readObject() as String
            key = ois.readObject() as String
            lastModified = ois.readObject() as Date
            size = ois.readObject() as Long
        }
    }

    companion object {

        fun fromMetadata(s3: AmazonS3Client, bucketName: String, key: String, metadata: ObjectMetadata): S3ObjectWrapper {
            val summary = summaryFromMetadata(metadata)
            summary.bucketName = bucketName
            summary.key = key
            val wrapper = S3ObjectWrapper()

            wrapper.summary = summary
            //TODO FIX THIS tokt wrapper.metadata = metadata
            wrapper.s3 = s3
            return wrapper
        }

        fun summaryFromMetadata(metadata: ObjectMetadata): S3ObjectSummary {
            val summary = S3ObjectSummary()
            summary.eTag = metadata.eTag
            summary.lastModified = metadata.lastModified
            summary.setSize(metadata.contentLength)
            return summary
        }
    }
}
