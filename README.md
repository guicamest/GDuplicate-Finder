GDuplicateFinder - A FREE Groovy way to find file duplicates!

Unlike other duplicate finders such as Easy Duplicate File Finder,
Auslogics Duplicate File Finder, MindGems Fast Duplicate File Finder,
Duplicate Cleaner, DupFiles and others, GDuplicateFinder is a FREE cross-platform
application, with the ability to search among not just local files, but files on
the network, such as a Windows or a Linux share using VFS library.

Taking advantage of Groovy facilities and GPars power to process in parallel,
GDuplicateFinder will help you get rid of those duplicates you always
wanted to dispose in an easy and friendly way.

If you feel like contributing, you can always fork this repo. Gradle will help
you configure the Eclipse project for you. You can also suggest improvements.

## Current features

	*Manage network drives(add, edit, remove)
	*Stop, save, load and resume a search for duplicates
	*Open duplicate files with system's default editor
	*Turn on/off network drives
	*Save/load duplicates results session
	*Internationalization

## Network Shares supported

	SMB (Samba)
	SFTP / SCP (Connection via Ssh)
	FTP


How to build
------------
Required gradle version >= 1.0-rc-1
Requires Java >= 1.6

On root folder (GDuplicateFinder) run gradle distZip.
Script will automatically detect your system's os.
If you want to change it's value with -Pos=$osname
where $osname can be one of the following:
windows32, windows64, linux32, linux64, maccocoa32 or maccocoa64.

How to run
----------
Requires Java >= 1.6

Get the build corresponding to your system from https://sourceforge.net/projects/gdupfinder/files/ .
Unzip FileDuplicateFinderUI-1.0.zip.
If you are on windows, run FileDuplicateFinderUI.bat on the bin directory.
If you are not on windows, run FileDuplicateFinderUI on the bin directory.

##Troubleshooting

#### Linux

If you are getting 'Unable to locate theme engine in module_path: "pixmap" '
you should install gtk2-engines-pixbuf package.
On an Ubuntu distribution just run "sudo apt-get install gtk2-engines-pixbuf"


