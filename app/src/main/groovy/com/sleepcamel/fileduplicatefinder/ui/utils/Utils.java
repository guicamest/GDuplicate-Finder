package com.sleepcamel.fileduplicatefinder.ui.utils;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {

	public static String formatInterval(final long millis){
		final long hr = TimeUnit.MILLISECONDS.toHours(millis);
		final long min = TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS.toSeconds(millis - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		return String.format("%02d:%02d:%02d", hr, min, sec);
	}
	
	private static NumberFormat percentFormatter = NumberFormat.getPercentInstance(Locale.getDefault());

	public static String percentString(double percent){
		return percentFormatter.format(percent);
	}
	
	public static String formatBytes(long bytes){
		Object[] countAndFileSize = getCountAndFileSize(bytes);
		return String.format("%3.2f %s", countAndFileSize[0], ((FileSize) countAndFileSize[1]).getFriendlyName());
	}
	
	public static Object[] getCountAndFileSize(long bytes){
		Object[] ret = new Object[2];
		for(FileSize size:FileSize.reversedValues()){
			double step = size.getSizeInBytes();
			ret[1] = size;
	        if (bytes > step){
	        	ret[0] = bytes / step;
	        	return ret;
	        }
		}
		ret[0] = bytes / 1.0;
		return ret;
	}

}
