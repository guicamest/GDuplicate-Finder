package com.sleepcamel.fileduplicatefinder.ui.dialogs

import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.swt.widgets.Shell

import com.sleepcamel.fileduplicatefinder.ui.tracking.AnalyticsTracker
import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources

class InternationalizedTrackingMessageDialogHelper {

	static FDFUIResources i18n = FDFUIResources.instance
	static AnalyticsTracker tracker = AnalyticsTracker.instance
	
	public static void openError(Shell parent, String title, String message) {
		open(1, parent, title, message, 0)
	}

	public static void openInformation(Shell parent, String title, String message) {
		open(2, parent, title, message, 0)
	}

	public static boolean openQuestion(Shell parent, String title, String message) {
		return open(3, parent, title, message, 0)
	}

	public static void openWarning(Shell parent, String title, String message) {
		open(4, parent, title, message, 0)
	}
	
	public static boolean openConfirm(Shell parent, String title, String message) {
		return open(5, parent, title, message, 0)
	}

	public static boolean open(int kind, Shell parent, String title, String message, int style) {
		MessageDialog dialog = new MessageDialog(parent, title, null, message, kind, getButtonLabels(kind), 0)
		style &= 268435456
		dialog.setShellStyle(dialog.getShellStyle() | style)
		
		def lastPage = tracker.lastPage
		tracker.trackPageView("/${i18n.keyForMsg(title)}")
		boolean retValue = (dialog.open() == 0)
		tracker.trackPageView(lastPage)
		retValue
	}

	static String[] getButtonLabels(int kind) {
		def dialogButtonLabels
		switch (kind) {
			case 1:
			case 2:
			case 4:
				dialogButtonLabels = [i18n.msg('FDFUI.dialogOKBtn')] as String []
				break
			case 3:
				dialogButtonLabels = [i18n.msg('FDFUI.dialogYesBtn'), i18n.msg('FDFUI.dialogNoBtn')] as String []
				break
			case 5:
				dialogButtonLabels = [i18n.msg('FDFUI.dialogOKBtn'), i18n.msg('FDFUI.dialogCancelBtn')] as String []
				break
			case 6:
				dialogButtonLabels = [i18n.msg('FDFUI.dialogYesBtn'), i18n.msg('FDFUI.dialogNoBtn'), i18n.msg('FDFUI.dialogCancelBtn')] as String []
				break
			default:
				throw new IllegalArgumentException("Illegal value for kind in MessageDialog.open()")
		}
		dialogButtonLabels
	}
}
