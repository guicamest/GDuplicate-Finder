package com.sleepcamel.fileduplicatefinder.ui.dialogs.preference

import org.apache.commons.lang3.StringUtils
import org.eclipse.jface.dialogs.IMessageProvider
import org.eclipse.jface.preference.ComboFieldEditor

import com.sleepcamel.fileduplicatefinder.ui.PropertyConsts
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources

public class LanguagesPreferencePage extends InternationalizedPreferencePage {

	FDFUIResources i18n = FDFUIResources.instance
	
	LanguagesPreferencePage(){
		title = i18n.msg('FDFUI.preferencesDialogLanguagePageTitle')
	}
	
	protected void createFieldEditors() {
		List<Locale> locales = FDFUIResources.instance.availableLocales
		String [][] f = locales.collect{ [StringUtils.capitalize(it.getDisplayName(it)), it.getLanguage() ] }
		ComboFieldEditor comboFieldEditor = new ComboFieldEditor(PropertyConsts.LANGUAGE, i18n.msg('FDFUI.preferencesDialogLanguagePageChooseLanguage'), f, getFieldEditorParent())
		addField(comboFieldEditor)
	}
	
	public boolean performOk() {
		boolean ok = super.performOk()
		def ps = getPreferenceStore()
		if ( ok && ps ){
			def ll = new Locale(ps.getString(PropertyConsts.LANGUAGE))
			def msg = i18n.msg('FDFUI.preferencesDialogLanguagePageChangedText', StringUtils.capitalize(ll.getDisplayName(ll)))
			setMessage(msg, IMessageProvider.INFORMATION)
		}
		ok
	}

}
