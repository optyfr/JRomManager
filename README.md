# JRomManager

A Rom Manager entirely written in Java and released under GPL v3

## Technical
_Minimal developement requirements_:
- Eclipse Oxygen for Java with WindowBuilder feature
- Java 8
- Apache Commons Codec 1.11 
- Apache Commons IO 2.6
- Apache Commons Lang3 3.7
- Apache Commons Text 1.3
- Apache Commons Compress 1.16 (used solely to list 7zip content)
- StreamEx 0.6.6
- SevenZipJBinding 9.20-2.00 (faster than using 7z cmd line)

_Minimal usage requirements_:
- 1GB Free Ram (2GB or more with Software Lists, MultiCore feature, 7z ultra compression, ...)
- Any OS with Java 8 runtime
- (optional) 7zip or p7zip cmdline program if you need 7z format and only if SevenZipJBinding doesn't work on your platform
- (optional) trrntzip cmdline program if you want to torrentzip your files

_Behavior compared to other Rom managers_
- Split mode may differ from ClrMamePro, it's because JRomManager is respectful of the merge attribute, whereas ClrMamePro will split as soon as a rom in parent set as same CRC even if no merge attribute as been set! The difference is especially visible for Software Lists where merge flag does not exist at all in the DTD (and that's not innocent), so in this case "Split Merged" mode will be the same as using "Non Merged" mode for JRomManager. RomCenter is also respectful of the merge attribute

## Current Features
- Zip support
- Mame and Logiqx Dat formats
- Import from Mame executable
- Dat parsing caching via Java Serialization
- Optimized to permit scan over shared network
- Dir scan caching via Java Serialization based on FileMDate+FileSize, one cache file per romdir, reusable between different Dat Scans if same dir used
- Split Scan/Fix/Rebuild
- Non-Merged Scan/Fix/Rebuild
- Merged Scan/Fix/Rebuild (with choice in case of name collision)
- Full SHA1 scan or on-need SHA1 scan in case of suspicious CRC32
- Retain SHA1 in cache until zip file change
- CHD support (no separate dir from roms yet)
- Per profile settings (what to fix, how to scan, ...)
- MultiThreading support (at least for archive manipulation and checksum calculation, fast disks required)
- MD5 support (for old dats)
- 7z support via SevenZipJBinding + 7z command line as functional backup
- TorrentZip support via trrntzip command line
- Multiple Source Dir
- Enhanced Gui with Report
- Translated to English and French
- Software List support

## Short Term Planned Features
- Better Dat Managing (with gui)
- Optional Separate Dest dir between Roms and CHDs
- Samples support (with separate Dest dir that may be eventually a subdir of Roms dir)
- Exclude CHDs and Samples scanning
- General settings
- Filtering functionalities (clone, machine, ...)

## Middle Term Planned Features
- Dir2Dat, Dat2Dat
- More Translations

## Long Term Planned Features and Ideas
- Interoperability with other managers?
- Torrent7Z support ?
- Batch mode?

## Not planned features
- Inverted/complemented CRC: obsolete replaced by "status" attribute since Mame 0.68
- Old ClrMamePro non-xml format: even considered as deprecated by ClrMamePro
- CHD verification: data won't be verified, only header is read to get md5/sha1, maybe the data size will be verified later but no hashing nor chdman
- Rom resizing

## Known issues
- ~~Never ever launch multiple instances of JRomManager, because 7zJBinding does have problem with this , and also because you may get stranges other things~~ *Prevented app to launch twice*
- JRomManager is still in early developpement stage, do not complain if it broke your romset and you didn't make any copy or backup
- Zip integrated in java leaves useless directory entries in zip central directory, it is by design, and this implementation is the fastest available, this will not disturb Mame or any emulator, but clrmamepro will complain about useless dirs, and trrntzip will remove those entries as expected :wink:
