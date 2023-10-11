package com.sleepcamel.fileduplicatefinder.core.domain.finder

interface DuplicateFinder {

	def scan()
	
	def findDuplicates()
	
	def suspend()
	
	def resume()
	
}
