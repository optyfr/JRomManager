## Release v1.9 build 29
- Enhancements  
	- Custom scan settings for dir updater batch tool  
- Minor enhancements  
	- Torrent check can now delete useless files and wrong files  
	- Report log now include datetime in its name and does not overwrite the former log  
	- Report log now include informations about originating dat/xml file and used scan settings  
- Fixes  
	- Added tooltips in table headers (batch tools and profile viewer)

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
