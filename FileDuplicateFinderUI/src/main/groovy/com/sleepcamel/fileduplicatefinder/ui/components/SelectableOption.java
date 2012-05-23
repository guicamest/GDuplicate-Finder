package com.sleepcamel.fileduplicatefinder.ui.components;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scrollable;

public class SelectableOption extends Composite {
	protected Scrollable child;
	private Button btnCheckButton;
	
	public SelectableOption(Composite parent, int style, String optionName, Scrollable child) {
		super(parent, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.fill = true;
		rowLayout.center = true;
		setLayout(rowLayout);
		
		btnCheckButton = new Button(this, SWT.CHECK);
		btnCheckButton.setText(optionName);
		btnCheckButton.setSelection(false);
		
		this.child = child;
		child.setParent(this);
		initDataBindings();
	}

	protected void initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		IObservableValue childEnabledObserveValue = SWTObservables.observeEnabled(child);
		IObservableValue btnCheckButtonObserveSelectionObserveWidget = SWTObservables.observeSelection(btnCheckButton);
		bindingContext.bindValue(childEnabledObserveValue, btnCheckButtonObserveSelectionObserveWidget, null, null);
	}
}
