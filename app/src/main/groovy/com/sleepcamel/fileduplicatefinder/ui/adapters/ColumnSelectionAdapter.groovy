package com.sleepcamel.fileduplicatefinder.ui.adapters

import groovy.lang.Singleton;

import java.util.Comparator;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper


@Singleton
class ColumnSelectionAdapter extends SelectionAdapter {
	def viewer
	def modelProperties
	def fileList
	def lastColumn
	def lastMult = 1

	void widgetSelected(SelectionEvent paramSelectionEvent) {
		def table = viewer.getTable()
        def newColumn = paramSelectionEvent.widget;
        int dir = table.getSortDirection();

		def mult = (lastColumn == newColumn) ? (lastMult * -1) : 1
		lastMult = mult

		def property = modelProperties[ArrayUtils.indexOf(table.getColumns(), newColumn)]

		Collections.sort(fileList, new Comparator<FileWrapper>() {
			int compare(FileWrapper paramT1, FileWrapper paramT2) {
				mult * paramT1[property].compareTo(paramT2[property]);
			}
		});
		lastColumn = newColumn
		viewer.refresh();
	}
}
