package com.sleepcamel.fileduplicatefinder.ui.dialogs.preference


import org.eclipse.jface.preference.PreferenceDialog
import org.eclipse.jface.preference.PreferenceManager
import org.eclipse.jface.preference.PreferenceNode
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Shell

import com.sleepcamel.fileduplicatefinder.ui.tracking.AnalyticsTracker;
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources
import com.sleepcamel.fileduplicatefinder.ui.utils.Settings

public class GDFPreferenceDialog extends PreferenceDialog {

	private Button okButton
	FDFUIResources i18n = FDFUIResources.instance
	AnalyticsTracker tracker = AnalyticsTracker.instance
	def title

	public GDFPreferenceDialog(Shell parentShell) {
		super(parentShell, getPreferenceMgr())
		title = i18n.msg('FDFUI.preferencesDialogTitle')
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell)
		newShell.setImages(getParentShell().getImages())
		newShell.setText(title)
	}

	public int open() {
		def lastPage = tracker.lastPage
		tracker.trackPageView("/${i18n.keyForMsg(title)}")
		setPreferenceStore(Settings.instance.preferenceStore())
		def retValue = super.open()
		tracker.trackPageView(lastPage)
		retValue
	}

	private static PreferenceManager getPreferenceMgr() {
		PreferenceManager mgr = new PreferenceManager()

		PreferenceNode generalNode = new PreferenceNode('general')
		PreferenceNode languageNode = new PreferenceNode('languages')
		generalNode.setPage(new GeneralPreferencePage())
		languageNode.setPage(new LanguagesPreferencePage())

		// Add the nodes
		mgr.addToRoot(generalNode)
		mgr.addToRoot(languageNode)
		mgr
	}

	protected void createButtonsForButtonBar(Composite parent) {
		this.okButton = createButton(parent, 0, i18n.msg('FDFUI.preferencesDialogOk'), true)
		getShell().setDefaultButton(this.okButton)
		createButton(parent, 1, i18n.msg('FDFUI.preferencesDialogCancel'), false)
	}

	public void updateButtons() {
		this.okButton.setEnabled(isCurrentPageValid())
	}

	protected Composite createTitleArea(Composite parent){
		def c = super.createTitleArea(parent)
		parent.setLayout(new GridLayout(marginHeight : 10, marginWidth : 0, horizontalSpacing : 0))
		c
	}

}
