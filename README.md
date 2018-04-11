# JRomManager

A Rom Manager entirely written in Java and released under GPL v3

## Technical
_Minimal developement requirements_:
- Eclipse Oxygen for Java with WindowBuilder feature
- Java 8
- Apache Commons Codec 1.11
- Apache Commons IO 2.6
- Apache Commons Lang3 3.7
- Apache Commons Compress 1.16
- StreamEx 0.6.6
- SevenZipJBinding 9.20-2.00

_Minimal usage requirements_:
- 1GB Free Ram (2GB or more with Software Lists and/or MultiCore feature)
- Any OS with Java 8 runtime
- (optional) 7zip or p7zip cmdline program if you need 7z format and only if SevenZipJBinding doesn't work on your platform
- (optional) trrntzip cmdline program if you want to torrentzip your files

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

## Short Term Planned Features
- Better Dat Managing (with gui)
- General settings
- Software List support
- Enhanced Gui with Report

## Middle Term Planned Features
- Dir2Dat, Dat2Dat

## Long Term Planned Features and Ideas
- Interoperability with other managers?
- Torrent7Z support ?
- Batch mode?

## Not planned features
- Inverted/complemented CRC: obsolete replaced by "status" attribute since Mame 0.68
- Old ClrMamePro non-xml format: even considered as deprecated by ClrMamePro
- CHD verification: data won't be verified, only header is read to get md5/sha1, maybe the data size will be verified later but no hashing nor chdman
- Rom resizing
