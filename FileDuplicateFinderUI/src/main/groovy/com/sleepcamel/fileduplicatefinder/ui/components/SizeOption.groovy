package com.sleepcamel.fileduplicatefinder.ui.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class SizeOption extends SelectableOption {

	public SizeOption(Composite parent, int style, String name) {
		super(parent, SWT.NONE, name, new SizeWidget(parent, style));
	}

	def getData() {
		child.enabled ? ((SizeWidget)child).getData() : null
	}
}
