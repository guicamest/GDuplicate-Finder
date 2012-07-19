package com.sleepcamel.fileduplicatefinder.ui.model;

import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources;
import com.sleepcamel.fileduplicatefinder.ui.utils.FileSize;

public enum UpdateStatus {
	NEW_AVAILABLE('Available'),
	NO_UPDATES('None'),
	ERROR('Error')
	
	private String friendlyName
	private static FDFUIResources i18n = FDFUIResources.instance
	
	private UpdateStatus(String friendlyName){
		this.friendlyName = friendlyName
	}
	
	public String getFriendlyName(){
		i18n.msg("FDFUI.update$friendlyName")
	}
	
}
