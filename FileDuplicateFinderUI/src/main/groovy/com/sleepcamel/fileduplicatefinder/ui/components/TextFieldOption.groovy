package com.sleepcamel.fileduplicatefinder.ui.components

import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Text

public class TextFieldOption extends SelectableOption {

	public TextFieldOption(Composite parent, int style, String name) {
		super(parent, SWT.BORDER, name, new Text(parent, SWT.BORDER) )
		child.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false))
	}

	def getData() {
		if ( !child.enabled )
			return null
		
		String text = ((Text)child).getText().trim()
		if ( text.isEmpty() )
			return null

		text.split(',').collect { it.trim() }
	}
}
