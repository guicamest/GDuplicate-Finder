package com.sleepcamel.fileduplicatefinder.core.main

import org.apache.commons.lang3.time.StopWatch


class MD5Compare {

	public static void main(String[] args) {
		
		Thread.start {
			StopWatch sw = new StopWatch();
			sw.start();
			MD5Utils.oldGenerateMD5(new File("E:\\My Virtual Machines\\Gentoo\\Gentoo.vmdk"))
			sw.stop();
			System.out.println("Took ${sw.getTime()} with old");
		}
		
		Thread.start {
			StopWatch sw = new StopWatch();
			sw.start();
			MD5Utils.fGenerateMD5(new File("E:\\My Virtual Machines\\To delete too.vmdk"))
//			twMD5.asHex(twMD5.getHash(new File("E:\\My Virtual Machines\\To delete too.vmdk")));
			sw.stop();
			System.out.println("Took ${sw.getTime()} with F");
		}
//		Thread.start {
//			StopWatch sw = new StopWatch();
//			sw.start();
//			MD5Utils.generateMD5(new File("E:\\My Virtual Machines\\To delete.vmdk"))
//			sw.stop();
//			System.out.println("Took ${sw.getTime()} with new");
//		}
	}
}
