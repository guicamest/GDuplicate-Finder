package com.sleepcamel.fileduplicatefinder.ui.dialogs


import org.apache.commons.beanutils.BeanUtils
import org.eclipse.core.databinding.observable.Realm
import org.eclipse.core.databinding.observable.list.IObservableList
import org.eclipse.core.databinding.observable.list.WritableList
import org.eclipse.jface.databinding.swt.SWTObservables
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider
import org.eclipse.jface.viewers.CheckboxTableViewer
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell

import java.util.List as JavaList

import com.sleepcamel.fileduplicatefinder.core.domain.vfs.NetworkAuth
import com.sleepcamel.fileduplicatefinder.ui.adapters.ClosureSelectionAdapter
import com.sleepcamel.fileduplicatefinder.ui.model.NetworkAuthModel
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources

public class NetworkDrivesManagerDialog extends InternationalizedDialog {

	protected Object result
	protected Shell shlDriveManager
	def CheckboxTableViewer listViewer
	
	JavaList drives = new ArrayList()
	IObservableList input
	
	def checked = []
	
	public NetworkDrivesManagerDialog(Shell parent, int style) {
		super(parent, style, 'FDFUI.networkDriveManagerDialogTitle')
	}

	public Object doOpen() {
		Display display = getParent().getDisplay()
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				   createContents()
			}
		})
		shlDriveManager.open()
		shlDriveManager.layout()
		while (!shlDriveManager.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep()
			}
		}
		result = drives
	}

	/**
	 * Create contents of the dialog.
	 */
	def createContents() {
		shlDriveManager = new Shell(getParent(), SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL)
		Rectangle parentBounds = getParent().getBounds()
		int x = ( parentBounds.x + parentBounds.width - 530 ) / 2
		int y = ( parentBounds.y + parentBounds.height - 360 ) / 2
		shlDriveManager.setBounds(x, y, 530, 360)
		shlDriveManager.setText(title)
		
		def layout = new GridLayout(2, false)
		layout.marginLeft = layout.marginRight = layout.marginTop = layout.marginBottom = layout.horizontalSpacing = 10
		shlDriveManager.setLayout(layout)
		
		listViewer = new CheckboxTableViewer(shlDriveManager, SWT.BORDER | SWT.V_SCROLL)
		listViewer.setContentProvider(new ObservableListContentProvider())

		input = new WritableList(drives, NetworkAuth.class)
		listViewer.setInput(input)

		def table = listViewer.getTable()
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
		
		def composite = new Composite(shlDriveManager, SWT.NONE)
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true))
		layout = new GridLayout(1, false)
		layout.verticalSpacing = 25
		composite.setLayout(layout)
		
		Button btnAddDrive = new Button(composite, SWT.NONE)
		btnAddDrive.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false))
		btnAddDrive.setText(i18n.msg('FDFUI.networkDriveManagerDialogAddBtn'))
		btnAddDrive.addSelectionListener(new ClosureSelectionAdapter(c: openAddDialog))
		
		Button btnModify = new Button(composite, SWT.NONE)
		btnModify.setText(i18n.msg('FDFUI.networkDriveManagerDialogModifyBtn'))
		btnModify.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false))
		btnModify.addSelectionListener(new ClosureSelectionAdapter(c: openEditDialog))
		
		Button btnRemoveDrive = new Button(composite, SWT.NONE)
		btnRemoveDrive.setText(i18n.msg('FDFUI.networkDriveManagerDialogRemoveBtn'))
		btnRemoveDrive.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false))
		btnRemoveDrive.addSelectionListener(new ClosureSelectionAdapter(c: removeNetworkDrive))
		
		Button btnDone = new Button(composite, SWT.NONE)
		btnDone.setText(i18n.msg('FDFUI.networkDriveManagerDialogSaveBtn'))
		btnDone.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false))
		btnDone.addSelectionListener(new ClosureSelectionAdapter(c: checkConnectivityOnSelected))
		
		drives.each {
			listViewer.setChecked(it, it['isMounted'])
		}
	}
	
	def openAddDialog = {
		def newModel = new NetworkAuthModel( auth : openDialogWithModel(new NetworkAuth()) )
		if ( newModel.auth && !input.contains(newModel.auth) ){
			input.add(newModel)
			listViewer.setChecked(newModel, true)
			listViewer.refresh()
		}
	}
	
	def openEditDialog = {
		StructuredSelection selection = listViewer.getSelection()
		if ( !selection.isEmpty() ){
			def model = selection.getFirstElement()
			def modelcopy = new NetworkAuth()
			BeanUtils.copyProperties(modelcopy, model.auth)
			def newModel = openDialogWithModel(modelcopy)
			if ( newModel ){
				BeanUtils.copyProperties(model.auth, modelcopy)
				listViewer.refresh()
			}
		}
	}
	
	def openDialogWithModel = { model ->
		def dialog = new NetworkDriveDialog(shlDriveManager, SWT.DIALOG_TRIM)
		dialog.model = model
		dialog.open()
	}
	
	def removeNetworkDrive = {
		StructuredSelection selection = listViewer.getSelection()
		if ( !selection.isEmpty() ){
			if( InternationalizedTrackingMessageDialogHelper.openQuestion(shlDriveManager, i18n.msg('FDFUI.networkDriveManagerDialogConfirmRemoveTitle'), i18n.msg('FDFUI.networkDriveManagerDialogConfirmRemoveText')) ){
				input.remove(selection.getFirstElement())
				listViewer.refresh()
			}
		}
	}
	
	def checkConnectivityOnSelected = {
		def errors = []
		def checked = listViewer.getCheckedElements() as JavaList
		checked.each{
			try{
				it.auth.checkPathExists()
			}catch(Exception e){
				errors << it.auth.toString()
			}
		}
		
		if ( errors.size() != 0 ){
			InternationalizedTrackingMessageDialogHelper.openError(shlDriveManager, i18n.msg('FDFUI.networkDriveManagerDialogErrorTitle'), i18n.msg('FDFUI.networkDriveManagerDialogErrorText', errors.join('\n')))
			return
		}
		drives.each {
			it['isMounted'] = checked.contains(it)
		}
		shlDriveManager.dispose()
	}
}
