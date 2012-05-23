package com.sleepcamel.fileduplicatefinder.ui.adapters;

import groovy.lang.Closure;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

class ClosureSelectionAdapter extends SelectionAdapter implements IDoubleClickListener, ICheckStateListener, ISelectionChangedListener {
	protected Closure c;
	public void widgetSelected(SelectionEvent paramSelectionEvent) {
		c.call(paramSelectionEvent)
	}
	
	public void doubleClick(DoubleClickEvent paramDoubleClickEvent){
		c.call(paramDoubleClickEvent)
	}

	public void checkStateChanged(CheckStateChangedEvent arg0) {
		c.call(arg0)
	}

	public void selectionChanged(SelectionChangedEvent paramSelectionChangedEvent) {
		c.call(paramSelectionChangedEvent)
	}
}