package com.sleepcamel.fileduplicatefinder.ui.utils

import groovy.lang.Singleton;

import org.codehaus.groovy.runtime.memoize.LRUCache;

@Singleton
class PreviewFilesCache {

	private static final int MAX_PREVIEW_FILES = 20;
	private LRUCache cachedFiles = new LRUCache(MAX_PREVIEW_FILES);

	def put(md5, file){
		cachedFiles.put(md5, file)
	}

	def get(md5){
		cachedFiles.get(md5)
	}
}