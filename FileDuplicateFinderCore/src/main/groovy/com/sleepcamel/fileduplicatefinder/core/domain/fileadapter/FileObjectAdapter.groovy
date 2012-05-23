package com.sleepcamel.fileduplicatefinder.core.domain.fileadapter


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.regex.Pattern;

import org.apache.commons.vfs.CacheStrategy
import org.apache.commons.vfs.FileObject
import org.apache.commons.vfs.FileSystemException
import org.apache.commons.vfs.FileSystemManager
import org.apache.commons.vfs.FileSystemOptions
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.impl.StandardFileSystemManager
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder



class FileObjectAdapter implements FileAdapter<FileObject>{

	static fileSystemManager
	static Pattern friendlyNamePattern = Pattern.compile('@[^/]+/(.*)')

	private static FileSystemManager getFileSystemManager(){
		if (fileSystemManager == null){
			try	{
				StandardFileSystemManager fm = new StandardFileSystemManager();
				fm.setCacheStrategy(CacheStrategy.MANUAL);
				fm.init();
				fileSystemManager = fm;
			} catch (Exception exc)	{
				throw new RuntimeException(exc);
			}
		}
		return fileSystemManager;
	}

	private static FileSystemOptions opts = new FileSystemOptions();

	static FileObject resolveFileObject(String fileUri){
//		try{
			if (fileUri.startsWith("sftp://")){
				SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
			}
			return getFileSystemManager().resolveFile(fileUri, opts);
//		}catch (FileSystemException ex)	{
//			return null;
//		}
	}

	public String getName(FileObject file) {
		file.getName().getBaseName()?:file.getName().getFriendlyURI()
	}

	public String getAbsolutePath(FileObject file) {
		file.getURL().toString()
	}
	
	public String getFriendlyPath(FileObject file) {
		def m = friendlyNamePattern.matcher(file.getURL().toString())
		m.find()
		m.group(1)
	}

	public boolean delete(FileObject file) {
		file.delete()
	}

	public boolean isDir(FileObject file) {
		file.getType() == FileType.FOLDER
	}

	public Long size(FileObject file) {
		isDir(file) ? 0L : file.getContent().getSize()
	}

	public Object[] files(FileObject file) {
		file.getChildren()
	}

	InputStream inputStream(FileObject file) {
		file.getContent().getInputStream()
	}

	public int depth(FileObject file) {
		int depth = 0
		def parent = getParentFile(file)
		while( parent != null ){
			depth++
			parent = getParentFile(parent)
		}
		depth
	}

	public FileObject getParentFile(FileObject file) {
		file.getParent()
	}

	void write(FileObject file, ObjectOutputStream oos) {
		oos.writeObject(file.getURL().toString())
	}

	def read(ObjectInputStream ois) {
		resolveFileObject(ois.readObject())
	}
}
