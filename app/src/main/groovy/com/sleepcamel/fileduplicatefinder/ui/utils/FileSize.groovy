package com.sleepcamel.fileduplicatefinder.ui.utils

import org.apache.commons.lang3.ArrayUtils

public enum FileSize {
	BYTE('Bytes'),
	KILOBYTE('Kbs'),
	MEGABYTE('Mbs'),
	GIGABYTE('Gbs'),
	TERABYTE('Tbs'),
	PENTABYTE('Pbs'),
	EXABYTE('Ebs')
	
	private String friendlyName
	private long sizeInBytes
	private static FileSize[] reversedValues
	private static FDFUIResources i18n = FDFUIResources.instance

	private FileSize(String friendlyName){
		this.friendlyName = friendlyName
		this.sizeInBytes = (long) Math.pow(1024, ordinal())
	}
	
	public String getFriendlyName(){
		i18n.msg("FDFUI.size$friendlyName")
	}
	
	public long getSizeInBytes(){
		sizeInBytes
	}

	public Long toBytes(int bytes){
		(long) Math.floor(bytes * this.sizeInBytes)
	}
	
	public static FileSize[] reversedValues(){
		if ( reversedValues == null ){
			reversedValues = values()
			ArrayUtils.reverse(reversedValues)
		}
		reversedValues
	}
}
