package com.sleepcamel.fileduplicatefinder.ui.dialogs;


import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import java.util.List as JavaList;

import com.sleepcamel.fileduplicatefinder.core.domain.smb.SmbAuth
import com.sleepcamel.fileduplicatefinder.core.domain.vfs.NetworkAuth
import com.sleepcamel.fileduplicatefinder.ui.adapters.ClosureSelectionAdapter
import com.sleepcamel.fileduplicatefinder.ui.model.NetworkAuthModel;

public class NetworkDrivesManagerDialog extends Dialog {

	protected Object result;
	protected Shell shlDriveManager;
	def CheckboxTableViewer listViewer
	
	JavaList drives = new ArrayList();
	IObservableList input;
	
	def checked = []

	public NetworkDrivesManagerDialog(Shell parent, int style) {
		super(parent, style);
	}

	public Object open() {
		Display display = getParent().getDisplay();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				   createContents();
			}
		});
		shlDriveManager.open();
		shlDriveManager.layout();
		while (!shlDriveManager.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		result = drives
	}

	/**
	 * Create contents of the dialog.
	 */
	def createContents() {
		shlDriveManager = new Shell(getParent(), SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL);
		Rectangle parentBounds = getParent().getBounds();
		int x = ( parentBounds.x + parentBounds.width - 530 ) / 2;
		int y = ( parentBounds.y + parentBounds.height - 360 ) / 2;
		shlDriveManager.setBounds(x, y, 530, 360);
		shlDriveManager.setText("Network Drives Manager");
		
		listViewer = new CheckboxTableViewer(shlDriveManager, SWT.BORDER | SWT.V_SCROLL);
		listViewer.setContentProvider(new ObservableListContentProvider());

		input = new WritableList(drives, NetworkAuth.class);
		listViewer.setInput(input);

		def table = listViewer.getTable()
		table.setBounds(10, 10, 403, 312);
		
		Button btnAddDrive = new Button(shlDriveManager, SWT.NONE);
		btnAddDrive.setBounds(430, 95, 84, 25);
		btnAddDrive.setText("Add drive");
		btnAddDrive.addSelectionListener(new ClosureSelectionAdapter(c: openAddDialog));
		
		Button btnModify = new Button(shlDriveManager, SWT.NONE);
		btnModify.setText("Modify");
		btnModify.setBounds(430, 142, 84, 25);
		btnModify.addSelectionListener(new ClosureSelectionAdapter(c: openEditDialog));
		
		Button btnRemoveDrive = new Button(shlDriveManager, SWT.NONE);
		btnRemoveDrive.setText("Remove drive");
		btnRemoveDrive.setBounds(430, 192, 84, 25);
		btnRemoveDrive.addSelectionListener(new ClosureSelectionAdapter(c: removeNetworkDrive));
		
		Button btnDone = new Button(shlDriveManager, SWT.NONE);
		btnDone.setText("Save");
		btnDone.setBounds(430, 300, 84, 25);
		btnDone.addSelectionListener(new ClosureSelectionAdapter(c: checkConnectivityOnSelected));
		
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
			if( MessageDialog.openQuestion(shlDriveManager, "Question", "You are about to remove this network drive. Are you sure about this?") ){
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
			MessageDialog.openError(shlDriveManager, "Error", "The following drives could not be connected:\n${errors.join('\n')}\nPlease remove or uncheck them in order to continue");
			return;
		}
		drives.each {
			it['isMounted'] = checked.contains(it)
		}
		shlDriveManager.dispose()
	}
}
