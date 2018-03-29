# JRomManager

A Rom Manager entirely written in Java and released under GPL v3

## Technical
_Minimal developement requirements_:
- Eclipse Oxygen for Java with WindowBuilder feature
- Java 8
- Apache Commons Codec 1.11
- Apache Commons IO 2.6
- Apache Commons Lang3 3.7

_Minimal usage requirements_:
- 1GB Free Ram (2GB or more with Software Lists and/or MultiCore feature)
- Any OS with Java 8 runtime

## Current Features
- Zip support
- Mame Dat format
- Import from Mame executable
- Dat parsing caching via Java Serialization
- Optimized to permit scan over shared network
- Dir scan caching via Java Serialization based on FileMDate+FileSize, one cache file per romdir, reusable between different Dat Scans if same dir used
- Split Scan and Fix/Rebuild
- Full SHA1 scan or on-need SHA1 scan in case of suspicious CRC32
- Retain SHA1 in cache until zip file change
- CHD support (no separate dir from roms yet)
- Per profile settings (what to fix, how to scan, ...)
- MultiThreading support (at least for archive manipulation and checksum calculation, fast disks required)
- MD5 support (for old dats)

## Short Term Planned Features
- Better Dat Managing (with gui)
- Multiple Source Dir
- Merged Fix/Rebuild
- Non-Merged Fix/Rebuild
- Software List support
- General settings
- Enhanced Gui with Report

## Middle Term Planned Features
- 7z support
- TorrentZip support
- Dir2Dat, Dat2Dat

## Long Term Planned Features and Ideas
- Other Dat formats
- Interoperability with other managers?
- Torrent7Z support ?
- Batch mode?

