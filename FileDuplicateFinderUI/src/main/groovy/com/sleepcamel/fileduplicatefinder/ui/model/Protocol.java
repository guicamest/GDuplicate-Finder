package com.sleepcamel.fileduplicatefinder.ui.model;

public enum Protocol {
	SMB("SMB", "Samba share", 445),
	SFTP("SFTP", "Scp share", 22),
	FTP("FTP", "Ftp share", 21);

	private String name;
	private Integer defaultPort;
	private String text;

	Protocol(String name, String text, int defaultPort) {
		this.name = name;
		this.defaultPort = defaultPort;
		this.text = text;
	}

	public String getName() {
		return name;
	}

	public int getDefaultPort() {
		return defaultPort;
	}

	public String getDescription() {
		return text;
	}

	public String toString() {
		return getName();
	}
}
