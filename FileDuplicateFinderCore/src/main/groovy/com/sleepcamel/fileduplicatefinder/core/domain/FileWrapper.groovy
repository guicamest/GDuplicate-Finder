package com.sleepcamel.fileduplicatefinder.core.domain

import static groovyx.gpars.GParsPool.withExistingPool

import org.apache.commons.vfs.FileObject


import groovy.beans.Bindable
import groovy.util.logging.Commons
import groovyx.gpars.GParsPool;
import jcifs.smb.SmbFile

import com.sleepcamel.fileduplicatefinder.core.domain.fileadapter.FileAdapter
import com.sleepcamel.fileduplicatefinder.core.domain.fileadapter.FileObjectAdapter
import com.sleepcamel.fileduplicatefinder.core.domain.fileadapter.LocalFileAdapter
import com.sleepcamel.fileduplicatefinder.core.domain.fileadapter.SmbFileAdapter
import com.sleepcamel.fileduplicatefinder.core.domain.filefilters.DirectoryFilter
import com.sleepcamel.fileduplicatefinder.core.domain.filefilters.FileWrapperFilter
import com.sleepcamel.fileduplicatefinder.core.util.MD5Utils

@Bindable
@Commons
class FileWrapper implements Serializable {

	def isRoot = false
	
	static def loadFilesPool = GParsPool.createPool(10)
	static def filterFilesPool = GParsPool.createPool(5)
	
	static Map<Class, FileAdapter<?>> adapters = [:]
	FileAdapter<?> adapterToUse
	transient Object file
	transient Object parent
	
	List files = [].asSynchronized()
	List dirs = [].asSynchronized()
	
	@Bindable
	String name
	boolean filesLoaded = false
	
	Integer depth = -1
	def size = 0
	
	@Bindable
	String path = ''
	String friendlyPath = ''
	
	@Bindable
	def md5 = ''
	
	static{
		adapters.put(FileObject, new FileObjectAdapter())
		adapters.put(SmbFile, new SmbFileAdapter())
		adapters.put(File, new LocalFileAdapter())
	}
	
	FileWrapper(file){
		this(file, null)
	}

	FileWrapper(file, parent){
		this.file = file
		this.parent = parent
		adapterToUse = getAdapterToUse(file?.class)
		if ( adapterToUse == null ){
			log.info("No adapter found for file ${file}")
		}else{
			size = adapterToUse.size(file)
			path = adapterToUse.getAbsolutePath(file)
			friendlyPath = adapterToUse.getFriendlyPath(file)
			name = adapterToUse.getName(file)?:path
		}
	}
	
	static def getAdapterToUse(fileClass){
		// Try direct match
		def adapter = adapters.get(fileClass)
		if ( !adapter && fileClass ){
			// Check for subclasses
			adapters.keySet().each {
				if ( !adapter && it.isAssignableFrom(fileClass) ){
					adapter = adapters[it]
				}
			}
			// Subclass found, add it to the map
			if ( adapter ){
				adapters.put(fileClass, adapter)
			}
		}
		adapter
	}
	
	boolean delete(){
		adapterToUse.delete(file)
	}
	
	boolean isDir(){
		adapterToUse.isDir(file)
	}
	
	def loadFiles(forceUpdate){
		if ( (!filesLoaded || forceUpdate) && adapterToUse ){
			files.clear()
			filesLoaded = true
			withExistingPool(loadFilesPool){
				def list = adapterToUse.files(file)?.collectParallel{ new FileWrapper(it, file) }
				if (list){
					files.addAll(list)
				}
			}
		}
	}
	
	def md5(){
		if ( md5.isEmpty() ){
			md5 = MD5Utils.generateMD5(inputStream())
		}
		md5
	}
	
	def inputStream(){
		new BufferedInputStream(adapterToUse.inputStream(file))
	}
	
	def isFatherOrGrandFatherOf(FileWrapper possibleSon){
		if ( depth() >= possibleSon.depth() )
			return false;

		def sonsParentWrapper = possibleSon.getParentWrapper()
		(sonsParentWrapper == this) ?: isFatherOrGrandFatherOf(sonsParentWrapper)
	}
	
	public FileWrapper getParentWrapper(){
		new FileWrapper(getParentFile())
	}
	
	private Object getParentFile() {
		parent?:adapterToUse.getParentFile(file)
	}
	
	int depth(){
		if ( depth == -1 ){
			depth = adapterToUse.depth(file)
		}
		depth
	}
	
	List filterFiles(FileWrapperFilter fileWrapperFilter){
		loadFiles(false)
		withExistingPool(filterFilesPool){
			files.findAllParallel{fileWrapperFilter.accept(it)}
		}
	}
	
	List dirs(){
		dirs.clear()
		dirs.addAll(filterFiles(new DirectoryFilter()))
		dirs
	}
	
	boolean isLocal(){
		adapterToUse?.class == LocalFileAdapter
	}

	String toString() {"${file?.name} ($path)"}

	public boolean equals(Object obj) {
		if (obj == null)
			return false
		if (getClass() != obj.getClass())
			return false
		FileWrapper other = (FileWrapper) obj
		if (file == null) {
			if (other.file != null)
				return false
		}
		if (adapterToUse == null) {
			if (other.adapterToUse != null)
				return false
		}else if (other.adapterToUse == null || adapterToUse.class != other.adapterToUse.class){
			return false
		}

		return path.equals(other.path)
	}

	private void writeObject(ObjectOutputStream oos) throws IOException{
		oos.defaultWriteObject()
		adapterToUse.write(file, oos)
		adapterToUse.write(parent, oos)
	}
	
	private void readObject(ObjectInputStream ois) throws IOException{
		ois.defaultReadObject()
		file = adapterToUse.read(ois)
		parent = adapterToUse.read(ois)
	}
}
