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
	
	TableCheckStateProvider(IObservableList list) {
		list.addListChangeListener(this)
	}
	
	void checkStateChanged(CheckStateChangedEvent event) {
		files[event.getElement()] = event.getChecked()
	}

	boolean isChecked(Object paramObject) {
		files[paramObject]
	}
	
	List getCheckedElements(){
		files.findAll {it.value}.collect {it.key}
	}

	boolean isGrayed(Object paramObject) {
		false
	}

	void handleListChange(ListChangeEvent event) {
		event.diff.differences.each{ ListDiffEntry diffEntry ->
			if ( diffEntry.isAddition() ){
				files[diffEntry.getElement()] = false
			}else{
				files.remove(diffEntry.getElement())
			}
		}
	}

	void setSelectedOnlyTheseElements(Object[] elements) {
		def set = elements as Set
		files.each { it.value = (set.contains(it.key)) }
	}

}
