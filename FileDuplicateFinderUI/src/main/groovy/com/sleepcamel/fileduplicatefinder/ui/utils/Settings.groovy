package com.sleepcamel.fileduplicatefinder.ui.utils

import org.apache.commons.beanutils.BeanUtils


@Singleton
class Settings implements Serializable {

	static final long serialVersionUID = -6618469841127325812L
	static final String SETTINGS_FILENAME = 'settings.dat'
	
	def lastNetworkAuthModels
	def lastLocale = Locale.getDefault()
	
	def getLastNetworkDrivesAuthModels(){
		lastNetworkAuthModels?:[]
	}
	
	def getLastLocale(){
		lastLocale
	}
	
	def getMountedNetworkDrivesAuthModels(){
		getLastNetworkDrivesAuthModels().findAll { it.isMounted }.collect { it.auth }
	}
	
	private Settings(){
		def file = new File(SETTINGS_FILENAME)
		boolean invalidFile = false
		if ( file.exists() ){
			file.withObjectInputStream { ois ->
				try{
					BeanUtils.copyProperties(this, ois.readObject())
				}catch(Exception e){
					invalidFile = true
				}
				ois.close()
			}
			if ( invalidFile ) file.delete()
		}
	}
	
	def save(){
		FileOutputStream fos = new FileOutputStream(new File(SETTINGS_FILENAME))
		fos.withObjectOutputStream { oos ->
			oos.writeObject(this)
			oos.close()
		}
	}
}
