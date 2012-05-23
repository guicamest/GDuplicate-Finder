package com.sleepcamel.fileduplicatefinder.core.main

import com.sleepcamel.fileduplicatefinder.core.domain.DuplicateEntry
import com.sleepcamel.fileduplicatefinder.core.domain.DuplicateFinder
import com.sleepcamel.fileduplicatefinder.core.domain.DuplicateFinderReporter
import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper


class MainClass {

	public static void main(String[] args) {
//		findWithoutSuspend()
//		findSuspend([10, 20, 30, 40])
		findSuspendSaveLoadAndResume([20])
	}
	
	public static findWithoutSuspend(){
		
		def df = new DuplicateFinder();
//		df.directories = [new FileWrapper(new File("e:\\My Virtual Machines"))]
		df.directories = [new FileWrapper(new File("E:\\Mi música"))]
//		df.directories = [new FileWrapper(new File("E:\\Iphone"))]

		def dfr = new DuplicateFinderReporter(progress: df.findProgress)

		dfr.startReportingScan()
		df.scan()
		
		while(!dfr.finishedReportingScan()){
			Thread.sleep(10L)
		}

		System.out.println("Finished Scan")
		System.out.println("Starting to find duplicates")
		
		dfr.startReportingDuplicates()
		df.findDuplicates()
		
		while(!dfr.finishedReportingDuplicates()){
			Thread.sleep(10L)
		}
		
		df.duplicatedEntries.each { DuplicateEntry entry ->
			System.out.println("md5 ${entry.getHash()} has ${entry.getFiles().size()} duplicates");
		}
		
		df.duplicatedFiles.each { file ->
			System.out.println("File ${file.path} is duplicated");
		}
	}
	
	public static findSuspendSaveLoadAndResume(percentsToSuspend){
		
		def savedFile = File.createTempFile('lala', 'eo')
		
		def df = new DuplicateFinder();
		df.directories = [new FileWrapper(new File("E:\\Mi música\\Las Mas Bailadas"))]

		def dfr = new DuplicateFinderReporter(progress: df.findProgress)

		dfr.startReportingScan()
		df.scan()
		
		while(!dfr.finishedReportingScan()){
			Thread.sleep(10L)
		}

		System.out.println("Finished Scan")
		System.out.println("Starting to find duplicates")
		
		dfr.startReportingDuplicates()
		df.findDuplicates()
		
		percentsToSuspend.each {percentToSuspend ->
			while(df.findProgress.percentDone() * 100 < percentToSuspend){
				Thread.sleep(10L)
			}
			df.suspend()
			dfr.suspendReporting()
			
			def p = df.findProgress;
			
			synchronized (p) {
				savedFile.withObjectOutputStream { oos ->
				oos.writeObject(p)
				}
			}
			
			println "Search for duplicates suspended\n Waiting 10 secs..."
			int i = 0
			while ( i < 10 ){
				Thread.sleep(1000L)
				i++
			}
			println "Finished sleeping... Loading results"

			def savedProgress
			savedFile.withObjectInputStream(){ ois ->
				savedProgress = ois.readObject()
			}
			dfr.progress = savedProgress

			df = new DuplicateFinder(findProgress: savedProgress)
			df.resume()

			dfr.resumeReporting()
		}
		
		while(!dfr.finishedReportingDuplicates()){
			Thread.sleep(10L)
		}
		
		df.duplicatedEntries.each { DuplicateEntry entry ->
			System.out.println("md5 ${entry.getHash()} has ${entry.getFiles().size()} duplicates");
		}
		
		df.duplicatedFiles.each { file ->
			System.out.println("File ${file.path} is duplicated");
		}
	}
	
	public static findSuspend(percentsToSuspend){
		
		def df = new DuplicateFinder();
		df.directories = [new FileWrapper(new File("E:\\Mi música"))]

		def dfr = new DuplicateFinderReporter(progress: df.findProgress)

		dfr.startReportingScan()
		df.scan()
		
		while(!dfr.finishedReportingScan()){
			Thread.sleep(10L)
		}

		System.out.println("Finished Scan")
		System.out.println("Starting to find duplicates")
		
		dfr.startReportingDuplicates()
		df.findDuplicates()
		
		percentsToSuspend.each {percentToSuspend ->
			while(df.findProgress.percentDone() * 100 < percentToSuspend){
				Thread.sleep(10L)
			}
			df.suspend()
			println "Search for duplicates suspended\n Waiting 10 secs..."
			int i = 0
			while ( i < 10 ){
				Thread.sleep(1000L)
				i++
				println "Slept $i secs..."
			}
			df.resume()
		}
		
		while(!dfr.finishedReportingDuplicates()){
			Thread.sleep(10L)
		}
		
		df.duplicatedEntries.each { DuplicateEntry entry ->
			System.out.println("md5 ${entry.getHash()} has ${entry.getFiles().size()} duplicates");
		}
		
		df.duplicatedFiles.each { file ->
			System.out.println("File ${file.path} is duplicated");
		}
	}
}
