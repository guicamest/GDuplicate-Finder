package com.sleepcamel.fileduplicatefinder.ui.utils

import groovy.lang.Singleton
import groovy.transform.Synchronized
import groovy.util.logging.Commons

import java.io.File;
import java.text.MessageFormat;
import java.util.MissingResourceException
import java.util.ResourceBundle

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.graphics.Image
import com.sleepcamel.fileduplicatefinder.ui.utils.Settings

@Singleton
@Commons
class FDFUIResources {
	private final String BUNDLE_PATH = 'com.sleepcamel.fileduplicatefinder.ui.i18n.'
    private final String BUNDLE_BASENAME = "${BUNDLE_PATH}resources"
    private ResourceBundle resourceBundle
	
	def availableLocales = []

	def getImageFile(imgFile){
		new Image(null, this.getClass().getResourceAsStream("/com/sleepcamel/fileduplicatefinder/ui/images/${imgFile}"));
	}
	
	private FDFUIResources() {
		try{
			loadResources()
			log.info('Loaded i18n resources')
		}catch (Exception ex){
			log.error('Unable to load i18n resources', ex)
		}
	}
	
    private def loadResources() throws MissingResourceException{
		def langsFileName = "/${BUNDLE_PATH.replaceAll(/\./,/\\//)}langs.txt"
		def resource = FDFUIResources.class.getResource(langsFileName)
		
		File langsFile = File.createTempFile('ddf', 'langs')
		FileUtils.copyURLToFile(resource, langsFile)

		availableLocales = langsFile.readLines().collect {new Locale(it)}

        resourceBundle = ResourceBundle.getBundle(BUNDLE_BASENAME, Settings.instance.getLastLocale())
    }

	@Synchronized
    def setResourceBundle(ResourceBundle rb){
        if (rb == null){
            throw new IllegalArgumentException('ResourceBundle object musn\'t be null!')
        }

        resourceBundle = rb
    }

	def msg(String messageKey, Object... arr){
		MessageFormat.format(msg(messageKey), arr);
	}
	
    def msg(String messageKey){
		def msg = null
        if (messageKey != null) {
		    synchronized (resourceBundle) {
		        try {
		            msg = resourceBundle.getString(messageKey)
		        } catch (MissingResourceException ex){
		            msg = messageKey + ' Untranslated'
		            log.error('Unable to retrieve i18n key', ex)
		        }
		    }
        }
        msg
    }
}
