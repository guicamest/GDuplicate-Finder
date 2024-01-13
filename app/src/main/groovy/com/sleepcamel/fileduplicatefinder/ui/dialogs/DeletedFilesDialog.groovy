package com.sleepcamel.fileduplicatefinder.ui.dialogs

import java.util.List

import org.eclipse.swt.custom.SashForm
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import org.eclipse.jface.dialogs.Dialog
import org.eclipse.jface.viewers.ListViewer
import org.eclipse.jface.viewers.ArrayContentProvider
import org.eclipse.swt.SWT

import com.sleepcamel.fileduplicatefinder.ui.tracking.AnalyticsTracker
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources


public class DeletedFilesDialog extends Dialog {

	List deletedFiles
	List notDeletedFiles
	FDFUIResources i18n = FDFUIResources.instance
	AnalyticsTracker tracker = AnalyticsTracker.instance
	def title

	public DeletedFilesDialog(Shell parent) {
		super(parent)
		title = i18n.msg('FDFUI.deletedFilesDialogTitle')
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell)
		shell.setImages(getParentShell().getImages())
		shell.setText(title)
	}
	
	int open(){
		def lastPage = tracker.lastPage
		tracker.trackPageView("/${i18n.keyForMsg(title)}")
		def retValue = super.open()
		tracker.trackPageView(lastPage)
		retValue
	}

	protected Control createDialogArea(Composite parent) {
		def composite = super.createDialogArea(parent)
		def sash = new SashForm(composite, SWT.NONE)
		sash.setLayoutData(new GridData(GridData.FILL_BOTH))
		createListComposite(sash, i18n.msg('FDFUI.deletedFilesDialogDeletedFilesText'), true, deletedFiles)
		createListComposite(sash, i18n.msg('FDFUI.deletedFilesDialogNotDeletedFilesText'), false, notDeletedFiles)
		sash.setWeights([1, 1]as int[])
		composite
	}

	def createListComposite(sashForm, msg, isLeft, files = []){
		Composite listComposite = new Composite(sashForm, SWT.NONE)
		def gridLayout = new GridLayout(1, false)
		if ( isLeft ){
			gridLayout.marginRight = 10
		}else{
			gridLayout.marginLeft = 10
		}
		listComposite.setLayout(gridLayout)

		Label listLbl = new Label(listComposite, SWT.NONE)
		listLbl.setText(msg)
		
		ListViewer viewer = new ListViewer(listComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.VIRTUAL)
		viewer.setContentProvider(new ArrayContentProvider())
		viewer.setInput(files)
		viewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH))
	}

	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, 0, i18n.msg('FDFUI.deletedFilesDialogContinueBtn'), true)
	}

	protected boolean isResizable() {
		true
	}
}
