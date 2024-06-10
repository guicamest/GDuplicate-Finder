# GDuplicateFinder - A **FREE** way to find file duplicates!

Unlike other duplicate finders such as Easy Duplicate File Finder,
Auslogics Duplicate File Finder, MindGems Fast Duplicate File Finder,
Duplicate Cleaner, DupFiles and others, GDuplicateFinder is a FREE cross-platform
application, with the ability to search among not just local files, but also among
_different types_ of [Network Shares](#supported-network-shares)

Taking advantage of Kotlin facilities and coroutines power to process concurrently,
GDuplicateFinder will help you get rid of those duplicates you always
wanted to dispose in an _easy_, __fast__ and ___friendly___ way.

If you feel like contributing with code, you can always fork this [repo](https://github.com/guicamest/GDuplicate-Finder).
If you'd like to suggest improvements, create a `New issue` in the [Issues](https://github.com/guicamest/GDuplicate-Finder/issues) section.
If you'd like to report a bug, create a `New issue` in the [Issues](https://github.com/guicamest/GDuplicate-Finder/issues) section.

If you don't feel like coding but want to donate some money, you can do that using [paypal](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=fezuqqg9t6j6y).

## Current features

- Manage network drives(add, edit, remove)
- Turn on/off network drives
- Save/load duplicates results session
- Stop, save, load and resume a search for duplicates
- Open duplicate files with system's default editor
- Internationalization
- Update checker

## Supported Network Shares

- SMB (Samba)
- SFTP / SCP (Connection via Ssh)
- FTP
- Amazon S3

## How to run

_Requires Java >= 11_

Get the build corresponding to your system from https://sourceforge.net/projects/gdupfinder/files/
and `unzip GDuplicateFinder-2.0.3.zip`

On windows run: `bin/FileDuplicateFinderUI.bat`

If you are on Mac or linux, run: `bin/FileDuplicateFinderUI`

## Troubleshooting

### Linux

If you are getting *Unable to locate theme engine in module_path: "pixmap"*
you should install **gtk2-engines-pixbuf** package.

On an Ubuntu distribution just run `sudo apt-get install gtk2-engines-pixbuf`

### Cannot load 64-bit SWT libraries

If you are getting *Exception in thread "main" java.lang.UnsatisfiedLinkError: Cannot load 64-bit SWT libraries on 32-bit JVM*
it means your default java installation is 32-bit. You can verify it opening a console and typing *java -version*.
To fix it, either install 64-bit java and set it as default or download the 32-bit version of GDuplicateFinder.
For more info, follow [this thread](http://sourceforge.net/p/gdupfinder/discussion/general/thread/a1a82607/)

## Development

_Requires Java >= 11_

### Install jdk using SDKMAN!

If you are using [SDKMAN!](https://sdkman.io/) to manage your sdks, you can add a `.sdkmanrc` file to the root
(already added to .gitignore) with the following content:
```properties
java=11.0.20.1-tem # Version may mismatch, adjust to the sdk that you have installed
#java=17.0.7-tem # Version may mismatch, adjust to the sdk that you have installed
#java=21.0.1-tem # Version may mismatch, adjust to the sdk that you have installed

# You can also enable auto-env setting `sdkman_auto_env=true` config
# in [~/.sdkman/etc/config]. See https://sdkman.io/usage#config
```

And then simply run
```shell
sdk env install
```

### Build

On root folder *GDuplicateFinder* run `./gradlew distZip`.
The script will automatically detect your system's os.
You can change the os by running `./gradlew -Pos=$osname`
where $osname can be one of the following:
`windows32`, `windows64`, `linux32`, `linux64`, `maccocoa32` or `maccocoa64`.

## Donations

Thanks a lot to the following people for their donations to keep GDuplicateFinder alive!
- *Prezzy card holder* from New Zealand
- *Risto Ronkka* from Finland

