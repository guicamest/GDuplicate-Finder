package com.sleepcamel.fileduplicatefinder.ui.adapters

import org.eclipse.jface.viewers.ILabelProvider
import org.eclipse.jface.viewers.ILabelProviderListener
import org.eclipse.swt.graphics.Image

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources


class FileWrapperTreeLabelProvider implements ILabelProvider {

	private List listeners

	// Images for tree nodes
//	private Image file;
	def dir
	def drive
	def netDrive

	public FileWrapperTreeLabelProvider() {
		// Create the list to hold the listeners
		listeners = new ArrayList()

		// Create the images
		try {
//			file = new Image(null, this.getClass().getResourceAsStream('/images/file.gif'));
			dir = FDFUIResources.instance.getImageFile('directory.gif')
			drive = FDFUIResources.instance.getImageFile('harddrive.gif')
			netDrive = FDFUIResources.instance.getImageFile('networkDrive.gif')
		} catch (FileNotFoundException e) {
			// Swallow it; we'll do without images
		}
	}

	public Image getImage(Object arg0) {
		def img = dir
		FileWrapper wrapper = (FileWrapper) arg0
		if ( wrapper.isRoot ){
			img = wrapper.isLocal() ? drive : netDrive
		}
		img
	}

	public String getText(Object arg0) {
		FileWrapper wrapper = (FileWrapper) arg0
		// Get the name of the file
		String text = wrapper.getName()

		// If name is blank, get the path
		if (text.length() == 0) {
			text = (String) wrapper.path
		}
		text
	}

	public void addListener(ILabelProviderListener arg0) {
		listeners.add(arg0)
	}

	public void dispose() {
		// Dispose the images
		if (dir != null) dir.dispose()
		if (drive != null) drive.dispose()
		if (netDrive != null) netDrive.dispose()
	}

	public boolean isLabelProperty(Object arg0, String arg1) {
		false
	}

	public void removeListener(ILabelProviderListener arg0) {
		listeners.remove(arg0)
	}
}
