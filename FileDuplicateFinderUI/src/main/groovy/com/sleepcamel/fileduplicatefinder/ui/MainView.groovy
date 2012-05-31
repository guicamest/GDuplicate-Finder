package com.sleepcamel.fileduplicatefinder.ui

import java.io.File

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize

import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport
import org.eclipse.core.databinding.beans.BeanProperties
import org.eclipse.core.databinding.observable.Realm
import org.eclipse.core.internal.databinding.beans.BeanListPropertyDecorator
import org.eclipse.core.internal.databinding.beans.BeanPropertyHelper
import org.eclipse.jface.databinding.swt.SWTObservables
import org.eclipse.jface.databinding.viewers.ViewerSupport
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.viewers.CheckboxTreeViewer
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.SashForm
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.custom.StackLayout
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.FileDialog
import org.eclipse.swt.widgets.Menu
import org.eclipse.swt.widgets.MenuItem
import org.eclipse.swt.widgets.Shell

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper
import com.sleepcamel.fileduplicatefinder.core.domain.filefilters.ExtensionFilter
import com.sleepcamel.fileduplicatefinder.core.domain.filefilters.NameFilter
import com.sleepcamel.fileduplicatefinder.core.domain.filefilters.OrWrapperFilter
import com.sleepcamel.fileduplicatefinder.core.domain.filefilters.SizeFilter
import com.sleepcamel.fileduplicatefinder.ui.adapters.ClosureSelectionAdapter
import com.sleepcamel.fileduplicatefinder.ui.adapters.FileWrapperTreeLabelProvider
import com.sleepcamel.fileduplicatefinder.ui.components.ScanProgress
import com.sleepcamel.fileduplicatefinder.ui.components.ScanResults
import com.sleepcamel.fileduplicatefinder.ui.components.SizeOption
import com.sleepcamel.fileduplicatefinder.ui.components.TextFieldOption
import com.sleepcamel.fileduplicatefinder.ui.dialogs.AboutDialog
import com.sleepcamel.fileduplicatefinder.ui.dialogs.NetworkDrivesManagerDialog
import com.sleepcamel.fileduplicatefinder.ui.model.RootFileWrapper
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources
import com.sleepcamel.fileduplicatefinder.ui.utils.FileWrapperBeanListProperty
import com.sleepcamel.fileduplicatefinder.ui.utils.Settings


public class MainView {

	protected Shell shlFileDuplicateFinder

	private SizeOption minSizeOption
	private SizeOption maxSizeOption
	private TextFieldOption nameOption
	private TextFieldOption extensionsOption
	
	private SashForm sashForm
	private StackLayout stackLayout

	private ScanProgress scanProgress
	private ScanResults scanResults

	private CheckboxTreeViewer checkboxTreeViewer
	
	MenuItem mntmLoadSearchSession
	
	FDFUIResources i18n = FDFUIResources.instance
	
	def treeInput

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainView window = new MainView()
			window.open()
		} catch (Exception e) {
			e.printStackTrace()
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Settings.instance.load()
		i18n.init()
		treeInput = new RootFileWrapper(name:'')

		Display display = Display.getDefault()
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
            public void run() {
               	createContents()
            }
		})
		shlFileDuplicateFinder.open()
		shlFileDuplicateFinder.layout()
		while (!shlFileDuplicateFinder.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep()
			}
		}
//		addWindowListener
//		(new WindowAdapter() {
//		  public void windowClosing(WindowEvent e) {
//			System.exit(0)
//			}
//		  }
//		)
		Settings.instance.save()
	}
	
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlFileDuplicateFinder = new Shell()
		shlFileDuplicateFinder.setSize(800, 500)
		shlFileDuplicateFinder.setText(i18n.msg('FDFUI.appTitle'))
		stackLayout = new StackLayout()
		shlFileDuplicateFinder.setLayout(stackLayout)
		
		Menu menu = new Menu(shlFileDuplicateFinder, SWT.BAR)
		shlFileDuplicateFinder.setMenuBar(menu)
		
		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE)
		mntmNewSubmenu.setText(i18n.msg('FDFUI.fileMenu'))
		
		Menu menu_1 = new Menu(mntmNewSubmenu)
		mntmNewSubmenu.setMenu(menu_1)
		
		mntmLoadSearchSession = new MenuItem(menu_1, SWT.NONE)
		mntmLoadSearchSession.setText(i18n.msg('FDFUI.fileLoadSearchSession'))
		mntmLoadSearchSession.addSelectionListener(new ClosureSelectionAdapter(c: loadSearchSession))
		
		new MenuItem(menu_1, SWT.SEPARATOR);
		
		MenuItem mntmNewItem_2 = new MenuItem(menu_1, SWT.NONE)
		mntmNewItem_2.setText(i18n.msg('FDFUI.fileClose'))
		mntmNewItem_2.addSelectionListener(new ClosureSelectionAdapter(c: close))
		
		MenuItem mntmDrives = new MenuItem(menu, SWT.CASCADE)
		mntmDrives.setText(i18n.msg('FDFUI.drivesMenu'))
		
		Menu menu_2 = new Menu(mntmDrives)
		mntmDrives.setMenu(menu_2)
		
		MenuItem mntmMapNetworkDrive = new MenuItem(menu_2, SWT.NONE)
		mntmMapNetworkDrive.setText(i18n.msg('FDFUI.drivesManage'))
		mntmMapNetworkDrive.addSelectionListener(new ClosureSelectionAdapter(c: openDrivesMapper))
		
		MenuItem mntmNewItem = new MenuItem(menu, SWT.NONE)
		mntmNewItem.setText(i18n.msg('FDFUI.aboutMenu'))
		mntmNewItem.addSelectionListener(new ClosureSelectionAdapter(c: openAboutDialog))
		
		sashForm = new SashForm(shlFileDuplicateFinder, SWT.NONE)
		
		checkboxTreeViewer = new CheckboxTreeViewer(sashForm, SWT.BORDER)
		checkboxTreeViewer.setUseHashlookup(true)
		
		def propertyDescriptor = BeanPropertyHelper.getPropertyDescriptor(FileWrapper.class, 'dirs')
		def property = new FileWrapperBeanListProperty(propertyDescriptor, FileWrapper.class)
		def decorator = new BeanListPropertyDecorator(property, propertyDescriptor)
		
		def showMsg = !syncDrivesWithTree(true)

		ViewerSupport.bind(checkboxTreeViewer, treeInput, decorator, BeanProperties.value(FileWrapper.class, 'name'))
		
		checkboxTreeViewer.setLabelProvider(new FileWrapperTreeLabelProvider())
		
		ScrolledComposite scrolledComposite_1 = new ScrolledComposite(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL)
		scrolledComposite_1.setExpandHorizontal(true)
		scrolledComposite_1.setExpandVertical(true)
		
		Composite composite = new Composite(scrolledComposite_1, SWT.NONE)
		composite.setLayout(new GridLayout(1, false))
		
		minSizeOption = new SizeOption(composite, SWT.NONE, i18n.msg('FDFUI.minSizeFilter'))
		
		maxSizeOption = new SizeOption(composite, SWT.NONE, i18n.msg('FDFUI.maxSizeFilter'))
		
		nameOption = new TextFieldOption(composite, SWT.NONE, i18n.msg('FDFUI.fileNameFilter'))
		nameOption.setToolTipText(i18n.msg('FDFUI.fileNameFilterTooltip'))
		
		extensionsOption = new TextFieldOption(composite, SWT.NONE, i18n.msg('FDFUI.extensionFilter'))
		extensionsOption.setToolTipText(i18n.msg('FDFUI.extensionFilterTooltip'))
		
		Button btnDuplicateSearch = new Button(composite, SWT.NONE)
		btnDuplicateSearch.setText(i18n.msg('FDFUI.searchBtn'))
		btnDuplicateSearch.addSelectionListener(new ClosureSelectionAdapter(c: searchForDuplicates))

		scrolledComposite_1.setContent(composite)
		scrolledComposite_1.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT))
		sashForm.setWeights([4, 6] as int[])
		
		scanProgress = new ScanProgress(shlFileDuplicateFinder, SWT.FILL | SWT.VERTICAL | SWT.HORIZONTAL )
		scanProgress.finishedFindingDuplicates = showDuplicates
		scanProgress.cancelFindingDuplicates = searchAgain
		
		scanResults = new ScanResults(shlFileDuplicateFinder, SWT.NONE)
		scanResults.btnSearchAgain.addSelectionListener(new ClosureSelectionAdapter(c: searchAgain))
		scanResults.filesDeleted = updateDirectories

		stackLayout.topControl = sashForm
		
		if ( showMsg ){
			MessageDialog.openInformation(shlFileDuplicateFinder, i18n.msg('FDFUI.disconnectedDrivesDialogTitle'), i18n.msg('FDFUI.disconnectedDrivesDialogText'))
		}
	}
	
	def close = {
		shlFileDuplicateFinder.close()
	}

	def openAboutDialog = {
		new AboutDialog(shlFileDuplicateFinder, SWT.DIALOG_TRIM).open()
	}

	def searchForDuplicates = {
		def directories = checkboxTreeViewer.getCheckedElements()
		if ( directories.length == 0 ){
			MessageDialog.openError(shlFileDuplicateFinder, i18n.msg('FDFUI.noFolderSelectedDialogTitle'), i18n.msg('FDFUI.noFolderSelectedDialogText'))
			return
		}
		
		
		def filters = []
		Long minSize = minSizeOption.getData() as Long
		Long maxSize = maxSizeOption.getData() as Long
		if ( minSize || maxSize ){
			def sizeFilter = new SizeFilter()
			if ( minSize ) sizeFilter.minSize = minSize
			if ( maxSize ) sizeFilter.maxSize = maxSize
			filters << sizeFilter
		}
		
		if ( nameOption.getData() ){
			filters << new OrWrapperFilter( filters : nameOption.getData().collect { new NameFilter(it) } )
		}
		
		if ( extensionsOption.getData() ){
			filters << new OrWrapperFilter( filters : extensionsOption.getData().collect { new ExtensionFilter(it) } )
		}

		showScanProgress()
		scanProgress.scanAndSearch(filters, directories)
	}
	
	def loadSearchSession = {
		FileDialog dlg = new FileDialog(shlFileDuplicateFinder, SWT.OPEN);
		dlg.setFilterNames([i18n.msg('FDFUI.loadSessionDialogFilterNames')] as String []);
		dlg.setFilterExtensions([i18n.msg('FDFUI.loadSessionDialogFilterExtensions')] as String []);
		String fn = dlg.open();
		if (fn != null) {
			def progress
			new File(fn).withObjectInputStream { ios ->
				progress = ios.readObject()
				DefaultGroovyMethodsSupport.closeQuietly(ios)
			}
			showScanProgress()
			scanProgress.resumeSearch(progress)
		}
	}

	def showDuplicates = { entries ->
		scanResults.updateEntries(entries)
		stackLayout.topControl = scanResults
		shlFileDuplicateFinder.layout()
		mntmLoadSearchSession.setEnabled(true)
	}
	
	def showScanProgress(){
		stackLayout.topControl = scanProgress
		shlFileDuplicateFinder.layout()
		mntmLoadSearchSession.setEnabled(false)
	}
	
	def searchAgain = {
		stackLayout.topControl = sashForm
		shlFileDuplicateFinder.layout()
		mntmLoadSearchSession.setEnabled(true)
	}

	def openDrivesMapper = {
		def manager = new NetworkDrivesManagerDialog(shlFileDuplicateFinder, SWT.DIALOG_TRIM)
		def lastAuthModels = Settings.instance.getLastNetworkDrivesAuthModels()
		manager.drives.addAll(lastAuthModels)
		
		def newModels = manager.open()
		Settings.instance.lastNetworkAuthModels = newModels
		syncDrivesWithTree()
		checkboxTreeViewer.refresh()
	}

	def syncDrivesWithTree(umountAtErrors = false){
		def successfullSync = true
		def fileRoots = []
		File.listRoots().each { root ->
			fileRoots << new FileWrapper(root)
		}

		try{
			def networkRoots = Settings.instance.getMountedNetworkDrivesAuthModels()
			networkRoots*.checkPathExists()
			networkRoots*.folderFile()
			networkRoots*.folderFile().each { root ->
				fileRoots << new FileWrapper(root)
			}
		}catch(Exception e){
			successfullSync = false
			if ( !umountAtErrors ){
				throw new RuntimeException(e)
			}
			Settings.instance.getLastNetworkDrivesAuthModels().each { it.isMounted = false }
		}
		
		fileRoots*.isRoot = true

		treeInput.files.clear()
		treeInput.files.addAll(fileRoots)
		treeInput.dirs()
		successfullSync
	}
	
	def updateDirectories = { directories ->
		directories.unique().each { directory -> treeInput.reloadDirectoryFiles(directory) }
	}

}

