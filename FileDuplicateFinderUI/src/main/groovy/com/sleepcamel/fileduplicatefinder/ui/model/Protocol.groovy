package com.sleepcamel.fileduplicatefinder.ui.model

import com.sleepcamel.fileduplicatefinder.ui.utils.FDFUIResources

public enum Protocol {
	SMB('Smb', 445),
	SFTP('Sftp', 22),
	FTP('Ftp', 21)

	private String friendlyName
	private Integer defaultPort
	private static FDFUIResources i18n = FDFUIResources.instance

	private Protocol(String name, int defaultPort) {
		this.friendlyName = name
		this.defaultPort = defaultPort
	}

	def getDefaultPort() {
		defaultPort
	}

	def getFriendlyName(){
		i18n.msg("FDFUI.protocol$friendlyName")
	}
}
