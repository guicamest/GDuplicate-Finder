package com.sleepcamel.fileduplicatefinder.ui.tracking

import groovy.util.logging.Commons

import com.dmurph.tracking.AnalyticsConfigData
import com.dmurph.tracking.JGoogleAnalyticsTracker
import com.dmurph.tracking.VisitorData
import com.dmurph.tracking.JGoogleAnalyticsTracker.DispatchMode;
import com.dmurph.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion
import com.sleepcamel.fileduplicatefinder.ui.PropertyConsts;
import com.sleepcamel.fileduplicatefinder.ui.UpdateFinder
import com.sleepcamel.fileduplicatefinder.ui.utils.Settings

@Singleton
@Commons
class AnalyticsTracker {

	JGoogleAnalyticsTracker tracker
	AnalyticsConfigData configData
	
	private static final String ACCOUNT_FILE = 'analytics.properties'
	private static final String ACCOUNT_PROPERTY = 'analytics.account'
	
	private AnalyticsTracker(){
		try{
		def props = new Properties()
		props.load(AnalyticsTracker.getResourceAsStream(ACCOUNT_FILE))

		def ps = Settings.instance.preferenceStore()
		def vs = VisitorData.newSession(ps.getInt(PropertyConsts.USER_ID), ps.getInt(PropertyConsts.TS_FIRST), ps.getInt(PropertyConsts.TS_LAST), ps.getInt(PropertyConsts.VISITS))
		
		configData = new AnalyticsConfigData(props.getProperty(ACCOUNT_PROPERTY), vs)
		configData.setUserLanguage(Locale.getDefault().getLanguage())
		tracker = new JGoogleAnalyticsTracker(configData, GoogleAnalyticsVersion.V_4_7_2)
		tracker.setDispatchMode(DispatchMode.MULTI_THREAD)
		}catch(Exception e){
			log.error(e.getMessage())
		}
	}
	
	public trackStarted(){
		trackPageView('/')
		trackEvent('AppStarted','Version',UpdateFinder.instance.currentVersion)
		trackEvent('AppStarted','Lang',Settings.instance.getLastLocale()?.getLanguage())
	}
	
	def lastPage
	
	public trackPageView(String component){
		if ( component ){track{
			tracker.trackPageView(component, "Page $component", 'http://gduplicatefinder.com')
			lastPage = component
		}}
	}
	
	public trackEvent(String category, String action, String label){
		track{
			tracker.trackEvent(category, action, label)
		}
	}
	
	public trackLastEvent(String category, String action, String label){
		track{
			tracker.setDispatchMode(DispatchMode.SYNCHRONOUS)
			tracker.trackEvent(category, action, label)
		}
	}
	
	def track = { Closure c ->
		if ( Settings.instance.preferenceStore().getBoolean(PropertyConsts.TRACKING) ){
			try{
				c.call()
			}catch(Exception e){
			}
		}
	}

	long getCurrentTS(){
		configData.getVisitorData().getTimestampCurrent()
	}
	
	int getVisits(){
		configData.getVisitorData().getVisits();
	}
}
