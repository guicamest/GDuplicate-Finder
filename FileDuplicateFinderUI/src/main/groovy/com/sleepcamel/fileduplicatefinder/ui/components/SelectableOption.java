package com.sleepcamel.fileduplicatefinder.ui.components;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scrollable;

public class SelectableOption extends Composite {
	protected Scrollable child;
	private Button btnCheckButton;
	private boolean enabledToUse;
	private PropertyChangeSupport support;
	
	public SelectableOption(Composite parent, int style, String optionName, Scrollable child) {
		super(parent, SWT.NONE);
		support = new PropertyChangeSupport(this);
		setLayout(new GridLayout(2, false));
		
		btnCheckButton = new Button(this, SWT.CHECK);
		btnCheckButton.setText(optionName);
		
		this.child = child;
		child.setParent(this);
		initDataBindings();
		setEnabledToUse(false);
	}

	protected void initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		IObservableValue modelEnabledValue = BeansObservables.observeValue(this, "enabledToUse");

		IObservableValue childEnabledObserveValue = SWTObservables.observeEnabled(child);
		bindingContext.bindValue(childEnabledObserveValue, modelEnabledValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);

		IObservableValue btnCheckButtonObserveSelectionObserveWidget = SWTObservables.observeSelection(btnCheckButton);
		bindingContext.bindValue(btnCheckButtonObserveSelectionObserveWidget, modelEnabledValue, null, null);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener paramPropertyChangeListener){
		support.addPropertyChangeListener(paramPropertyChangeListener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener paramPropertyChangeListener){
		support.removePropertyChangeListener(paramPropertyChangeListener);
	}
	
	public void setData(final Object data){
		setEnabledToUse(data != null);
	}

	public boolean getEnabledToUse() {
		return enabledToUse;
	}

	public void setEnabledToUse(boolean enabledToUse) {
		boolean oldValue = this.enabledToUse;
		support.firePropertyChange("enabledToUse", oldValue, this.enabledToUse = enabledToUse);
	}

}
