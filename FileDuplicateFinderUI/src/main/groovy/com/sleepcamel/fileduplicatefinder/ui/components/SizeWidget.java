package com.sleepcamel.fileduplicatefinder.ui.components;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import com.sleepcamel.fileduplicatefinder.ui.utils.FileSize;

public class SizeWidget extends Composite {

	private ComboViewer combo;
	private Spinner spinner;

	public SizeWidget(Composite parent, int style) {
		super(parent, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.justify = true;
		rowLayout.fill = true;
		rowLayout.center = true;
		setLayout(rowLayout);

		spinner = new Spinner(this, SWT.BORDER);
		spinner.setMaximum(Integer.MAX_VALUE - 1);
		spinner.setLayoutData(new RowData(50, SWT.DEFAULT));

		combo = new ComboViewer(this, SWT.BORDER | SWT.READ_ONLY);
		combo.setContentProvider(ArrayContentProvider.getInstance());
		combo.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				FileSize f = (FileSize) element;
				return ((element == null) ? "" : f.getFriendlyName());
			}
		});
		combo.setInput(FileSize.values());
		combo.getCombo().select(0);
	}

	public Object getData() {
		StructuredSelection selection = (StructuredSelection) combo.getSelection();
		FileSize selectedElement = (FileSize) selection.getFirstElement();
		return selectedElement.toBytes(spinner.getSelection());
	}
}
