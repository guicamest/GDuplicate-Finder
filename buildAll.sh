#!/bin/bash

#Script for building each environment distribution zips

cp FileDuplicateFinderUI/src/main/resources/com/sleepcamel/fileduplicatefinder/ui/tracking/analytics.properties ga.dev
cp prodData/analytics.prod FileDuplicateFinderUI/src/main/resources/com/sleepcamel/fileduplicatefinder/ui/tracking/analytics.properties
for os in windows32 windows64 linux32 linux64 maccocoa32 maccocoa64
do
	gradle distZip -Pos=$os
done
mv ga.dev FileDuplicateFinderUI/src/main/resources/com/sleepcamel/fileduplicatefinder/ui/tracking/analytics.properties
