package com.sleepcamel.fileduplicatefinder.core.domain.fileadapter;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public interface FileAdapter<E> extends Serializable {

	String getName(E file);
	
	String getAbsolutePath(E file);
	
	String getFriendlyPath(E file);
	
	boolean exists(E file);

	boolean delete(E file);
	
	boolean isDir(E file);
	
	Long size(E file);
	
	String md5(E file);
	
	Object[] files(E file);

	InputStream inputStream(E file);
	
	int depth(E file);

	E getParentFile(E file);

	void write(E file, ObjectOutputStream oos);
	
	E read(ObjectInputStream ois);
}
