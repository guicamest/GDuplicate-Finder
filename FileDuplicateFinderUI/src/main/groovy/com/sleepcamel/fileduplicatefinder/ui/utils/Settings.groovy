package com.sleepcamel.fileduplicatefinder.ui.utils

import org.apache.commons.beanutils.BeanUtils
import org.eclipse.jface.preference.PreferenceStore


@Singleton
class Settings implements Serializable {

	static final long serialVersionUID = -6618469841127325812L
	static final String SETTINGS_FILENAME = 'settings.dat'
	
	def lastNetworkAuthModels
	transient def lastLocale
	transient def PreferenceStore ps
	
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
			lastLocale = new Locale(preferenceStore().getString('language'))
		}
	}
	
	def preferenceStore(){
		if ( !ps ){
			// Set the preference store
			ps = new PreferenceStore('gduplicatefinder.properties');
			ps.setDefault('language', Locale.getDefault().getLanguage())
			ps.setDefault('automaticUpdates', true)
			try {
				ps.load();
			} catch (IOException e) {
				// Ignore
			}
		}
		ps
	}
	
	def save(){
		FileOutputStream fos = new FileOutputStream(new File(SETTINGS_FILENAME))
		fos.withObjectOutputStream { oos ->
			oos.writeObject(this)
			oos.close()
		}
	}
}
