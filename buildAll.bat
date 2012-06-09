@echo off
FOR %%O IN (windows32 windows64 linux32 linux64) DO gradle distZip -Pos=%%O
