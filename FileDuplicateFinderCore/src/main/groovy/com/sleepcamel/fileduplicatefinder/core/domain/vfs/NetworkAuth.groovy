package com.sleepcamel.fileduplicatefinder.core.domain.vfs




import com.sleepcamel.fileduplicatefinder.core.domain.fileadapter.FileObjectAdapter

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

		sb.append("${protocol.toLowerCase()}://")
		if (!protocol.toLowerCase().equals('file')) {
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
		FileObjectAdapter.resolveFileObject(sharePath())
	}

	public String toString() {
		"$username @ $hostname - $folder"
	}

	def checkPathExists = {
		folderFile()?.exists()
	}
}
