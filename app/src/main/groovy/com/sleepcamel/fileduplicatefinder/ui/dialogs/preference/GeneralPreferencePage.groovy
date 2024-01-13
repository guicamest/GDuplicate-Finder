package com.sleepcamel.fileduplicatefinder.ui.dialogs.preference

import org.eclipse.jface.preference.BooleanFieldEditor

import com.sleepcamel.fileduplicatefinder.ui.PropertyConsts
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources

public class GeneralPreferencePage extends InternationalizedPreferencePage {

	FDFUIResources i18n = FDFUIResources.instance
	
	public GeneralPreferencePage(){
		title = i18n.msg('FDFUI.preferencesDialogGeneralPageTitle')
	}
	
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(PropertyConsts.AUTOMATIC_UPDATES, i18n.msg('FDFUI.preferencesDialogGeneralPageAutomaticUpdateText'), getFieldEditorParent()))
		addField(new BooleanFieldEditor(PropertyConsts.TRACKING, i18n.msg('FDFUI.preferencesDialogGeneralPageTrackingText'), getFieldEditorParent()))
	}

}
