

package com.sleepcamel.fileduplicatefinder.core.domain.s3

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectSummary

class S3ObjectWrapper implements Serializable {

	transient AmazonS3 s3
	transient S3ObjectSummary summary
	transient S3Object object
	transient ObjectMetadata metadata
	
	S3Object getObject(){
		if ( !object ){
			object = s3.getObject(bucketName, summary.key)
		}
		object
	}
	
	def exists() {
		try{
			getMetadata()
		}catch(AmazonServiceException e){
			return e.getStatusCode() != 404
		}
		return true
	}
	
	ObjectMetadata getMetadata(){
		if ( !metadata ){
			metadata = s3.getObjectMetadata(bucketName, summary.key)
		}
		metadata
	}
	
	def getBucketName(){
		summary.getBucketName()
	}
	
	def getKey(){
		summary.getKey()
	}
	
	static S3ObjectWrapper fromMetadata(def s3, def bucketName, def key, ObjectMetadata metadata){
		def summary = summaryFromMetadata(metadata)
		summary.bucketName = bucketName
		summary.key = key
		new S3ObjectWrapper(s3:s3, summary: summary, metadata: metadata)
	}

	static S3ObjectSummary summaryFromMetadata(ObjectMetadata metadata){
		new S3ObjectSummary(eTag : metadata.getETag(), size: metadata.contentLength, lastModified: metadata.lastModified)
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException{
		oos.defaultWriteObject()
		AWSCredentials credentials = s3.awsCredentialsProvider.getCredentials()
		oos.writeObject(credentials.getAWSAccessKeyId())
		oos.writeObject(credentials.getAWSSecretKey())
		oos.writeObject(summary.getBucketName())
		oos.writeObject(summary.getETag())
		oos.writeObject(summary.getKey())
		oos.writeObject(summary.getLastModified())
		oos.writeObject(summary.getSize())
	}
	
	private void readObject(ObjectInputStream ois) throws IOException{
		ois.defaultReadObject()
		def access = ois.readObject()
		def secret = ois.readObject()
		s3 = new AmazonS3Client(new BasicAWSCredentials(access, secret))
		summary = new S3ObjectSummary()
		summary.bucketName = ois.readObject()
		summary.setETag(ois.readObject())
		summary.key = ois.readObject()
		summary.lastModified = ois.readObject()
		summary.size = ois.readObject()
	}
}
