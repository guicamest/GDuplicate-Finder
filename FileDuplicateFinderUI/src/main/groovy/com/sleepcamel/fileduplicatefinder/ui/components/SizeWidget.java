package com.sleepcamel.fileduplicatefinder.ui.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

public class SizeWidget extends Composite {

	private Combo combo;
	private Spinner spinner;

	public SizeWidget(Composite parent, int style) {
		super(parent, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.justify = true;
		rowLayout.fill = true;
		rowLayout.center = true;
		setLayout(rowLayout);
		
		spinner = new Spinner(this, SWT.BORDER);
		spinner.setMaximum(Integer.MAX_VALUE-1);
		spinner.setLayoutData(new RowData(50, SWT.DEFAULT));
		
		combo = new Combo(this, SWT.NONE);
		combo.add("bytes");
		combo.add("Kbs");
		combo.add("Mbs");
		combo.add("Gbs");
		combo.select(0);
		combo.setLayoutData(new RowData(31, SWT.DEFAULT));
	}

	public Object getData(){
		return spinner.getSelection() * Math.pow(1024, combo.getSelectionIndex());
	}
}
