package com.sleepcamel.fileduplicatefinder.ui.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import swing2swt.layout.BorderLayout;

import com.sleepcamel.fileduplicatefinder.core.domain.DuplicateEntry;
import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper;
import com.sleepcamel.fileduplicatefinder.ui.adapters.TableLabelProvider;

public class OldScanResults extends Composite {
	
	public Button btnSearchAgain;
	
	public List<DuplicateEntry> entries;

	private List<FileWrapper> fileList = new ArrayList<FileWrapper>();

	private CheckboxTableViewer checkboxTableViewer;

	public OldScanResults(Composite parent, int style) {
		super(parent,  SWT.FILL);
		setLayout(new BorderLayout(10, 10));

		Composite btnComposite = new Composite(this, SWT.NONE);
		btnComposite.setLayoutData(BorderLayout.SOUTH);
		RowLayout rl_btnComposite = new RowLayout(SWT.HORIZONTAL);
		rl_btnComposite.marginBottom = 10;
		rl_btnComposite.justify = true;
		rl_btnComposite.spacing = 30;
		btnComposite.setLayout(rl_btnComposite);
		
		Button btnDeleteDuplicates = new Button(btnComposite, SWT.NONE);
		btnDeleteDuplicates.setText("Delete duplicates");
		
		Button btnSaveSession = new Button(btnComposite, SWT.NONE);
		btnSaveSession.setText("Save session");

		btnSearchAgain = new Button(btnComposite, SWT.NONE);
		btnSearchAgain.setText("Search again");
		
		SashForm sashForm = new SashForm(this, SWT.NONE);
		
		ListViewer listViewer = new ListViewer(sashForm, SWT.BORDER | SWT.V_SCROLL);
		
		checkboxTableViewer = CheckboxTableViewer.newCheckList(sashForm, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);

		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		checkboxTableViewer.setContentProvider(contentProvider);

		IObservableList observableList = Observables.staticObservableList(fileList);
		IObservableMap[] attributes = BeansObservables.observeMaps(contentProvider.getKnownElements(), FileWrapper.class, new String[] { "name", "path", "size", "md5" });
		checkboxTableViewer.setLabelProvider(new TableLabelProvider(attributes, observableList));

		checkboxTableViewer.setInput(observableList);
		
		Table table = checkboxTableViewer.getTable();
		table.setHeaderVisible(true);

		TableLayout layout = new TableLayout();
	    layout.addColumnData(new ColumnWeightData(25, 180, true));
	    layout.addColumnData(new ColumnWeightData(25, 220, true));
	    layout.addColumnData(new ColumnWeightData(1, 50, true));
	    layout.addColumnData(new ColumnWeightData(25, 220, true));
	    table.setLayout(layout);

		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText("Name");
		column1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				Collections.sort(fileList, new Comparator<FileWrapper>() {
					public int compare(FileWrapper paramT1, FileWrapper paramT2) {
						return paramT1.getName().compareTo(paramT2.getName());
					}
				});
				checkboxTableViewer.refresh();
			}
		});

	    TableColumn column2 = new TableColumn(table, SWT.NONE);
	    column2.setText("Path");
	    column2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				Collections.sort(fileList, new Comparator<FileWrapper>() {
					public int compare(FileWrapper paramT1, FileWrapper paramT2) {
						return paramT1.getPath().compareTo(paramT2.getPath());
					}
				});
				checkboxTableViewer.refresh();
			}
		});

	    TableColumn column3 = new TableColumn(table, SWT.NONE);
	    column3.setText("Size");
	    TableColumn column4 = new TableColumn(table, SWT.NONE);
	    column4.setText("Hash");
	    
		table.setLayoutData(BorderLayout.CENTER);
		
		Menu menu = new Menu(table);
		table.setMenu(menu);
		
		MenuItem mntmHola = new MenuItem(menu, SWT.NONE);
		mntmHola.setText("HOLA");
		checkboxTableViewer.refresh();
		
		Label l1 = new Label(this, SWT.NONE);
		l1.setLayoutData(BorderLayout.EAST);
		
		Label l2 = new Label(this, SWT.NONE);
		l2.setLayoutData(BorderLayout.WEST);
		
		Label l3 = new Label(this, SWT.NONE);
		l3.setLayoutData(BorderLayout.NORTH);
		
		sashForm.setWeights(new int[]{1, 3});
	}
	
	@SuppressWarnings("unchecked")
	public void updateEntries(List<DuplicateEntry> entries){
		this.entries = entries;
		fileList.clear();
		for(DuplicateEntry entry:entries){
			fileList.addAll((Collection<? extends FileWrapper>) entry.getFiles());
		}
		checkboxTableViewer.refresh();
	}
}

