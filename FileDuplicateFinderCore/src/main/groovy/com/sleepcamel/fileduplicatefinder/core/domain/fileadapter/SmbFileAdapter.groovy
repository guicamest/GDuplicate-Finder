package com.sleepcamel.fileduplicatefinder.core.domain.fileadapter

import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

import com.sleepcamel.fileduplicatefinder.core.util.MD5Utils


import jcifs.smb.SmbFile

class SmbFileAdapter implements FileAdapter<SmbFile> {

	String getName(SmbFile file) {
		file.name.endsWith('/') ? file.name.substring(0, file.name.length()-1) : file.name
	}

	String getAbsolutePath(SmbFile file) {
		file.path
	}
	
	String getFriendlyPath(SmbFile file) {
		getAbsolutePath(file)
	}

	boolean exists(SmbFile file){
		file.exists()
	}

	boolean delete(SmbFile file) {
		try{
			file.delete()
			return true
		}catch(Exception e){
		}
		return false
	}

	boolean isDir(SmbFile file) {
		file.isDirectory()
	}

	Long size(SmbFile file) {
		file.size
	}
	
	InputStream inputStream(SmbFile file){
		file.getInputStream()
	}
	
	int depth(SmbFile file){
		getAbsolutePath(file).count('/')
	}

	Object[] files(SmbFile file) {
		isDir(file)? file.listFiles() : []
	}

	SmbFile getParentFile(SmbFile file) {
		new SmbFile(file.getParent())
	}

	void write(SmbFile file, ObjectOutputStream oos) {
		throw new RuntimeException('Not implemented yet')
	}

	def read(ObjectInputStream ois) {
		throw new RuntimeException('Not implemented yet')
	}

	@Override
	public String md5(SmbFile file) {
		MD5Utils.generateMD5(new BufferedInputStream(inputStream(file)))
	}

}
