package com.sleepcamel.fileduplicatefinder.ui.dialogs


import org.eclipse.core.databinding.DataBindingContext
import org.eclipse.core.databinding.UpdateValueStrategy
import org.eclipse.core.databinding.beans.BeansObservables
import org.eclipse.core.databinding.observable.Realm
import org.eclipse.core.databinding.observable.value.IObservableValue
import org.eclipse.jface.databinding.swt.SWTObservables
import org.eclipse.jface.databinding.swt.WidgetProperties
import org.eclipse.jface.viewers.ArrayContentProvider
import org.eclipse.jface.viewers.ComboViewer
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.jface.viewers.SelectionChangedEvent
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.swt.widgets.Dialog
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.layout.RowData
import org.eclipse.swt.widgets.Text
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Button
import org.eclipse.wb.swt.SWTResourceManager
import org.eclipse.swt.layout.RowLayout

import com.sleepcamel.fileduplicatefinder.core.domain.vfs.NetworkAuth
import com.sleepcamel.fileduplicatefinder.ui.adapters.ClosureSelectionAdapter
import com.sleepcamel.fileduplicatefinder.ui.adapters.NegativeUpdateValueStrategy
import com.sleepcamel.fileduplicatefinder.ui.model.Protocol
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources
import com.sleepcamel.fileduplicatefinder.ui.utils.FileSize

public class NetworkDriveDialog extends Dialog {

	protected Object result
	protected Shell shell

	private Button btnCheckButton

	private ComboViewer cmbProtocol
	private Text txtPort
	private Text txtAddress
	private Text txtUsername
	private Text txtPassword
	private Text txtPathToFolderShare

	Label lblError
	NetworkAuth model = new NetworkAuth()
	
	FDFUIResources i18n = FDFUIResources.instance

	public NetworkDriveDialog(Shell parent, int style) {
		super(parent, style)
		setText(i18n.msg('FDFUI.networkDriveDialogTitle'))
	}

	public Object open() {
		Display display = getParent().getDisplay()
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				createContents()
			}
		})
		shell.open()
		shell.layout()
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep()
			}
		}
		return result
	}

	def createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL)
		shell.setSize(450, SWT.DEFAULT)
		shell.setText(getText())
		shell.setLayout(new GridLayout(1, false))
		
		Composite composite = new Composite(shell, SWT.NONE)
		GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1)
		gd_composite.widthHint = 434
		composite.setLayoutData(gd_composite)
		composite.setLayout(new GridLayout(2, false))
		
		Label lblProtocol = new Label(composite, SWT.NONE)
		lblProtocol.setSize(42, 15)
		lblProtocol.setText(i18n.msg('FDFUI.networkDriveDialogProtocolLbl'))
		
		cmbProtocol = new ComboViewer(composite, SWT.BORDER | SWT.READ_ONLY)
		cmbProtocol.getCombo().setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1))
		cmbProtocol.setContentProvider(ArrayContentProvider.getInstance())
		cmbProtocol.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				Protocol f = (Protocol) element
				return ((element == null) ? '' : f.getFriendlyName())
			}
		})
		cmbProtocol.setInput(Protocol.values())
		cmbProtocol.addSelectionChangedListener(new ClosureSelectionAdapter(c: updatePort))
		
		Label lblPort = new Label(composite, SWT.NONE)
		lblPort.setSize(42, 15)
		lblPort.setText(i18n.msg('FDFUI.networkDriveDialogPortLbl'))
		
		txtPort = new Text(composite, SWT.BORDER)
		txtPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))
		txtPort.setSize(50, 21)
		txtPort.setMessage(i18n.msg('FDFUI.networkDriveDialogPortTooltip'))
		
		Label lblAddress = new Label(composite, SWT.NONE)
		lblAddress.setSize(42, 15)
		lblAddress.setText(i18n.msg('FDFUI.networkDriveDialogHostLbl'))
		
		txtAddress = new Text(composite, SWT.BORDER)
		txtAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1))
		txtAddress.setSize(364, 21)
		txtAddress.setMessage(i18n.msg('FDFUI.networkDriveDialogHostTooltip'))
		
		btnCheckButton = new Button(composite, SWT.CHECK)
		btnCheckButton.setText(i18n.msg('FDFUI.networkDriveDialogGuestLbl'))
		btnCheckButton.setSelection(false)
		new Label(composite, SWT.NONE)
		
		Label lblUsername = new Label(composite, SWT.NONE)
		lblUsername.setText(i18n.msg('FDFUI.networkDriveDialogUsernameLbl'))
		
		txtUsername = new Text(composite, SWT.BORDER)
		txtUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1))
		txtUsername.setMessage(i18n.msg('FDFUI.networkDriveDialogUsernameTooltip'))
		
		Label lblPassword = new Label(composite, SWT.NONE)
		lblPassword.setText(i18n.msg('FDFUI.networkDriveDialogPasswordLbl'))
		
		txtPassword = new Text(composite, SWT.BORDER | SWT.PASSWORD)
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))
		txtPassword.setMessage(i18n.msg('FDFUI.networkDriveDialogPasswordTooltip'))
		
		Label lblFolderShare = new Label(composite, SWT.NONE)
		lblFolderShare.setText(i18n.msg('FDFUI.networkDriveDialogFolderShareLbl'))
		
		txtPathToFolderShare = new Text(composite, SWT.BORDER)
		txtPathToFolderShare.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1))
		txtPathToFolderShare.setMessage(i18n.msg('FDFUI.networkDriveDialogFolderShareTooltip'))
		
		Composite composite_1 = new Composite(shell, SWT.NONE)
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1))
		composite_1.setLayout(new GridLayout(1, false))
		
		lblError = new Label(composite_1, SWT.NONE)
		lblError.setFont(SWTResourceManager.getFont('Verdana', 10, SWT.NORMAL))
		lblError.setAlignment(SWT.CENTER)
		lblError.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1))
		lblError.setText('')

		Composite btnComposite = new Composite(composite_1, SWT.NONE)
		btnComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1))
		RowLayout rl_btnComposite = new RowLayout(SWT.HORIZONTAL)
		rl_btnComposite.spacing = 30
		btnComposite.setLayout(rl_btnComposite)
		
		Button btnSave = new Button(btnComposite, SWT.NONE)
		btnSave.setText(i18n.msg('FDFUI.networkDriveDialogSaveBtn'))
		btnSave.addSelectionListener(new ClosureSelectionAdapter(c: save))
		
		Button btnConnectivity = new Button(btnComposite, SWT.NONE)
		btnConnectivity.setText(i18n.msg('FDFUI.networkDriveDialogCheckBtn'))
		btnConnectivity.addSelectionListener(new ClosureSelectionAdapter(c: checkConnectivity))
		
		Button btnCancel = new Button(btnComposite, SWT.NONE)
		btnCancel.setText(i18n.msg('FDFUI.networkDriveDialogCancelBtn'))
		btnCancel.addSelectionListener(new ClosureSelectionAdapter(c: {
			shell.dispose()
		}))
	
		cmbProtocol.setSelection(new StructuredSelection(Protocol.SFTP))
		createBindings()
		shell.pack()
	}
	
	def createBindings(){
		def bindingContext = new DataBindingContext()
		
		IObservableValue modelAddressValue = BeansObservables.observeValue(model, 'hostname')
		IObservableValue widgetAddressValue = WidgetProperties.text(SWT.Modify).observe(txtAddress)
		bindingContext.bindValue(widgetAddressValue, modelAddressValue)
		
		IObservableValue modelUsernameValue = BeansObservables.observeValue(model, 'username')
		IObservableValue widgetUsernameValue = WidgetProperties.text(SWT.Modify).observe(txtUsername)
		bindingContext.bindValue(widgetUsernameValue, modelUsernameValue)
		
		IObservableValue modelPasswordValue = BeansObservables.observeValue(model, 'password')
		IObservableValue widgetPasswordValue = WidgetProperties.text(SWT.Modify).observe(txtPassword)
		bindingContext.bindValue(widgetPasswordValue, modelPasswordValue)
		
		IObservableValue modelFolderValue = BeansObservables.observeValue(model, 'folder')
		IObservableValue widgetFolderValue = WidgetProperties.text(SWT.Modify).observe(txtPathToFolderShare)
		bindingContext.bindValue(widgetFolderValue, modelFolderValue)
		
		IObservableValue modelPortValue = BeansObservables.observeValue(model, 'port')
		IObservableValue widgetPortValue = WidgetProperties.text(SWT.Modify).observe(txtPort)
		bindingContext.bindValue(widgetPortValue, modelPortValue)
		
		IObservableValue modelProtocolValue = BeansObservables.observeValue(model, 'protocol')
		IObservableValue widgetProtocolValue = WidgetProperties.selection().observe(cmbProtocol.getCombo())
		bindingContext.bindValue(widgetProtocolValue, modelProtocolValue)

		UpdateValueStrategy strategy = new NegativeUpdateValueStrategy()

		IObservableValue btnCheckButtonObserveSelectionObserveWidget = SWTObservables.observeSelection(btnCheckButton)
		bindingContext.bindValue(SWTObservables.observeEnabled(txtUsername), btnCheckButtonObserveSelectionObserveWidget, strategy, strategy)
		bindingContext.bindValue(SWTObservables.observeEnabled(txtPassword), btnCheckButtonObserveSelectionObserveWidget, strategy, strategy)
	}
	
	def updatePort = { SelectionChangedEvent selectionEvent ->
		txtPort.text = selectionEvent.selection.getFirstElement().defaultPort
	}

	def checkConnectivity = {
		updateModel()

		lblError.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN))
		lblError.text = i18n.msg('FDFUI.networkDriveDialogStatusConnecting')
		try{
			model.checkPathExists()
			lblError.text = i18n.msg('FDFUI.networkDriveDialogStatusConnected')
		}catch(Exception e){
			lblError.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED))
			lblError.text = e.message
		}
	}
	
	def updateModel = {
		if ( btnCheckButton.getSelection() ){
			model.username = 'GUEST'
			model.password = ''
		}
	}
	
	def save = {
		updateModel()
		result = model
		shell.dispose()
	}
}
