#!/bin/bash

#Script for building each environment distribution zips

for os in windows32 windows64 linux32 linux64
do
	gradle distZip -Pos=$os
done
