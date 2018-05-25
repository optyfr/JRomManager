# JRomManager

A Rom Manager entirely written in Java and released under GPL v3

## Technical
_Minimal developement requirements_:
- Eclipse Oxygen for Java with WindowBuilder feature and Gradle Buildship
- Java 8
- Gradle dependencies (via Maven repositories)
	- Apache Commons Codec 1.11 
	- Apache Commons IO 2.6
	- Apache Commons Lang3 3.7
	- Apache Commons Text 1.3
	- Apache Commons Compress 1.16 (used solely to list 7zip content)
	- StreamEx 0.6.6
	- SevenZipJBinding 9.20-2.00 (faster than using 7z cmd line)
- Git submodules dependencies
	- Jtrrntzip
	- JUpdater

_Minimal usage requirements_:
- 1GB Free Ram (2GB or more with Software Lists, MultiCore feature, 7z ultra compression, ...)
- Any OS with at least Java 8 runtime (64 bits version required to get more than 1GB)
- (optional) 7zip or p7zip cmdline program if you need 7z format and only if SevenZipJBinding doesn't work on your platform
- ~~(optional) trrntzip cmdline program if you want to torrentzip your files~~ *(now integrated with Jtrrntzip)*

_Behavior compared to other Rom managers_
- By default, Split mode may differ from ClrMamePro, it's because JRomManager is by default using "explicit merging" and so will split only according the merge attribute presence (as preconised in logiqx faq), whereas ClrMamePro will split as soon as a rom in parent set as same CRC even if no merge attribute as been set! The difference is especially visible for Software Lists where merge flag does not exist at all in the DTD, so in this case "Split Merged" mode will be the same as using "Non Merged" mode for JRomManager. RomCenter is also known to be respectful of the merge attribute. To reproduce the ClrMamePro behavior you will have to select "implicit merge" option in profile settings

## Current Features
- Mame and Logiqx Dat formats
- Import from Mame executable
- Dat manager with stats and mame update check (if imported from mame)
- Zip support
- Split Scan/Fix/Rebuild
- Non-Merged Scan/Fix/Rebuild
- Merged Scan/Fix/Rebuild (with choice in case of name collision)
- Full SHA1 scan or on-need SHA1 scan in case of suspicious CRC32
- Retain SHA1 in cache until zip file change
- CHD support
- Software List support
- Per profile settings (what to fix, how to scan, ...)
- MultiThreading support (at least for archive manipulation and checksum calculation, fast disks required)
- MD5 support (for old dats)
- 7z support via SevenZipJBinding + 7z command line as functional backup
- TorrentZip support using [Jtrrntzip](https://github.com/optyfr/Jtrrntzip)
- Samples support (with separate Dest dir that may be eventually a subdir of Roms dir)
- Multiple Source Dir
- Dat parsing caching via Java Serialization
- Optimized to permit scan over shared network
- Dir scan caching via Java Serialization based on FileMDate+FileSize, one cache file per romdir, reusable between different Dat Scans if same dir used
- Enhanced Gui with Report
- Translated to English and French
- Filtering functionalities (clone, chds, systems list, display mode, cabinet type, driver status, ...)
- Optional Separate Dest dir between Roms, CHDs, Software Roms, Software CHDs
- Drag & Drop on src/dest dirs controls
- Double click on game name in profile viewer will launch game if (Mame is linked with profile and item is green)
- Double click on software name in profile viewer will launch software with a valid machine (if Mame is linked with profile and item is green)
- Double click on cloneof or romof item in profile viewer will jump to that item definition
- Advanced filtering functionalities when a nplayers.ini and catver.ini is associated with a mame profile
- Popup menu in profile viewer to export as dat (dat2dat)... selection mode : all or selected, filtering mode : filtered or unfiltered, format : logiqx/mame/softwarelist/softwarelists (according selection and/or profile context)
- One-click easy updater
- Jar installer

## Short Term Planned Features
- Auto update option (and show Changes log a after auto-update)

## Middle Term Planned Features
- Dir2Dat
- More Translations
- Fine grained machine filtering in profile viewer (for 1G1R, ...)

## Long Term Planned Features and Ideas
- Interoperability with other managers?
- Torrent7Z support ?
- Batch mode?
- Headered Roms support?

## Not planned features
- Inverted/complemented CRC: obsolete replaced by "status" attribute since Mame 0.68
- Old ClrMamePro non-xml format: even considered as deprecated by ClrMamePro
- CHD verification: data won't be verified, only header is read to get md5/sha1, maybe the data size will be verified later but no hashing nor chdman
- Rom resizing

## Known issues
- ~~Never ever launch multiple instances of JRomManager, because 7zJBinding does have problem with this , and also because you may get stranges other things~~ *(Prevented app to launch twice)*
- JRomManager is still in early developpement stage, do not complain if it broke your romset and you didn't make any copy or backup
- Zip integrated in java can leave useless directory entries in zip central directory, it is by design, and this implementation is the fastest available, this will not disturb Mame or any emulator, but clrmamepro will complain about useless dirs, and trrntzip will remove those entries as expected :wink:
