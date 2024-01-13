package com.sleepcamel.fileduplicatefinder.ui.components;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.sleepcamel.fileduplicatefinder.ui.adapters.TableCheckStateProvider;

public class MyCheckboxTableViewer extends CheckboxTableViewer {

	private TableCheckStateProvider checkStateProvider;
	private List<Object> filteredElements = Collections.emptyList();

	public MyCheckboxTableViewer(Composite parent, int style, IObservableList list) {
		super(new Table(parent, 0x20 | style));
		checkStateProvider = new TableCheckStateProvider(list);
		setCheckStateProvider(checkStateProvider);
		addCheckStateListener(checkStateProvider);
	}

	public boolean setChecked(Object element, boolean state) {
		boolean checked = super.setChecked(element, state);
		checkStateProvider.checkStateChanged(new CheckStateChangedEvent(this, element, state));
		return checked;
	}
	
	public void setCheckedElements(Object[] elements) {
		super.setCheckedElements(elements);
		checkStateProvider.setSelectedOnlyTheseElements(elements);
	}
	
	@SuppressWarnings("rawtypes")
	public List getAllCheckedElements(){
		return checkStateProvider.getCheckedElements();
	}

	public void notifyElementsFiltered(List<Object> asList) {
		this.filteredElements = asList;
	}
	
	public List<Object> getFilteredElements(List<Object> initialList){
		if ( filteredElements.isEmpty() ){
			return initialList;
		}
		return filteredElements;
	}
}
