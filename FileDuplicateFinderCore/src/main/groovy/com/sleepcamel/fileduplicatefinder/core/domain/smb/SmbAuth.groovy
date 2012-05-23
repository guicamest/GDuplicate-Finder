package com.sleepcamel.fileduplicatefinder.core.domain.smb

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbSession;
import groovy.beans.Bindable;
import groovy.transform.EqualsAndHashCode;

@Bindable
@EqualsAndHashCode
class SmbAuth implements Serializable {

	String address = "";
	String username = "";
	String password = "";
	String folder = "";
	
	def domain(){
		UniAddress.getByName(address)
	}
	
	def auth(){
		new NtlmPasswordAuthentication(address, username, password)
	}
	
	def folderPath(){
		"smb://$address/$folder/"
	}
	
	def folderFile(){
		new SmbFile(folderPath(), auth())
	}
	
	public String toString() {
		"$username @ ${folderPath()}"
	}
	
	def checkPathExists = {
		SmbSession.logon(domain(), auth());
		folderFile().exists();
	}
}
