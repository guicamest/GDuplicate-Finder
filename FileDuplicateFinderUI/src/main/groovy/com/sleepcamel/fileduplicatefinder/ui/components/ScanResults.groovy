package com.sleepcamel.fileduplicatefinder.ui.components

import java.awt.Desktop
import java.util.ArrayList
import java.util.List

import org.apache.commons.io.FileUtils
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport
import org.eclipse.core.databinding.beans.BeansObservables
import org.eclipse.core.databinding.observable.Observables
import org.eclipse.core.databinding.observable.list.IObservableList
import org.eclipse.core.databinding.observable.list.WritableList
import org.eclipse.core.databinding.observable.map.IObservableMap
import org.eclipse.jface.action.Action
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.viewers.CheckboxTableViewer
import org.eclipse.jface.viewers.ColumnWeightData
import org.eclipse.jface.viewers.ListViewer
import org.eclipse.jface.viewers.TableLayout
import org.eclipse.jface.viewers.ViewerFilter
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.SashForm
import org.eclipse.swt.layout.RowLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.FileDialog
import org.eclipse.swt.widgets.Menu
import org.eclipse.swt.widgets.MenuItem
import org.eclipse.swt.widgets.Table
import org.eclipse.swt.widgets.TableColumn

import swing2swt.layout.BorderLayout

import com.sleepcamel.fileduplicatefinder.core.domain.DuplicateEntry
import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper
import com.sleepcamel.fileduplicatefinder.core.domain.fileadapter.LocalFileAdapter
import com.sleepcamel.fileduplicatefinder.ui.adapters.ClosureSelectionAdapter
import com.sleepcamel.fileduplicatefinder.ui.adapters.ColumnSelectionAdapter
import com.sleepcamel.fileduplicatefinder.ui.adapters.ListLabelProvider
import com.sleepcamel.fileduplicatefinder.ui.adapters.TableCheckStateProvider
import com.sleepcamel.fileduplicatefinder.ui.adapters.TableLabelProvider
import com.sleepcamel.fileduplicatefinder.ui.filters.PathTableFilter
import com.sleepcamel.fileduplicatefinder.ui.listeners.MenuKeyDetectListener
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources
import com.sleepcamel.fileduplicatefinder.ui.utils.PreviewFilesCache

public class ScanResults extends Composite {
	
	public Button btnSearchAgain
	
	public List<DuplicateEntry> entries

	private List<FileWrapper> fileList = new ArrayList<FileWrapper>()
	private List<FileWrapper> folderList = new ArrayList<FileWrapper>()

	private ListViewer listViewer
	private CheckboxTableViewer checkboxTableViewer
	
	PathTableFilter tableFilter
	MenuKeyDetectListener menuKeyListener
	IObservableList observableFileList
	
	Closure filesDeleted
	
	FDFUIResources i18n = FDFUIResources.instance

	public ScanResults(Composite parent, int style) {
		super(parent,  SWT.FILL)
		setLayout(new BorderLayout(10, 10))

		Composite btnComposite = new Composite(this, SWT.NONE)
		btnComposite.setLayoutData(BorderLayout.SOUTH)
		RowLayout rl_btnComposite = new RowLayout(SWT.HORIZONTAL)
		rl_btnComposite.marginBottom = 10
		rl_btnComposite.justify = true
		rl_btnComposite.spacing = 30
		btnComposite.setLayout(rl_btnComposite)
		
		Button btnDeleteDuplicates = new Button(btnComposite, SWT.NONE)
		btnDeleteDuplicates.setText(i18n.msg('FDFUI.scanResultsDeleteBtn'))
		btnDeleteDuplicates.addSelectionListener(new ClosureSelectionAdapter(c:deleteDuplicates))
		
		Button btnSaveSession = new Button(btnComposite, SWT.NONE)
		btnSaveSession.setText(i18n.msg('FDFUI.scanResultsSaveSessionBtn'))
		btnSaveSession.addSelectionListener(new ClosureSelectionAdapter(c:saveDuplicateSession))

		btnSearchAgain = new Button(btnComposite, SWT.NONE)
		btnSearchAgain.setText(i18n.msg('FDFUI.scanResultsSearchAgainBtn'))
		
		SashForm sashForm = new SashForm(this, SWT.NONE)
		sashForm.setLayoutData(BorderLayout.CENTER)
		
		listViewer = new ListViewer(sashForm, SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL)
		listViewer.setContentProvider(new ObservableListContentProvider())
		listViewer.setLabelProvider(new ListLabelProvider())
		listViewer.setInput(Observables.staticObservableList(folderList))
		listViewer.addSelectionChangedListener(new ClosureSelectionAdapter(c: filterTable))
		
		checkboxTableViewer = CheckboxTableViewer.newCheckList(sashForm, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL)

		ObservableListContentProvider contentProvider = new ObservableListContentProvider()
		checkboxTableViewer.setContentProvider(contentProvider)

		def modelProperties = ['name', 'friendlyPath', 'size', 'md5']
		observableFileList = new WritableList(fileList, FileWrapper.class)
		IObservableMap[] attributes = BeansObservables.observeMaps(contentProvider.getKnownElements(), FileWrapper.class, modelProperties as String[])
		checkboxTableViewer.setLabelProvider(new TableLabelProvider(attributes, observableFileList))

		TableCheckStateProvider checkStateProvider = new TableCheckStateProvider(observableFileList)
		checkboxTableViewer.setCheckStateProvider(checkStateProvider)
		checkboxTableViewer.addCheckStateListener(checkStateProvider)
		checkboxTableViewer.setInput(observableFileList)

		ColumnSelectionAdapter.instance.viewer = checkboxTableViewer
		ColumnSelectionAdapter.instance.modelProperties = modelProperties
		ColumnSelectionAdapter.instance.fileList = fileList
		
		checkboxTableViewer.addDoubleClickListener(new ClosureSelectionAdapter(c: openFile))
		tableFilter = new PathTableFilter('All')
		checkboxTableViewer.setFilters([tableFilter] as ViewerFilter[])
		
		Table table = checkboxTableViewer.getTable()
		table.setHeaderVisible(true)

		menuKeyListener = new MenuKeyDetectListener()
		
		table.addKeyListener(menuKeyListener)
		table.addMenuDetectListener(menuKeyListener)
		
		Menu tableContextMenu = new Menu(table)
//		table.setMenu(tableContextMenu)

		MenuItem mntmSelectAll = new MenuItem(tableContextMenu, SWT.NONE)
		mntmSelectAll.setText(i18n.msg('FDFUI.scanResultsMenuSelectAll'))
		mntmSelectAll.addSelectionListener(new ClosureSelectionAdapter(c: selectAll))
		
		MenuItem mntmDeselectAll = new MenuItem(tableContextMenu, SWT.NONE)
		mntmDeselectAll.setText(i18n.msg('FDFUI.scanResultsMenuDeselectAll'))
		mntmDeselectAll.addSelectionListener(new ClosureSelectionAdapter(c: deselectAll))
		
		new MenuItem(tableContextMenu, SWT.SEPARATOR)

		MenuItem mntmSelectAllFromFolder = new MenuItem(tableContextMenu, SWT.NONE)
		mntmSelectAllFromFolder.setText(i18n.msg('FDFUI.scanResultsMenuSelectAllFromFolder'))
		mntmSelectAllFromFolder.addSelectionListener(new ClosureSelectionAdapter(c: selectAllFromFolder))
		
		MenuItem mntmDeselectAllFromFolder = new MenuItem(tableContextMenu, SWT.NONE)
		mntmDeselectAllFromFolder.setText(i18n.msg('FDFUI.scanResultsMenuDeselectAllFromFolder'))
		mntmDeselectAllFromFolder.addSelectionListener(new ClosureSelectionAdapter(c: deselectAllFromFolder))
		
		TableLayout layout = new TableLayout()
	    layout.addColumnData(new ColumnWeightData(25, 180, true))
	    layout.addColumnData(new ColumnWeightData(25, 190, true))
	    layout.addColumnData(new ColumnWeightData(1, 80, true))
	    layout.addColumnData(new ColumnWeightData(25, 220, true))
	    table.setLayout(layout)

		createColumn(checkboxTableViewer, i18n.msg('FDFUI.scanResultsTableHeaderName'))
		createColumn(checkboxTableViewer, i18n.msg('FDFUI.scanResultsTableHeaderPath'))
		createColumn(checkboxTableViewer, i18n.msg('FDFUI.scanResultsTableHeaderSize'))
		createColumn(checkboxTableViewer, i18n.msg('FDFUI.scanResultsTableHeaderHash'))
		
		sashForm.setWeights([2, 5] as int[])
		checkboxTableViewer.refresh()
	}
	
	def createColumn(viewer, columnName){
		def column = new TableColumn(viewer.getTable(), SWT.NONE)
		column.setText(columnName)
		column.addSelectionListener(ColumnSelectionAdapter.instance)
	}
	
	def updateEntries(List<DuplicateEntry> entries){
		this.entries = entries
		refresh()
	}
	
	def refresh(){
		observableFileList.clear()
		folderList.clear()
		entries*.getFiles()*.each{ observableFileList.add(it) }

		folderList.addAll(observableFileList.groupBy { file -> file.getParentWrapper().getFriendlyPath() }
						   .collect { entry -> [ filePath : entry.key , count : entry.value.size()] })
		folderList.sort(true) { a, b ->
			a.count.compareTo(b.count) * -1
		}

		def firstElement = [ filePath : 'All' , count : observableFileList.size()]
		folderList.add(0, firstElement)
		
		tableFilter.filterUsingPath('All')
		listViewer.refresh()
	}
	
	def filterTable = { selectedEvent ->
		if ( !selectedEvent.getSelection().isEmpty() && tableFilter.filterUsingPath(selectedEvent.getSelection().getFirstElement().filePath) ){
			checkboxTableViewer.refresh()
		}
	}

	def selectAllFromFolder = { allFromFolderToState(true) }
	def deselectAllFromFolder = { allFromFolderToState(false) }
	def allFromFolderToState = { state ->
		keepIfCtrlDown(state) {
			def currentItem = checkboxTableViewer.getSelection().getFirstElement()
			if ( !currentItem ) return null
			def father = currentItem.getParentWrapper()
			fileList.findAll { file -> file.getParentWrapper().equals(father) }
		}
	}
	
	def selectAll = { selectionToState(true) }
	def deselectAll = { selectionToState(false) }
	def selectionToState = { state ->
		keepIfCtrlDown(state) {
			checkboxTableViewer.getSelection().toArray()
		}
	}

	def keepIfCtrlDown(boolean toSelected, Closure c){
		def keepSelected = menuKeyListener.ctrlStatus
		def items = c.call()
		if ( items == null )
			return

		if ( keepSelected ){
			items.each{
				checkboxTableViewer.setChecked(it, toSelected)
			}
		}else{
			items = ( toSelected ? items : [] ) as Object[]
			checkboxTableViewer.setCheckedElements(items)
		}
	}
	
	def saveDuplicateSession = {
		FileDialog dlg = new FileDialog(shell, SWT.SAVE)
		dlg.setFilterNames([i18n.msg('FDFUI.loadDuplicateSessionDialogFilterNames')] as String [])
		dlg.setFilterExtensions([i18n.msg('FDFUI.loadDuplicateSessionDialogFilterExtensions')] as String [])
		String fn = dlg.open()
		if (fn != null) {
			new File(fn).withObjectOutputStream { oos ->
				oos.writeObject(entries)
				DefaultGroovyMethodsSupport.closeQuietly(oos)
			}
		}
	}
	
	def deleteDuplicates = {
		if ( groupIsAllSelected() ){
			if ( !MessageDialog.openConfirm(null, i18n.msg('FDFUI.scanResultsAllInGroupDialogTitle'), i18n.msg('FDFUI.scanResultsAllInGroupDialogText')) )
				return
		}
		def asList = checkboxTableViewer.getCheckedElements() as List
		if ( asList.isEmpty() ){
			MessageDialog.openInformation(null, i18n.msg('FDFUI.scanResultsNoFileDialogTitle'), i18n.msg('FDFUI.scanResultsNoFileDialogText'))
			return
		}
		def selectedFiles = asList.groupBy {it.getMd5()}

		entries.each { entry ->
			if ( entry.hash in selectedFiles.keySet()){
				selectedFiles[entry.hash].each { fileToDelete ->
					if ( fileToDelete.delete() ){
						entry.files.remove(fileToDelete)
					}
				}
			}
		}

		// Call close to refresh deleted file's parents
		filesDeleted(asList.collect{it.getParentWrapper()})
		entries.removeAll{ entry ->
			!entry.hasDuplicates()
		}
		refresh()
	}
	
	def groupIsAllSelected(){
		def allSelected = false
		for(DuplicateEntry entry:entries){
			allSelected = true
			entry.getFiles().each { allSelected = allSelected && checkboxTableViewer.getChecked(it) }
			if ( allSelected ){
				break
			}
		}
		allSelected
	}
	
	def openFile = { event ->
		def selectedFile = event.selection.getFirstElement()
		if ( selectedFile && selectedFile.adapterToUse ){
			def file
			switch (selectedFile.adapterToUse.class){
				case LocalFileAdapter:
					file = selectedFile.file
					break
				default:
					file = PreviewFilesCache.instance.get(selectedFile.md5())
					if( !file && MessageDialog.openQuestion(null, i18n.msg('FDFUI.scanResultsPreviewFileTitle'), i18n.msg('FDFUI.scanResultsPreviewFileText')) ){
						file = File.createTempFile('dff', selectedFile.name)
						FileUtils.copyInputStreamToFile(selectedFile.inputStream(), file)
						PreviewFilesCache.instance.put(selectedFile.md5(), file)
					}
					break
			}
			if ( file ){
				Desktop.getDesktop().open(file)
			}
		}
	}
}

class ContextMenuAction extends Action {
	protected ContextMenuAction(String text, int style){
		super(text,style)
	}
}
