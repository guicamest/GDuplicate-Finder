package com.sleepcamel.fileduplicatefinder.ui

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.codehaus.jackson.Version
import org.codehaus.jackson.map.ObjectMapper
import org.eclipse.swt.widgets.Display

import com.sleepcamel.fileduplicatefinder.ui.dialogs.UpdateDialog
import com.sleepcamel.fileduplicatefinder.ui.model.UpdateStatus;

@Singleton
class UpdateFinder {

	def props = new Properties()
	def currentVersion

	private UpdateFinder(){
		props.load(UpdateFinder.class.getResourceAsStream('app.properties'))
		currentVersion = props.getProperty('FDFUI.app.version')
	}
	
	def searchForUpdate(automaticCheck){
		Thread.start {
			def showDialog = !automaticCheck
			def status = UpdateStatus.NO_UPDATES
			def updateAppString
			def downloadUrl

			try{
				def appArch = props.getProperty('FDFUI.app.build')
				
				def appVersionInts = currentVersion.split('\\.').collect { Integer.parseInt(it) }
				def currentAppVersion = new Version(appVersionInts[0], appVersionInts[1], appVersionInts[2], null)
				
				HttpClient client = new DefaultHttpClient()
				HttpGet getMethod = new HttpGet('https://raw.github.com/guicamest/GDuplicate-Finder/master/version.json')
				HttpResponse response = client.execute(getMethod)
				Map map = new ObjectMapper().readValue(response.getEntity().getContent(), Map)
				
				updateAppString = map.version
				def updateVersionInts = map.version.split('\\.').collect { Integer.parseInt(it) }
				def updateAppVersion = new Version(updateVersionInts[0], updateVersionInts[1], updateVersionInts[2], null)

				if ( updateAppVersion.compareTo(currentAppVersion) > 0 ){
					showDialog = true
					status = UpdateStatus.NEW_AVAILABLE
					downloadUrl = map.builds["${appArch}"]
				}
			}catch(Exception e){
				status = UpdateStatus.ERROR
			}
			if ( showDialog ){
				Display.getDefault().syncExec(new Runnable() { public void run() {
					new UpdateDialog(status, updateAppString, downloadUrl).open()
				}})
			}
		}
	}
}
