package com.sleepcamel.fileduplicatefinder.ui.utils


import org.apache.commons.beanutils.BeanUtils
import org.eclipse.jface.preference.PreferenceStore

import com.sleepcamel.fileduplicatefinder.ui.PropertyConsts;


@Singleton
class Settings implements Serializable {

	{
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
		lastLocale = new Locale(preferenceStore().getString(PropertyConsts.LANGUAGE))
	}

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

	def preferenceStore(){
		if ( !ps ){
			// Set the preference store
			ps = new PreferenceStore(PROPERTIES_FILENAME)
			ps.setDefault(PropertyConsts.LANGUAGE, Locale.getDefault().getLanguage())
			ps.setDefault(PropertyConsts.AUTOMATIC_UPDATES, true)
			ps.setDefault(PropertyConsts.TRACKING, true)
			ps.setDefault(PropertyConsts.FIRST_TIME, true)
			try {
				ps.load()
				initUserExperience(ps)
			} catch (IOException e) {
				// Ignore
			}
		}
		ps
	}

	void initUserExperience(PreferenceStore ps){
		Integer userId = ps.getInt(PropertyConsts.USER_ID)
		long now = now()
		def save = false
		if ( !userId ){
			save = true
			def r = new Random();
			ps.setValue(PropertyConsts.USER_ID, r.nextInt())
		}
		Long first = ps.getLong(PropertyConsts.TS_FIRST)
		if ( !first ){
			save = true
			ps.setValue(PropertyConsts.TS_FIRST, now)
		}
		Long last = ps.getLong(PropertyConsts.TS_LAST)
		if ( !last ){
			save = true
			ps.setValue(PropertyConsts.TS_LAST, now)
		}
		Integer visits = ps.getInt(PropertyConsts.VISITS)
		if ( !visits ){
			save = true
			ps.setValue(PropertyConsts.VISITS, 0)
		}
		if ( save ){ ps.save() }
	}
	
	private static long now() {
		long now = System.currentTimeMillis() / 1000L;
		return now;
	}

	def save(){
		FileOutputStream fos = new FileOutputStream(new File(SETTINGS_FILENAME))
		fos.withObjectOutputStream { oos ->
			oos.writeObject(this)
			oos.close()
		}
	}
	static final long serialVersionUID = -6618469841127325812L
	String SETTINGS_FILENAME = 'settings.dat'
	String PROPERTIES_FILENAME = 'gduplicatefinder.properties'
}
