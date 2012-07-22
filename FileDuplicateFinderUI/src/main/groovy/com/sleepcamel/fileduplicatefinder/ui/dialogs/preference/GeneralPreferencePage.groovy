package com.sleepcamel.fileduplicatefinder.ui.dialogs.preference

import org.eclipse.jface.preference.BooleanFieldEditor

import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources

public class GeneralPreferencePage extends InternationalizedPreferencePage {

	FDFUIResources i18n = FDFUIResources.instance
	
	public GeneralPreferencePage(){
		title = i18n.msg('FDFUI.preferencesDialogGeneralPageTitle')
	}
	
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor('automaticUpdates', i18n.msg('FDFUI.preferencesDialogGeneralPageAutomaticUpdateText'), getFieldEditorParent()))
	}

}
