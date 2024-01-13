package com.sleepcamel.fileduplicatefinder.ui.dialogs


import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout
import org.eclipse.swt.program.Program
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Link
import org.eclipse.swt.widgets.Shell

import com.sleepcamel.fileduplicatefinder.ui.model.UpdateStatus
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources;

class UpdateDialog extends InternationalizedDialog {

	Shell shlUpdate
	static final Integer DIALOG_WIDTH = 400
	static final Integer DIALOG_HEIGHT = 120
	def downloadUrl;
	def version;
	UpdateStatus status;
	
	public UpdateDialog(UpdateStatus s, String version, String downloadUrl) {
		super(new Shell(Display.getCurrent()), SWT.NONE, 'FDFUI.updateDialogTitle')
		this.status = s
		this.version = version
		this.downloadUrl = downloadUrl
	}

	public Object doOpen() {
		createContents()
		shlUpdate.open()
		shlUpdate.layout()
		Display display = getParent().getDisplay()
		while (!shlUpdate.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep()
			}
		}
	}

	private void createContents() {
		shlUpdate = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL)
		Rectangle parentBounds = getParent().getBounds()
		int x = ( parentBounds.x + parentBounds.width - DIALOG_WIDTH ) / 2
		int y = ( parentBounds.y + parentBounds.height - DIALOG_HEIGHT ) / 2
		shlUpdate.setBounds(x, y, DIALOG_WIDTH, DIALOG_HEIGHT)
		shlUpdate.setText(title)
		shlUpdate.setLayout(new GridLayout(1, false))
		
		Composite composite = new Composite(shlUpdate, SWT.NONE)
		GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1)
		gd_composite.widthHint = DIALOG_WIDTH - 10
		composite.setLayoutData(gd_composite)
		composite.setLayout(new GridLayout(1, false))
		
		Label label = new Label(composite, SWT.CENTER)
		label.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, true, false, 1, 8))
		label.setText(status.getFriendlyName())
		
		if ( status == UpdateStatus.NEW_AVAILABLE ){
			Link link = new Link(composite, SWT.NONE)
			link.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, true, false, 1, 8))
			link.setText("<a href=\"$downloadUrl\">GDuplicateFinder v${version}</a>")
			
			link.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e){
					try {
						Program.launch(downloadUrl.toString())
						shlUpdate.dispose()
					} catch (Exception e1) {
					}
				}
			})
			
		}
		
		Composite btnComposite = new Composite(shlUpdate, SWT.NONE)
		btnComposite.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false, 1, 1))
		btnComposite.setLayout(new RowLayout(SWT.HORIZONTAL))
		
		Button btnClose = new Button(btnComposite, SWT.NONE)
		btnClose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shlUpdate.dispose()
			}
		})
		btnClose.setText(i18n.msg('FDFUI.updateDialogCloseBtn'))
		shlUpdate.pack()
	}
}
