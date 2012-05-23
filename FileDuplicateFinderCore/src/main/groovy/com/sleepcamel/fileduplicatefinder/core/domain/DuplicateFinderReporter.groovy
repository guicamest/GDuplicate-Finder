package com.sleepcamel.fileduplicatefinder.core.domain;

import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.plaf.ProgressBarUI;

import org.apache.commons.lang3.time.StopWatch;

import groovy.time.TimeCategory;
import groovy.time.TimeDuration;
import groovy.transform.Synchronized;
import groovyx.gpars.agent.AgentBase.AwaitClosure;

public class DuplicateFinderReporter {

	def percentFormatter = NumberFormat.getPercentInstance(Locale.getDefault());
	
	DuplicateFinderProgress progress

	def reportScanInterval = 5
	def reportDuplicatesInterval = 1000
	def finishedScan = false
	def finishedDuplicates = false

	def startReportingScan(){
		Thread.start {
			while( !progress.finishedScanning ){
				def lastFoundFile = (progress.foundFiles) ? progress.foundFiles[-1] : null
				if ( lastFoundFile ){
					System.out.println("Found file ${lastFoundFile.path}");
					System.out.println("Files found: ${progress.totalFiles} - Size: ${progress.totalFileSize}")
				}
				Thread.sleep(reportScanInterval)
			}
			finishedScan = true
		}
	}
	
	@Synchronized
	def finishedReportingScan(){
		finishedScan
	}
	
	AtomicBoolean ab = new AtomicBoolean(false);
	
	def suspendReporting(){
		ab.set(true);
	}
	
	def resumeReporting(){
		ab.set(false);
		synchronized (ab) {
		ab.notify()
		}
	}
	
	def reportingThread
	
	def startReportingDuplicates(){
		reportingThread = Thread.start {
			def stopWatch = new StopWatch();
			stopWatch.start()

			while( !progress.finishedFindingDuplicates ){
				Thread.sleep(reportDuplicatesInterval)
				if ( ab.get() ){
					synchronized (ab) {
						ab.wait();
					}
				}
				
				def percentDone = progress.percentDone()
				def elapsedTime = stopWatch.getTime()
				def timeLeft = Math.floor((elapsedTime * (1 - percentDone)) / percentDone) as Long

				System.out.println("\nProgress ${percentString(percentDone)}");
				System.out.println("Processed ${progress.processedFiles.size()} - Left ${progress.foundFiles.size()} files");
				System.out.println("Processed ${progress.processedFilesQty} of ${progress.totalFiles} files");
				System.out.println("Processed ${progress.processedFileSize} of ${progress.totalFileSize} bytes");
				System.out.println("Elapsed time: ${formatInterval(elapsedTime)} - Remaining time: ${formatInterval(timeLeft)}");
			}
			finishedDuplicates = true
		}
	}
	
	private static String formatInterval(final long millis)
	{
		final long hr = TimeUnit.MILLISECONDS.toHours(millis);
		final long min = TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS.toSeconds(millis - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		String.format("%02d:%02d:%02d", hr, min, sec);
	}
	
	def percentString(percent){
		percentFormatter.format(percent);
	}
	
	@Synchronized
	def finishedReportingDuplicates(){
		finishedDuplicates
	}
}
