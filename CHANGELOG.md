## Release v3.0.0 beta 5
- docker images support

## Release v3.0.0 beta 4
- logback-classic was missing in all the arch packaged versions (so no log output)

## Release v3.0.0 beta 3
- WebClient was not found when not using module version (case of zip package)
- Some bugs fixed in WebClient, mainly because of misinterpretation of doc in SmartGWT for datasource's request customization
- Provided scripts to install server versions with systemd

## Release v3.0.0 beta 2
- fixed broken torrentzip
- fixed sha1 scan miss (whatever rom is suspicious or unconditional sha1 calculation was enabled)

## Release v3.0.0 beta 1
- 3.x milestone is an attempt to simplify the whole project since it was becoming more and more unmanageable
- Java 17 is now required
- JavaFX 17 gui is the new desktop standard interface, as a replacement of the outdated swing interface
- src archive is removed, to compile from sources => ``git clone --recursive https://github.com/optyfr/JRomManager.git``
- modularization (jigsaw) has been dropped, it give far more problems and constraints than features
- zip4j is replacing the hacked Zip File System Provider, it should be able to add/delete entries into a zip without overall archive recompression
- possibility to skip during scan dirs or files using glob list (not yet functional)
- Warning : No desktop gui for noarch/all/zip builds since javafx is hardware/os dependent, you'll have to use arch specific packages/installers, for uncommon os/hardware it'll be web or cli mode

## Release v2.5.0
- Java 11 is now a minimal source/target requirement
- Dropped obsolete/unmaintained NanoHTTPd, and using only Jetty
- Modularized code and dependencies to bundle inside a designated java runtime that target some specific platforms (currently JRE11 + Windows_64)
- Rebranded MSI installer for windows (using jpackage from Java 14+)
- More informative about what's invalid in a datfile when it can't be loaded

## Release v2.4.4
- Dummy release because 2.4.3 packages were build with wrong jdk...

## Release v2.4.3
- Property fixes with WebClient
- Better RemoteFileChooser handling for BatchTools in WebClient

## Release v2.4.2
- Fixed "dry run" bug for server mode
- Certs dir was missing (was only in zip version)
- Rescan after fix in batch mode

## Release v2.4.1
- Fixed wrong packaging for webclient
- Included a self signed "localhost" certificate for full server mode HTTPS

## Release v2.4.0
- Added full server mode
    - Made for an usage over the Internet (but it should be kept legal between users)
    - Jetty is used instead of NanoHTTPd
    - Multi-user with access rights, a per user WorkDir, a shared read-only space, and no access to the rest of the entire FileSystem
    - Totally secured (separate accounts with login/password, HTTPS with TLS 1.3, server certificate handling and auto reload, obfuscated paths, ...)
    - HTTP2 support (only with TLS 1.2) which permit long polling request usage in place of WebSockets without loosing too much network performance
    - This mode is still **experimental**, work still need to be made on disk/thread quotas, and a firewall with IP filtering may be required to get full control on who is attempting to connect 
- Implemented Long polling Request as alternative of WebSockets for simple server mode
- New "Single File" mode, made for single ROM per game sets than can be kept in base directory without creating a sub-directory for each game...
- Added support for import/export of settings from one profile to another
- More graphical icons for tabs and buttons
- Various fixes
- **This will be the last version supporting Java 8, next one will require at least Java 11**

## Release v2.3.0
- Added "Copy CRC/SHA1/Name" menu entries on entities in Report views and in ProfileViewer
- Added "Detail" menu on entries in Report views
- Added "Search on the Web" menu on entries in Report views and in ProfileViewer
- Fixed Torrent Check report problem
- Reuse back new latest SevenZipJBinding from official repository 
- Various fixes and sanitizations

## Release v2.2.0
- Using custom sevenzipjbinding which include more architectures/os, and a more recent sevenzip version
- RAR5 extraction (as per sevenzipjbinding update)
- CLI version
    - include access to all functionalities and more
    - support for environment variables (replacement) and java system properties (replacement, set, and clear)
- Modularized building which permit special packages for server and cli excluding all code from standalone interface (swing)
    - Most of the code were refactored and reorganized to get a total separation from core and standalone code

## Release v2.1.0
- RAR4 extraction
- RAR4 and 7Zip multi-volume extraction
- Included ARM for sevenzipjbinding
- Fully functional on Raspberry PI
- Service is now configured for a maximum of 75% percentage of memory instead of a fixed size of 4GB, this should resolve launching problems on low memory devices
- Better accuracy of progress bars and times estimations (now also according data size to process)
- Better multiprocessor scheduler : tasks are sorted first then distributed dynamically (instead of using a spliterator)
- Many fixes and speed enhancements

## Release v2.0.4
- Fixed tzip marked as needed when an archive is partially fixable but we asked to build only fully fixable

## Release v2.0.3
- Fixed clones subdirs in merged mode when destination format is DIR
- Fixed dir->7z and 7z->any
- Fixed problem in the way we fix wrong named entries in some cases

## Release v2.0.2
- Fixed 7z archive extraction

## Release v2.0.1
- Fixed a launch problem without debug mode
- Added Mac OS X DMG package

## Release v2.0.0
- New naming scheme
- Batch Compressor : to compress many files at a time from any format to Zip, TrntZip, or 7ZIP 
- Server mode + Web Client (EXPERIMENTAL).
    - JRomManager can listen to a defined port and serve a web interface instead of the classical Swing gui
    - The initial purpose of the server mode is to run JRomManager on headless server and directly on NAS
    - The Web interface is full feature complete, and very powerful, thanks to SmartGWT from isomorphic
    - This feature is **experimental**, it means that :
        - It has not been tested over the internet => the purpose is to access from intranet
        - It's a simple web server with basic websockets support => proxies may not work, no encrypted connection (https/wss), no protection against DDOS, no connection limitations, ...
        - There is currently no multiuser support nor access control implemented => you can break your server easily if you don't know what you're doing
    - See the [wiki](https://github.com/optyfr/JRomManager/wiki/Server-mode#server-mode) for more informations on server mode and web client
- Fixed [issue #17](https://github.com/optyfr/JRomManager/issues/17)
- Various fixes (and maybe new bugs?) implied by some code rework for server mode

## Release v1.9 build 29
- Enhancements  
    - Custom scan settings for dir updater batch tool
    - Details and Report lists in Batch dir updater
    - Torrent checker is able to detect wrongly archived folders and unzip them
    - Created our own overlay for Gentoo Linux, releases will be in sync for this distribution :
        - add the overlay using command `layman -o https://raw.githubusercontent.com/optyfr/jrommanager-gentoo/master/overlay.xml -f -a jrommanager`
        - to keep in sync use `layman -s jrommanager`
- Minor enhancements  
    - Torrent checker can now delete useless files and wrong files  
    - Report log now include datetime in its name and does not overwrite the former log  
    - Report log now include informations about originating dat/xml file and used scan settings 
    - Added file selectors to Dir Updater and Torrent checker (drag & drop alternative)
- Fixes  
    - Added tooltips in table headers (batch tools and profile viewer)
    - Bad Zip64 reading header
    - Performance problem with machines with lot of roms (and so archives with lot of entries to compare to)

## Release v1.8 build 28
- Fixes
    - Edge case found in scanner when two roms in same set need to be swapped (was resulting in removing one of the roms)
    - Does not report wrong hash entry when it is fixable (and will be fixed), so that "dry run" mode return good fixed count
    - More explicit result when torrent checking is successful
    - Made multithreaded progress interface more robust, because it may be wrong under Linux as thread IDs are not reused like on windows 

## Release v1.8 build 27
- Fixes
    - pop-up menu was missing in torrent checker, so that deleting entries was impossible
    - torrent checking was unstoppable
    - out of <workdir> dats (batch tools case) won't have anymore their cache, properties, and nfo files saved with them but instead they will be stored in <workdir>/work special dir
- Minor enhancements
    - Multithreading enabled for torrent checker
    - Individual selection for torrent check and directories updater
    - added hints in batch tools tables

## Release v1.8 build 26
- Dir2Dat
- Batch Tools
    - Dir Updater : to update many dats files to many dirs. Accept dirs of dats as source (software list mode)
    - Torrent checker : to check many torrent to many dirs. From file name mode to sha1 mode

## Release v1.7 build 24
- Executable launcher for windows (using [JLauncher](https://github.com/optyfr/JLauncher) project)
- Documented source code and javadoc archive available
- Available in Gentoo Linux repository 
- Fixed licensing issues (with the help of Michał Górny from Gentoo)
- Fixed problem with SevenzipJBinding initialization temporary directory 

## Release v1.6 build 20
- Machine/Software can individually (de)selected in Profile Viewer, this will impact the scan and filtered export
- Possibility to auto(de)select by keywords,
    - keywords list is built with all terms between parenthesis in each description
    - keywords selection interface permit to choose which keywords is accepted and by order of priority
    - autoselection will do the best to select only one game with the same name without keywords, and based upon keywords preference order (so that 1G1R and more should be possible this way)
- backup profile option : Each deleted rom will be stored in a backup folder which is reused upon future scans, so that roms are never loose (but eat some disk space)
- load device descriptions from profile to launch softwares with the correct media
- there is now 2 launch options (made for packaging via official linux distribution tools) :
    - --multiuser : set working folder inside home directory instead of app directory
    - --noupdate : disable search for update 
- documentation available on [github wiki](https://github.com/optyfr/JRomManager/wiki)
- fixed wrongly unneeded files when software list is present in profile
- fixed problem with progress bar when thread id changed in between two parallel streams
- fixed stupid problem with dat containing uppercase crc/sha1/md5 (thanks go to SpaceAgeHero)
- .deb package available with [github releases](https://github.com/optyfr/JRomManager/releases) for Debian and Ubuntu
- package for ArchLinux available on [AUR](https://aur.archlinux.org/packages/jrommanager/)

## Release v1.5 build 18
- New progress bar which show multithreaded operations separately
- Added intermediate progression for fix operations and TorrentZipping
- HalfDumb mode (also known as PD Mame merged mode), works a bit better
but still only 99.8% torrent join
- Rom reading order is now kept
- Added menu shortcut Presets on scanner settings panel
- new Non-Merge mode which include devices (PD Mame Non-Merged mode)
- new options to exclude games and machines and to generate bios/devices only sets, or machine/bios/devices only sets
- network access fix for zipfilesystem
- Fixed status in profile viewer (again) for files with same crc in same
dir but one is not present (should be green)
- added an extra "media" argument on command line to launch a software available for the same computer in multiple format (floppy, cartridge, tape, ...)
- avoid extra torrentzip check when not needed
- don't create empty folders for software list until we find roms or chds to put in 
- chd files were not anymore refreshed in profile viewer
- New merge hash collision option called that will also make subirs for clone for chds
- JUpdater does not cause fail and quit anymore when connect timeout on update url
- JUpdater is now copying attributes, that should resolve problem encountered to launch .sh

## Release v1.4 build 16
- ZipFileSystem class from JRE has problem with file modification over shared network, it pretends that archive file is not writable while it is, so it's time to duplicate Oracle's code into JRomManager... and patch it!
- As a consequence ZipFileSystem has also been enhanced to support compression level (from STORE[0] to ULTRA[9]) instead of the DEFAULT[-1] compression
- It is also now possible to choose a size threshold where ZipFileSystem will choose between uncompress to RAM or to temporary folder  
- Now that we have full control on ZipFileSystem behavior, ZIP compression level will be automatically set to 1 prior TorrentZipping
- When launching a software from Profile Viewer you will have choice of the machine (sorted by compatibility level)
- Samples are also TorrentZipped now
- Fixed an out of bound stream bug with Jtrrntzip when reading STORE files from archive (that was returning a CorruptZip code)

## Release v1.4 build 15
- Added update check at start with ChangeLog diff (from current to latest) shown in a MessageDialog (English only)
- Added one-click updater: just click the link in the MessageDialog from update checker, and it will download and update by itself, then restart using [JUpdater](https://github.com/optyfr/JUpdater)
- Added Jar Installer: just download the Jar version from [github](https://github.com/optyfr/JRomManager/releases/latest) and run it with Java
- Added System's filter new shortcuts menu
- Fixed software list refresh bug in profile viewer
- Fixed special case of ROMs with no name (and with loadflag)

## Release v1.3 build 13
- Resources folders were missing in jar

## Release v1.3 build 12
- Unknown containers checking was not filtered
- Code is now compilable with jdk 9/10 (but still jdk 8 compatible)
- Migrated java eclipse project to gradle nature project (no more manual download of dependencies needed)

## Release v1.3 build 10
- Export to logiqx datafile, Mame dat, Mame SW List
- Export all/selected + filtered/unfiltered
- Basic filter by Min/Max year
- Advanced filtering:
    - Filter by categories/subcategories using catver.ini (with an extra menu shortcut for mature exclusion/inclusion)
    - Filter by nb of players using nplayers.ini
- More Drag & Drop
- Added Samples support
- Various code cleanups and optimizations

## Release v1.2 build 8
- When Importing Software List, Roms will be also imported... As a consequence:
    - Roms are displayed with software lists in profile viewer
    - There is a jrm profile format (xml inside), that tell where to find the two mame xml files (roms + swlists)
    - Roms ans SWLists are scanned at the same time
    - There is now an optional software dir and an even more optional software disks dir
- Double click on game name in profile viewer will launch game if (Mame is linked with profile and item is green)
- Double click on software name in profile viewer will launch software with a valid machine (if Mame is linked with profile and item is green)
- Double click on cloneof or romof item in profile viewer will jump to that item definition
- Search field in profile viewer
- Complete profile manager with stats
- TorrentZip support is now integrated and optimized (with [Jtrrntzip](https://github.com/optyfr/Jtrrntzip))
- Optional Separate Dest dir between Roms, CHDs, Software Roms, Software CHDs
- Option to ignore unknown containers
- Option to ignore unneeded containers
- Option to ignore unneeded entries
- Option to do implicit merging (like ClrMamePro)
- Drag & Drop on src/dest dirs controls
- Added garbage filter when importing xml from mame executable
- Extra code to handle bad or incomplete Dats
- Bug Fixes

## Release v1.1 build 3
- Added filter support
- Bug fixes

## Release v1.0 build 2
- Initial release
