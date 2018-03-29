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
- Dir scan caching via Java Serialization based on FileMDate+FileSize, one cache file by romdir, reusable between Dat if same romdir
- Split Fix/Rebuild
- SHA1 optional scan (instead of CRC32 + FileSize comparison)
- Retain SHA1 in cache until zip file change
- CHD support
- Per profile settings (what to fix, how to scan, ...)
- MultiThreading support (at least for archive manipulation and checksum calculation)

## Short Term Planned Features
- Better Dat Managing (with gui)
- Multiple Source Dir
- Merged Fix/Rebuild
- Non-Merged Fix/Rebuild
- Software List support
- General settings
- Enhanced Gui with Report

## Middle Term Planned Features
- 7z support (at least decompression)
- TorrentZip support
- Dir2Dat, Dat2Dat

## Long Term Planned Features and Ideas
- Other Dat formats
- Interoperability with other managers?
- Batch mode?

