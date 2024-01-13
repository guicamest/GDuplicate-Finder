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

import com.sleepcamel.fileduplicatefinder.ui.tracking.AnalyticsTracker;
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources;

class ClosureSelectionAdapter extends SelectionAdapter implements IDoubleClickListener, ICheckStateListener, ISelectionChangedListener {
	protected Closure c;
	public void widgetSelected(SelectionEvent paramSelectionEvent) {
		if ( paramSelectionEvent.widget.hasProperty('text') ){
			AnalyticsTracker.instance.trackEvent('Item Selected', FDFUIResources.instance.keyForMsg(paramSelectionEvent.widget.text),'')
		}
		c.call(paramSelectionEvent)
	}
	
	public void doubleClick(DoubleClickEvent paramDoubleClickEvent){
		if ( paramDoubleClickEvent.source ){
			AnalyticsTracker.instance.trackEvent('Double click', paramDoubleClickEvent.source.getClass().getSimpleName(),'')
		}
		c.call(paramDoubleClickEvent)
	}

	public void checkStateChanged(CheckStateChangedEvent checkStateEvent) {
		if ( checkStateEvent.source ){
			AnalyticsTracker.instance.trackEvent('Check changed', checkStateEvent.source.getClass().getSimpleName(),'')
		}
		c.call(checkStateEvent)
	}

	public void selectionChanged(SelectionChangedEvent paramSelectionChangedEvent) {
		if ( paramSelectionChangedEvent.source ){
			AnalyticsTracker.instance.trackEvent('Selection Changed', paramSelectionChangedEvent.source.getClass().getSimpleName(),'')
		}
		c.call(paramSelectionChangedEvent)
	}
}