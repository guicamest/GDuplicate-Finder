package com.sleepcamel.fileduplicatefinder.ui.dialogs


import org.apache.commons.lang3.StringUtils
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.widgets.Dialog
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.jface.viewers.ListViewer
import org.eclipse.jface.viewers.ArrayContentProvider
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.swt.SWT

import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources
import com.sleepcamel.fileduplicatefinder.ui.utils.Settings

public class LanguagesDialog extends Dialog {

	Shell languagesShl
	FDFUIResources i18n = FDFUIResources.instance

	public LanguagesDialog(Shell parent, int style) {
		super(parent, style)
	}

	public void open() {
		createContents()
		languagesShl.open()
		languagesShl.layout()
		Display display = getParent().getDisplay()
		while (!languagesShl.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep()
			}
		}
	}

	private void createContents() {
		languagesShl = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL)
		languagesShl.setSize(400, 300)
		languagesShl.setText(i18n.msg('FDFUI.languagesDialogTitle'))
		
		Label lblTheFollowingFiles = new Label(languagesShl, SWT.NONE)
		lblTheFollowingFiles.setBounds(10, 10, 380, 15)
		lblTheFollowingFiles.setText(i18n.msg('FDFUI.languagesDialogText'))
		
		ListViewer viewer = new ListViewer(languagesShl, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER)
		viewer.setContentProvider(new ArrayContentProvider())
		viewer.setLabelProvider(new LabelProvider(){
			String getText(element) {
				StringUtils.capitalize(element.getDisplayName(element))
			}
		})
		viewer.setInput(FDFUIResources.instance.availableLocales)
		viewer.setSelection(new StructuredSelection(Settings.instance.getLastLocale()))
		viewer.getList().setBounds(10, 31, 380, 201)
		
		Button btnOk = new Button(languagesShl, SWT.NONE)
		btnOk.setBounds(100, 238, 200, 25)
		btnOk.setText(i18n.msg('FDFUI.languagesDialogSetAsDefault'))
		btnOk.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( !viewer.getSelection().isEmpty() ){
					def selection = viewer.getSelection().getFirstElement()
					if ( selection != Settings.instance.lastLocale ){
						Settings.instance.lastLocale = selection
						def displayName = StringUtils.capitalize(selection.getDisplayName(selection))
						MessageDialog.openInformation(languagesShl, i18n.msg('FDFUI.languagesDialogLanguageChangedTitle'), i18n.msg('FDFUI.languagesDialogLanguageChangedText', displayName))
					}
				}
			}
		})
	}

}
