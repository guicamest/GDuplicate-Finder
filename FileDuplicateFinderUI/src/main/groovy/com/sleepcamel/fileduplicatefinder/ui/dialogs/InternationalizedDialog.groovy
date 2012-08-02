package com.sleepcamel.fileduplicatefinder.ui.dialogs

import org.eclipse.swt.widgets.Dialog
import org.eclipse.swt.widgets.Shell;

import com.sleepcamel.fileduplicatefinder.ui.tracking.AnalyticsTracker
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources

class InternationalizedDialog extends Dialog {

	FDFUIResources i18n = FDFUIResources.instance
	AnalyticsTracker tracker = AnalyticsTracker.instance
	String title
	
	public InternationalizedDialog(Shell paramShell, int paramInt, String title){
		super(paramShell, paramInt)
		this.title = i18n.msg(title)
	}
	
	public Object open() {
		def lastPage = tracker.lastPage
		tracker.trackPageView("/${i18n.keyForMsg(title)}")
		def retValue = doOpen()
		tracker.trackPageView(lastPage)
		retValue
	}
}
