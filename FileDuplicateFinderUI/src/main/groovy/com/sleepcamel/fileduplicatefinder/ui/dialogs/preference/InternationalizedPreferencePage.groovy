package com.sleepcamel.fileduplicatefinder.ui.dialogs.preference

import org.eclipse.jface.dialogs.Dialog
import org.eclipse.jface.preference.FieldEditorPreferencePage
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite

import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources

abstract public class InternationalizedPreferencePage extends
		FieldEditorPreferencePage {

	private Button defaultsButton
	private Button applyButton
	FDFUIResources i18n = FDFUIResources.instance

	public InternationalizedPreferencePage() {
		noDefaultAndApplyButton()
	}

	protected void contributeButtons(Composite buttonBar) {
		GridLayout layout = (GridLayout) buttonBar.getLayout()
		layout.numColumns += 2
		String[] labels = [i18n.msg('FDFUI.preferencesDialogDefaults'), i18n.msg('FDFUI.preferencesDialogApply')] as String[]
		int widthHint = convertHorizontalDLUsToPixels(61)
		this.defaultsButton = new Button(buttonBar, 8)
		this.defaultsButton.setText(labels[0])
		Dialog.applyDialogFont(this.defaultsButton)
		GridData data = new GridData(256)
		Point minButtonSize = this.defaultsButton.computeSize(-1, -1, true)
		data.widthHint = Math.max(widthHint, minButtonSize.x)
		this.defaultsButton.setLayoutData(data)
		this.defaultsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performDefaults()
			}
		})
		this.applyButton = new Button(buttonBar, 8)
		this.applyButton.setText(labels[1])
		Dialog.applyDialogFont(this.applyButton)
		data = new GridData(256)
		minButtonSize = this.applyButton.computeSize(-1, -1, true)
		data.widthHint = Math.max(widthHint, minButtonSize.x)
		this.applyButton.setLayoutData(data)
		this.applyButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performApply()
			}
		})
		this.applyButton.setEnabled(isValid())
		applyDialogFont(buttonBar)
	}

	protected void updateApplyButton() {
		if (this.applyButton != null)
			this.applyButton.setEnabled(isValid())
	}

	protected Button getApplyButton() {
		return this.applyButton
	}

	protected Button getDefaultsButton() {
		return this.defaultsButton
	}
}