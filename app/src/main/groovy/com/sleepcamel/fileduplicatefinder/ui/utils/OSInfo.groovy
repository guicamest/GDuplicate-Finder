package com.sleepcamel.fileduplicatefinder.ui.utils;

public class OSInfo {

	static def autodetectOS(){
		def defaultOS = 'maccocoa'
		def osName = System.getProperty('os.name').toLowerCase()
		def os = defaultOS

		if ( osName.contains('windows') ){
			os = 'windows'
		}
		if ( osName.contains('linux') ){
			os = 'linux'
		}
		
		def arch = System.getProperty('os.arch').contains('64') ? '64' : '32'
		"${os}${arch}"
	}
}
