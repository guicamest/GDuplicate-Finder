package com.sleepcamel.fileduplicatefinder.core.util
import java.security.MessageDigest

import org.apache.commons.codec.digest.DigestUtils;
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;

class MD5Utils {

	static def oldGenerateMD5(final file) {
		MessageDigest digest = MessageDigest.getInstance('MD5')
		InputStream is = new BufferedInputStream(new FileInputStream(file), 16384);
		byte[] buffer = new byte[16384];
		int read = 0
		while( (read = is.read(buffer)) > 0) {
			digest.update(buffer, 0, read);
		}
		new BigInteger(1, digest.digest()).toString(16)
	}

	static def generateMD5(InputStream is) {
		is = new BufferedInputStream(is, 16384);
		MessageDigest digest = MessageDigest.getInstance('MD5');
		byte[] buffer = new byte[16384]
		int read = 0
		while( (read = is.read(buffer)) > 0) {
			digest.update(buffer, 0, read);
		}
		DefaultGroovyMethodsSupport.closeQuietly(is)
		new BigInteger(1, digest.digest()).toString(16)
	}
	
}
