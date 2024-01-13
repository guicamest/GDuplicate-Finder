package com.sleepcamel.fileduplicatefinder.ui.dialogs

import java.util.List

import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import org.eclipse.jface.viewers.ListViewer
import org.eclipse.jface.viewers.ArrayContentProvider
import org.eclipse.swt.SWT

import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources

public class FilesNotFoundDialog extends InternationalizedDialog {

	Shell shlFilesWereNot
	List files

	public FilesNotFoundDialog(Shell parent, int style) {
		super(parent, style, 'FDFUI.filesNotFoundDialogTitle')
	}

	public Object doOpen() {
		createContents()
		shlFilesWereNot.open()
		shlFilesWereNot.layout()
		Display display = getParent().getDisplay()
		while (!shlFilesWereNot.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep()
			}
		}
	}

	private void createContents() {
		shlFilesWereNot = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL)
		shlFilesWereNot.setSize(450, 300)
		shlFilesWereNot.setText(title)
		
		Label lblTheFollowingFiles = new Label(shlFilesWereNot, SWT.NONE)
		lblTheFollowingFiles.setBounds(10, 10, 424, 15)
		lblTheFollowingFiles.setText(i18n.msg('FDFUI.filesNotFoundDialogText'))
		
		ListViewer viewer = new ListViewer(shlFilesWereNot, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER)
		viewer.setContentProvider(new ArrayContentProvider())
		viewer.setInput(files.toArray())
		viewer.getList().setBounds(10, 31, 424, 201)
		
		Button btnOk = new Button(shlFilesWereNot, SWT.NONE)
		btnOk.setBounds(186, 238, 75, 25)
		btnOk.setText(i18n.msg('FDFUI.filesNotFoundDialogContinueBtn'))
		btnOk.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shlFilesWereNot.dispose()
			}
		})
	}

}
