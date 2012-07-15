

package com.sleepcamel.fileduplicatefinder.core.domain.fileadapter

import groovy.util.logging.Commons

import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

import org.apache.commons.lang3.StringUtils

import com.amazonaws.services.s3.model.ListObjectsRequest
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.sleepcamel.fileduplicatefinder.core.domain.s3.S3ObjectWrapper

@Commons
class S3FileAdapter implements FileAdapter<S3ObjectWrapper>{

	private static int MD5_STRING_LENGTH = 32
	private static Integer MAX_KEYS_PER_REQUEST = 2000
	private static String FOLDER_DELIMITER = '/'
	
	def isRoot(file){
		file.key.isEmpty()
	}
	
	public String getName(S3ObjectWrapper file) {
		if ( isRoot(file) )
			return file.bucketName
		
		file.key.split(FOLDER_DELIMITER)[-1]
	}
	
	public String getAbsolutePath(S3ObjectWrapper file) {
		"${file.bucketName}://${file.key}"
	}
	
	public String getFriendlyPath(S3ObjectWrapper file) {
		"${file.bucketName}://${file.key}"
	}
	
	public boolean exists(S3ObjectWrapper file) {
		file.exists()
	}
	
	public boolean delete(S3ObjectWrapper file) {
		file.s3.deleteObject(file.bucketName, file.key)
		true
	}
	
	public boolean isDir(S3ObjectWrapper file) {
		file.key.endsWith('/') || file.key.isEmpty()
	}
	
	public Long size(S3ObjectWrapper file) {
		file.summary.size
	}
	
	String md5(S3ObjectWrapper file) {
		StringUtils.leftPad(file.summary.eTag, MD5_STRING_LENGTH, '0')
	}
	
	public Object[] files(S3ObjectWrapper file) {
		getFiles(file, file.s3.listObjects(new ListObjectsRequest().withBucketName(file.bucketName).withPrefix(file.key).withDelimiter(FOLDER_DELIMITER).withMaxKeys(MAX_KEYS_PER_REQUEST))) as Object[]
	}
	
	private getFiles(S3ObjectWrapper file, ObjectListing objectListing){
		def files = []
		for(S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
			if ( objectSummary.key != file.key ){
				files.add(new S3ObjectWrapper(s3: file.s3, summary: objectSummary))
			}
		}
		for (String prefix : objectListing.getCommonPrefixes()) {
			files.add(new S3ObjectWrapper(s3: file.s3, summary: new S3ObjectSummary(bucketName:file.bucketName, key: prefix) ) )
		}
		log.info("Fetched ${files.size()} files from s3")
		if(objectListing.isTruncated()){
			files.addAll(getFiles(file, file.s3.listNextBatchOfObjects(objectListing)))
		}
		files.flatten()
	}
	
	public InputStream inputStream(S3ObjectWrapper file) {
		file.getObject().getObjectContent()
	}
	
	public int depth(S3ObjectWrapper file) {
		file.key.split(FOLDER_DELIMITER).length
	}
	
	public S3ObjectWrapper getParentFile(S3ObjectWrapper file) {
		if ( isRoot(file) )
			return null

		def parentKey = ''		
		def paths = (file.key.split(FOLDER_DELIMITER) as List)
		if ( paths.size() > 1 ){
			parentKey = "${paths[0 .. -2].join(FOLDER_DELIMITER)}${FOLDER_DELIMITER}"
		}

		S3ObjectWrapper.fromMetadata(file.s3, file.bucketName, parentKey, file.s3.getObjectMetadata(file.bucketName, parentKey))
	}
	
	public void write(S3ObjectWrapper file, ObjectOutputStream oos) {
		oos.writeObject(file)
	}
	
	public S3ObjectWrapper read(ObjectInputStream ois) {
		ois.readObject()
	}

}
