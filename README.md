#GDuplicateFinder - A **FREE** Groovy way to find file duplicates!

Unlike other duplicate finders such as Easy Duplicate File Finder,
Auslogics Duplicate File Finder, MindGems Fast Duplicate File Finder,
Duplicate Cleaner, DupFiles and others, GDuplicateFinder is a FREE cross-platform
application, with the ability to search among not just local files, but files on
the network, such as a Windows or a Linux share using VFS library.

Taking advantage of Groovy facilities and GPars power to process in parallel,
GDuplicateFinder will help you get rid of those duplicates you always
wanted to dispose in an easy and friendly way.

If you feel like contributing with code, you can always fork this [repo](https://github.com/guicamest/GDuplicate-Finder). Gradle will help
you configure the Eclipse project for you. You can also suggest improvements.

If you don't feel like coding but want to donate some money, you can do that using [paypal](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=fezuqqg9t6j6y).

## Current features

- Manage network drives(add, edit, remove)
- Turn on/off network drives
- Save/load duplicates results session
- Stop, save, load and resume a search for duplicates
- Open duplicate files with system's default editor
- Internationalization
- Update checker

## Network Shares supported

- SMB (Samba)
- SFTP / SCP (Connection via Ssh)
- FTP
- Amazon S3

## How to build
Required gradle version >= 1.0-rc-1

Requires Java >= 1.6

On root folder *GDuplicateFinder* run `gradle distZip`.
Script will automatically detect your system's os.
You can change the os by running `gradle -Pos=$osname`
where $osname can be one of the following:
windows32, windows64, linux32, linux64, maccocoa32 or maccocoa64.

## How to run

Requires Java >= 1.6

Get the build corresponding to your system from https://sourceforge.net/projects/gdupfinder/files/ 
and unzip GDuplicateFinder-2.0.0.zip

On windows run: `bin/FileDuplicateFinderUI.bat`

If you are on Mac or linux, run: `bin/FileDuplicateFinderUI`

##Troubleshooting

### Linux

If you are getting *Unable to locate theme engine in module_path: "pixmap"*
you should install **gtk2-engines-pixbuf** package.

On an Ubuntu distribution just run `sudo apt-get install gtk2-engines-pixbuf`

##ChangeLog

### Version 2.0.0

- Ability to run program with file to load search parameters / search session / duplicate results
- Improved duplicate files selection
- Save/load search parameters
- Added user experience improvement program
- AUTO-DELETE feature. Searches for duplicates, and when finished, removes all but one file in each duplicate group, keeping that copy

### Version 1.2.0

- Added Amazon S3 to supported network drives
- Added portuguese to internationalization
- Added notification when update is available
- Updated preferences dialog
- Fixed bug in searching files

### Version 1.1
- Improved finder algorithm

### Version 1.0

- Added internationalization support
- Manage network drives(add, edit, remove)
- Turn on/off network drives
- Save/load duplicates results session
- Stop, save, load and resume a search for duplicates
- Open duplicate files with system's default editor
- Internationalization ( english, spanish )
- Supported network drives(Smb, Scp, Ftp)


