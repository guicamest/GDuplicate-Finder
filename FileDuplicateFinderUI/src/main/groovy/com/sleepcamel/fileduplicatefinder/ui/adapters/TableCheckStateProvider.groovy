package com.sleepcamel.fileduplicatefinder.ui.adapters;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;

public class TableCheckStateProvider implements ICheckStateProvider, ICheckStateListener, IListChangeListener {

	def files = [:]
	
	public TableCheckStateProvider(IObservableList list) {
		list.addListChangeListener(this)
	}
	
	public void checkStateChanged(CheckStateChangedEvent event) {
		files[event.getElement()] = event.getChecked()
	}

	public boolean isChecked(Object paramObject) {
		files[paramObject]
	}

	public boolean isGrayed(Object paramObject) {
		false
	}

	public void handleListChange(ListChangeEvent event) {
		event.diff.differences.each{ ListDiffEntry diffEntry ->
			if ( diffEntry.isAddition() ){
				files[diffEntry.getElement()] = false
			}else{
				files.remove(diffEntry.getElement())
			}
		}
	}

}
