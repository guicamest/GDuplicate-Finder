package com.sleepcamel.fileduplicatefinder.core.domain.fileadapter

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


class LocalFileAdapter implements FileAdapter<File> {

	String getName(File file) {
		file.name;
	}

	String getAbsolutePath(File file) {
		file.absolutePath;
	}
	
	String getFriendlyPath(File file) {
		getAbsolutePath(file)
	}
	
	boolean exists(File file){
		file.exists()
	}

	boolean delete(File file) {
		file.delete();
	}

	boolean isDir(File file) {
		file.isDirectory();
	}

	Long size(File file) {
		file.size();
	}

	InputStream inputStream(File file){
		new FileInputStream(file);
	}
	
	int depth(File file){
		file.absolutePath.count(File.separator)
	}

	Object[] files(File file) {
		isDir(file)? file.listFiles() : [];
	}

	File getParentFile(File file) {
		file.getParentFile();
	}

	void write(File file, ObjectOutputStream oos) {
		oos.writeObject(file)
	}

	def read(ObjectInputStream ois) {
		ois.readObject()
	}

}
