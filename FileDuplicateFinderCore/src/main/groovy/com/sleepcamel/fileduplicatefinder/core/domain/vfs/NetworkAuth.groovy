package com.sleepcamel.fileduplicatefinder.core.domain.vfs




import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.sleepcamel.fileduplicatefinder.core.domain.fileadapter.FileObjectAdapter
import com.sleepcamel.fileduplicatefinder.core.domain.s3.S3ObjectWrapper

import groovy.beans.Bindable
import groovy.transform.EqualsAndHashCode
@Bindable
@EqualsAndHashCode
class NetworkAuth implements Serializable {

	String hostname = ''
	String username = ''
	String password = ''
	String folder = ''
	String protocol = 'SMB'
	int port = -1
	
	def sharePath(){
		StringBuilder sb = new StringBuilder()
		def folderStr = folder

		def lowerProtocol = protocol.toLowerCase()
		sb.append("${lowerProtocol}://")
		if (!lowerProtocol.equals('file')) {
			if (!(''.equals(username.trim()))) {
				sb.append(username)
				if (password.size() != 0) {
					sb.append(":$password")
				}

				sb.append('@')
			}

			sb.append(this.hostname)
			if (port != -1) {
				sb.append(":$port")
			}

			if ( !folderStr.startsWith('/') ){
				folderStr = "/$folderStr"
			}
		}

		return sb.append(folderStr).toString()
	}

	def folderFile(){
		if ( protocol == 'S3' ){
			AmazonS3Client s3 = new AmazonS3Client(new BasicAWSCredentials(username, password))
			def path = folder.trim()
			if ( !path.isEmpty() && !path.endsWith('/') ){
				path = "$path/"
			}
			return new S3ObjectWrapper(s3:s3, summary:new S3ObjectSummary(bucketName:hostname, key: path) )
		}
		FileObjectAdapter.resolveFileObject(sharePath())
	}

	public String toString() {
		"$username @ $hostname - $folder"
	}

	def checkPathExists(){
		folderFile()?.exists()
	}
}
