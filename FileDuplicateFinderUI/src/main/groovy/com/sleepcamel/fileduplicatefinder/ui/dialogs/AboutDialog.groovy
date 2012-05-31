package com.sleepcamel.fileduplicatefinder.ui.dialogs

import java.awt.Desktop
import java.net.URI

import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Dialog
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Link
import org.eclipse.swt.widgets.Shell
import org.eclipse.wb.swt.SWTResourceManager

import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources;

class AboutDialog extends Dialog {

	Shell shlAbout
	FDFUIResources i18n = FDFUIResources.instance

	public AboutDialog(Shell parent, int style) {
		super(parent, style)
	}

	public Object open() {
		createContents()
		shlAbout.open()
		shlAbout.layout()
		Display display = getParent().getDisplay()
		while (!shlAbout.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep()
			}
		}
	}

	private void createContents() {
		shlAbout = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL)
		Rectangle parentBounds = getParent().getBounds()
		int x = ( parentBounds.x + parentBounds.width - 383 ) / 2
		int y = ( parentBounds.y + parentBounds.height - 137 ) / 2
		shlAbout.setBounds(x, y, 383, 137)
		shlAbout.setText(i18n.msg('FDFUI.aboutDialogTitle'))
		
		Label lblFileDuplicateFinder = new Label(shlAbout, SWT.NONE)
		lblFileDuplicateFinder.setFont(SWTResourceManager.getFont('Verdana', 14, SWT.NORMAL))
		lblFileDuplicateFinder.setAlignment(SWT.CENTER)
		lblFileDuplicateFinder.setBounds(62, 10, 265, 29)
		lblFileDuplicateFinder.setText(i18n.msg('FDFUI.aboutDialogAppTitle'))
		
		Link link = new Link(shlAbout, SWT.NONE)
		link.setBounds(110, 45, 165, 15)
		
		def mailto = "mailto:guillermocampelo@gmail.com?subject=${i18n.msg('FDFUI.aboutDialogMailSubject')}"
		link.setText("<a href=\"$mailto\">${i18n.msg('FDFUI.aboutDialogLinkText')}</a>")
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				Desktop desktop = Desktop.getDesktop()
				try {
					desktop.mail(new URI(mailto))
				} catch (Exception e1) {
					System.out.println(e1)
				}
			}
		})
		
		Button btnClose = new Button(shlAbout, SWT.NONE)
		btnClose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shlAbout.dispose()
			}
		})
		btnClose.setBounds(147, 74, 75, 25)
		btnClose.setText(i18n.msg('FDFUI.aboutDialogCloseBtn'))

	}
}
