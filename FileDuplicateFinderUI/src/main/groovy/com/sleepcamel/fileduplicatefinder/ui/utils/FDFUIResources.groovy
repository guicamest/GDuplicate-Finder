package com.sleepcamel.fileduplicatefinder.ui.utils

import groovy.lang.Singleton
import groovy.transform.Synchronized
import groovy.util.logging.Commons

import java.text.MessageFormat;
import java.util.MissingResourceException
import java.util.ResourceBundle

import org.eclipse.swt.graphics.Image

@Singleton
@Commons
class FDFUIResources {
    private final String BUNDLE_BASENAME = 'com.sleepcamel.fileduplicatefinder.ui.i18n.resources'
    private ResourceBundle resourceBundle

	def getImageFile(imgFile){
		new Image(null, this.getClass().getResourceAsStream("/com/sleepcamel/fileduplicatefinder/ui/images/${imgFile}"));
	}
	
	public init(){
		try{
			loadResources()
			log.info('Loaded i18n resources')
		}catch (Exception ex){
			log.severe('Unable to load i18n resources', ex)
		}
	}
	
    // load the default message translations
    private def loadResources() throws MissingResourceException{
        resourceBundle = ResourceBundle.getBundle(BUNDLE_BASENAME)
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
		            log.severe('Unable to retrieve i18n key', ex)
		        }
		    }
        }
        msg
    }
}
