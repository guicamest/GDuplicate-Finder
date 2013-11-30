package com.sleepcamel.fileduplicatefinder.ui

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize
import groovy.util.logging.Slf4j

import java.awt.Desktop

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.time.StopWatch
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
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.GridData
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
import com.sleepcamel.fileduplicatefinder.ui.dialogs.FilesNotFoundDialog
import com.sleepcamel.fileduplicatefinder.ui.dialogs.InternationalizedTrackingMessageDialogHelper
import com.sleepcamel.fileduplicatefinder.ui.dialogs.NetworkDrivesManagerDialog
import com.sleepcamel.fileduplicatefinder.ui.dialogs.preference.GDFPreferenceDialog
import com.sleepcamel.fileduplicatefinder.ui.model.RootFileWrapper
import com.sleepcamel.fileduplicatefinder.ui.model.SearchParameters
import com.sleepcamel.fileduplicatefinder.ui.tracking.AnalyticsTracker
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources
import com.sleepcamel.fileduplicatefinder.ui.utils.FileWrapperBeanListProperty
import com.sleepcamel.fileduplicatefinder.ui.utils.OSInfo
import com.sleepcamel.fileduplicatefinder.ui.utils.Settings
import com.sleepcamel.fileduplicatefinder.ui.utils.Utils
import com.sleepcamel.fileduplicatefinder.ui.utils.associations.FileAssociations
import com.sleepcamel.fileduplicatefinder.ui.utils.associations.FileHandler

@Slf4j
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
	
	MenuItem mntmNewSearch
	MenuItem mntmLoadSearchParameters
	MenuItem mntmLoadSearchSession
	MenuItem mntmLoadDuplicateResultsSession
	MenuItem mntmPreferences
	
	FDFUIResources i18n = FDFUIResources.instance
	AnalyticsTracker tracker = AnalyticsTracker.instance
	
	def treeInput
	def syncedDrives = false

	public static void main(String[] args) {
		try {
			new MainView().open(args)
		} catch (Exception e) {
			println "${e.class}"
			e.printStackTrace()
		}
	}
	
	MainView(){
		FileAssociations.instance.registerHandler('drs', loadDuplicateResultsSessionFile)
		FileAssociations.instance.registerHandler('sps', loadSearchSessionFile)
		FileAssociations.instance.registerHandler('sp', loadSearchParametersFile)
	}

	public void open(String[] args) {
		log.info(i18n.msg('FDFUI.buildNotice',OSInfo.autodetectOS()))

		treeInput = new RootFileWrapper(name:'')

		Display display = Display.getDefault()
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
            public void run() {
               	createContents()
            }
		})
		def iconsFiles = [16, 24, 32, 48, 64, 128, 256, 512].collect { "logo_${it}x${it}.png" }
		def iconsImages = FDFUIResources.instance.getIcons(iconsFiles) as Image[]
		shlFileDuplicateFinder.setImages(iconsImages)
		shlFileDuplicateFinder.open()
		shlFileDuplicateFinder.layout()

		def handled = false
		if ( args.length != 0 ){
			FileHandler handler = FileAssociations.instance.getHandlerFor(args[0])
			if ( handler ){
				handled = handler.handle(args[0])
			}else{
				showCouldNotOpenFile(i18n.msg('FDFUI.couldNotOpenFileDialogUnknownFileExtensionText',args[0], FilenameUtils.getExtension(args[0])))
			}
		}
		
		if ( !handled ){
			syncAndShowMessage()
		}

		def ps = Settings.instance.preferenceStore()
		if ( ps.getBoolean(PropertyConsts.AUTOMATIC_UPDATES) ){
			UpdateFinder.instance.searchForUpdate(true)
		}
		
		boolean firstTime = ps.getBoolean(PropertyConsts.FIRST_TIME)
		if ( firstTime ){
			def dialog = new MessageDialog(shlFileDuplicateFinder, i18n.msg('FDFUI.userExperienceDialogTitle'), null, i18n.msg('FDFUI.userExperienceDialogText'), MessageDialog.INFORMATION, [i18n.msg('FDFUI.userExperienceDialogOkLbl'), i18n.msg('FDFUI.userExperienceDialogNoLbl')] as String[], 0)
			dialog.setShellStyle(dialog.getShellStyle())
			boolean inUserExperience = (dialog.open() == 0)
			ps.setValue(PropertyConsts.TRACKING, inUserExperience)
			ps.setValue(PropertyConsts.FIRST_TIME, false)
			ps.save()
		}

		tracker.trackStarted()
		try{
			def sw = new StopWatch()
			sw.start()
			while (!shlFileDuplicateFinder.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep()
				}
			}
			sw.stop()
			Settings.instance.save()
			tracker.trackLastEvent('AppClosed','Normal',Utils.formatInterval(sw.getTime()))
		}catch(Exception e){
			tracker.trackLastEvent('AppClosed','Exception',e.getMessage())
		}
		ps.setValue(PropertyConsts.TS_LAST, tracker.getCurrentTS())
		ps.setValue(PropertyConsts.VISITS, tracker.getVisits())
		ps.save()
//		addWindowListener
//		(new WindowAdapter() {
//		  public void windowClosing(WindowEvent e) {
//			System.exit(0)
//			}
//		  }
//		)
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
		
		Menu menuFile = new Menu(mntmNewSubmenu)
		mntmNewSubmenu.setMenu(menuFile)
		
		mntmNewSearch = new MenuItem(menuFile, SWT.NONE)
		mntmNewSearch.setText(i18n.msg('FDFUI.fileNewSearch'))
		mntmNewSearch.addSelectionListener(new ClosureSelectionAdapter(c: searchAgain))
		
		mntmLoadSearchParameters = new MenuItem(menuFile, SWT.NONE)
		mntmLoadSearchParameters.setText(i18n.msg('FDFUI.fileLoadSearchParameters'))
		mntmLoadSearchParameters.addSelectionListener(new ClosureSelectionAdapter(c: loadSearchParameters))
		
		mntmLoadSearchSession = new MenuItem(menuFile, SWT.NONE)
		mntmLoadSearchSession.setText(i18n.msg('FDFUI.fileLoadSearchSession'))
		mntmLoadSearchSession.addSelectionListener(new ClosureSelectionAdapter(c: loadSearchSession))
		
		mntmLoadDuplicateResultsSession = new MenuItem(menuFile, SWT.NONE)
		mntmLoadDuplicateResultsSession.setText(i18n.msg('FDFUI.fileLoadDuplicateResultsSession'))
		mntmLoadDuplicateResultsSession.addSelectionListener(new ClosureSelectionAdapter(c: loadDuplicateResultsSession))
		
		new MenuItem(menuFile, SWT.SEPARATOR)
		
		mntmPreferences = new MenuItem(menuFile, SWT.NONE)
		mntmPreferences.setText(i18n.msg('FDFUI.filePreferences'))
		mntmPreferences.addSelectionListener(new ClosureSelectionAdapter(c: openPreferences))
		
		new MenuItem(menuFile, SWT.SEPARATOR)
		
		MenuItem mntmNewItem_2 = new MenuItem(menuFile, SWT.NONE)
		mntmNewItem_2.setText(i18n.msg('FDFUI.fileClose'))
		mntmNewItem_2.addSelectionListener(new ClosureSelectionAdapter(c: close))
		
		MenuItem mntmDrives = new MenuItem(menu, SWT.CASCADE)
		mntmDrives.setText(i18n.msg('FDFUI.drivesMenu'))
		
		Menu menuDrives = new Menu(mntmDrives)
		mntmDrives.setMenu(menuDrives)
		
		MenuItem mntmMapNetworkDrive = new MenuItem(menuDrives, SWT.NONE)
		mntmMapNetworkDrive.setText(i18n.msg('FDFUI.drivesManage'))
		mntmMapNetworkDrive.addSelectionListener(new ClosureSelectionAdapter(c: openDrivesMapper))
		
		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE)
		mntmHelp.setText(i18n.msg('FDFUI.helpMenu'))
		
		Menu menuHelp = new Menu(mntmHelp)
		mntmHelp.setMenu(menuHelp)
		
		MenuItem mntmUpdate = new MenuItem(menuHelp, SWT.NONE)
		mntmUpdate.setText(i18n.msg('FDFUI.helpUpdate'))
		mntmUpdate.addSelectionListener(new ClosureSelectionAdapter(c: {
			UpdateFinder.instance.searchForUpdate(false)
		}))
		
		new MenuItem(menuHelp, SWT.SEPARATOR)
		
		MenuItem mntmDonate = new MenuItem(menuHelp, SWT.NONE)
		mntmDonate.setText(i18n.msg('FDFUI.helpDonate'))
		mntmDonate.addSelectionListener(new ClosureSelectionAdapter(c: goToDonationPage))
		
		MenuItem mntmAbout = new MenuItem(menuHelp, SWT.NONE)
		mntmAbout.setText(i18n.msg('FDFUI.helpAbout'))
		mntmAbout.addSelectionListener(new ClosureSelectionAdapter(c: openAboutDialog))
		
		sashForm = new SashForm(shlFileDuplicateFinder, SWT.NONE)
		
		checkboxTreeViewer = new CheckboxTreeViewer(sashForm, SWT.BORDER)
		checkboxTreeViewer.setUseHashlookup(true)
		
		def propertyDescriptor = BeanPropertyHelper.getPropertyDescriptor(FileWrapper.class, 'dirs')
		def property = new FileWrapperBeanListProperty(propertyDescriptor, FileWrapper.class)
		def decorator = new BeanListPropertyDecorator(property, propertyDescriptor)
		
		ViewerSupport.bind(checkboxTreeViewer, treeInput, decorator, BeanProperties.value(FileWrapper.class, 'name'))
		
		checkboxTreeViewer.setLabelProvider(new FileWrapperTreeLabelProvider())
		
		ScrolledComposite scrolledComposite_1 = new ScrolledComposite(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL)
		scrolledComposite_1.setExpandHorizontal(true)
		scrolledComposite_1.setExpandVertical(true)
		
		Composite composite = new Composite(scrolledComposite_1, SWT.NONE)
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true))
		
		def gridLayout = new GridLayout(1, false)
		gridLayout.verticalSpacing = 13
		composite.setLayout(gridLayout)

		minSizeOption = new SizeOption(composite, SWT.NONE, i18n.msg('FDFUI.minSizeFilter'))
		
		maxSizeOption = new SizeOption(composite, SWT.NONE, i18n.msg('FDFUI.maxSizeFilter'))
		
		nameOption = new TextFieldOption(composite, SWT.NONE, i18n.msg('FDFUI.fileNameFilter'))
		nameOption.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false))
		nameOption.setToolTipText(i18n.msg('FDFUI.fileNameFilterTooltip'))
		
		extensionsOption = new TextFieldOption(composite, SWT.NONE, i18n.msg('FDFUI.extensionFilter'))
		extensionsOption.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false))
		extensionsOption.setToolTipText(i18n.msg('FDFUI.extensionFilterTooltip'))
		
		Button btnSaveParameters = new Button(composite, SWT.NONE)
		btnSaveParameters.setText(i18n.msg('FDFUI.saveParametersBtn'))
		btnSaveParameters.addSelectionListener(new ClosureSelectionAdapter(c: saveSearchParameters))
		
		Button btnDuplicateSearch = new Button(composite, SWT.NONE)
		btnDuplicateSearch.setText(i18n.msg('FDFUI.searchBtn'))
		btnDuplicateSearch.addSelectionListener(new ClosureSelectionAdapter(c: searchForDuplicates))
		
		Button btnAutoDelete = new Button(composite, SWT.NONE)
		btnAutoDelete.setText(i18n.msg('FDFUI.autoDeleteBtnLabel'))
		btnAutoDelete.setToolTipText(i18n.msg('FDFUI.autoDeleteBtnTooltip'))
		btnAutoDelete.addSelectionListener(new ClosureSelectionAdapter(c: autoDelete))

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
	}
	
	def close = {
		shlFileDuplicateFinder.close()
	}
	
	def goToDonationPage = {
		try{
		Desktop.getDesktop().browse(new URI('https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=fezuqqg9t6j6y'))
		} catch (Exception e1) {}
	}

	def openAboutDialog = {
		new AboutDialog(shlFileDuplicateFinder, SWT.DIALOG_TRIM).open()
	}
	
	def autoDelete = {
		doSearchForDuplicates(true)
	}
	
	def searchForDuplicates = {doSearchForDuplicates()}
	
	def doSearchForDuplicates = { autodelete = false ->
		def directories = checkboxTreeViewer.getCheckedElements()
		if ( directories.length == 0 ){
			InternationalizedTrackingMessageDialogHelper.openError(shlFileDuplicateFinder, i18n.msg('FDFUI.noFolderSelectedDialogTitle'), i18n.msg('FDFUI.noFolderSelectedDialogText'))
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
		scanProgress.scanAndSearch(filters, directories, autodelete)
	}
	
	def saveSearchParameters = {
		FileDialog dlg = new FileDialog(shlFileDuplicateFinder, SWT.SAVE)
		dlg.setFilterNames([i18n.msg('FDFUI.loadSearchParametersDialogFilterNames')] as String [])
		dlg.setFilterExtensions([i18n.msg('FDFUI.loadSearchParametersDialogFilterExtensions')] as String [])
		String fn = dlg.open()
		if (fn != null) {
			new File(fn).withObjectOutputStream { oos ->
				oos.writeObject(getSearchParameters())
				DefaultGroovyMethodsSupport.closeQuietly(oos)
			}
		}
	}
	
	def loadSearchParameters = {
		showOpenFileDialog([i18n.msg('FDFUI.loadSearchParametersDialogFilterNames')], [i18n.msg('FDFUI.loadSearchParametersDialogFilterExtensions')], loadSearchParametersFile)
	}
	
	def loadSearchParametersFile = { File file ->
		loadFile(file){ SearchParameters parameters ->
			syncAndShowMessage()
			minSizeOption.setData(parameters.getMinSize())
			maxSizeOption.setData(parameters.getMaxSize())
			nameOption.setData(parameters.getNamesFilter())
			extensionsOption.setData(parameters.getExtensionsFilter())
		}
	}
	
	def getSearchParameters = {
		new SearchParameters(minSize: minSizeOption.getData() as Long, maxSize: maxSizeOption.getData() as Long,
			namesFilter: nameOption.getData(), extensionsFilter: extensionsOption.getData(),
			directories: checkboxTreeViewer.getCheckedElements())
	}
	
	def loadSearchSession = {
		showOpenFileDialog([i18n.msg('FDFUI.loadSearchSessionDialogFilterNames')], [i18n.msg('FDFUI.loadSearchSessionDialogFilterExtensions')], loadSearchSessionFile)
	}
	
	def loadSearchSessionFile = { File file ->
		loadFile(file){ progress ->
			showScanProgress()
			scanProgress.resumeSearch(progress)
		}
	}
	
	def loadDuplicateResultsSession = {
		showOpenFileDialog([i18n.msg('FDFUI.loadDuplicateSessionDialogFilterNames')], [i18n.msg('FDFUI.loadDuplicateSessionDialogFilterExtensions')], loadDuplicateResultsSessionFile)
	}
	
	def loadDuplicateResultsSessionFile = { File file ->
		loadFile(file){ entries ->
			def sanitized = sanitizeEntries(entries)
			if ( !sanitized.nonExistingFiles.isEmpty() ){
				def dialog = new FilesNotFoundDialog(shlFileDuplicateFinder, SWT.DIALOG_TRIM)
				dialog.files = sanitized.nonExistingFiles
				dialog.open()
			}
			tracker.trackPageView("/seeDuplicates?file")
			showDuplicates(sanitized.entries)
		}
	}
	
	def showOpenFileDialog(names, extensions, Closure c){
		FileDialog dlg = new FileDialog(shlFileDuplicateFinder, SWT.OPEN)
		dlg.setFilterNames(names as String [])
		dlg.setFilterExtensions(extensions as String [])
		String fn = dlg.open()
		if (fn != null) {
			c.call(new File(fn))
		}
	}
	
	boolean loadFile(File file, afterOpenedClosure){
		def obj
		def loaded = false
		try{
			file.withObjectInputStream { ios ->
				obj = ios.readObject()
				DefaultGroovyMethodsSupport.closeQuietly(ios)
			}
			afterOpenedClosure.call(obj)
			loaded = true
		}catch(FileNotFoundException e){
			showCouldNotOpenFile(i18n.msg('FDFUI.couldNotOpenFileDialogSessionFileNotFoundText',file.getAbsolutePath()))
		}catch(StreamCorruptedException e){
			showCouldNotOpenFile(i18n.msg('FDFUI.couldNotOpenFileDialogDamagedSessionFileText',file.getAbsolutePath()))
		}catch(InvalidClassException e){
			showCouldNotOpenFile(i18n.msg('FDFUI.couldNotOpenFileDialogDamagedSessionFileText',file.getAbsolutePath()))
		}
		
		loaded
	}
	
	def sanitizeEntries = { entries ->
		def sanitized = [ nonExistingFiles : [] ]
		sanitized.entries = entries.findAll{entry ->
			sanitized.nonExistingFiles.addAll(entry.removeNonExistingFiles())
			entry.hasDuplicates()
		}
		sanitized
	}
	
	def showDuplicates = { entries, autodelete = false ->
		if ( autodelete ){
			scanResults.autoDeleteEntries(entries)
		}else{
			scanResults.updateEntries(entries)
		}
		stackLayout.topControl = scanResults
		shlFileDuplicateFinder.layout()
		enableFileMenu()
	}
	
	def enableFileMenu(){
		setFileMenuStatus(true)
	}
	
	def disableFileMenu(){
		setFileMenuStatus(true)
	}
	
	def setFileMenuStatus = { state ->
		mntmNewSearch.setEnabled(state)
		mntmLoadSearchParameters.setEnabled(state)
		mntmLoadSearchSession.setEnabled(state)
		mntmLoadDuplicateResultsSession.setEnabled(state)
	}
	
	def showScanProgress(){
		stackLayout.topControl = scanProgress
		shlFileDuplicateFinder.layout()
		disableFileMenu()
	}
	
	def searchAgain = {
		tracker.trackPageView("/search")
		if ( !syncedDrives ){
			syncAndRefresh()
		}
		stackLayout.topControl = sashForm
		shlFileDuplicateFinder.layout()
		enableFileMenu()
	}
	
	def openPreferences = {
		new GDFPreferenceDialog(shlFileDuplicateFinder).open()
	}

	def openDrivesMapper = {
		def manager = new NetworkDrivesManagerDialog(shlFileDuplicateFinder, SWT.DIALOG_TRIM)
		def lastAuthModels = Settings.instance.getLastNetworkDrivesAuthModels()
		manager.drives.addAll(lastAuthModels)
		
		def newModels = manager.open()
		Settings.instance.lastNetworkAuthModels = newModels
		syncAndRefresh()
	}
	
	def syncAndShowMessage(){
		if ( !syncAndRefresh(true) ){
			InternationalizedTrackingMessageDialogHelper.openInformation(shlFileDuplicateFinder, i18n.msg('FDFUI.disconnectedDrivesDialogTitle'), i18n.msg('FDFUI.disconnectedDrivesDialogText'))
		}
	}
	
	def syncAndRefresh(umount = false){
		def status = syncDrivesWithTree(umount)
		checkboxTreeViewer.refresh()
		status
	}

	def syncDrivesWithTree(umountAtErrors = false){
		syncedDrives = true
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
		if ( syncedDrives ){
			directories.unique().each { directory -> treeInput.reloadDirectoryFiles(directory) }
		}
	}

	def showCouldNotOpenFile(msg){
		InternationalizedTrackingMessageDialogHelper.openError(shlFileDuplicateFinder, i18n.msg('FDFUI.couldNotOpenFileDialogTitle'), msg)
	}
}

