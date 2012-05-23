package com.sleepcamel.fileduplicatefinder.ui.listeners

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;

class MenuKeyDetectListener implements MenuDetectListener, KeyListener {
	private def ctrlPressed = false
	def ctrlStatus = false
	public void menuDetected(MenuDetectEvent paramMenuDetectEvent){
		ctrlStatus = ctrlPressed
		ctrlPressed = false
	}

	public void keyPressed(KeyEvent paramKeyEvent) {
		ctrlPressed = (paramKeyEvent.keyCode & SWT.CTRL) != 0
	}

	public void keyReleased(KeyEvent paramKeyEvent) {
		ctrlPressed = false
	}
}


