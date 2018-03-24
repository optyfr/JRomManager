# JRomManager

A Rom Manager entirely written in Java

## Current Features
- Zip support
- Mame Dat format
- Import from Mame executable
- Dat parsing caching via Java Serialization
- Optimized to permit scan over shared network
- Dir scan caching via Java Serialization based on FileMDate+FileSize, one cache file by romdir, reusable between Dat if same romdir
- Split Fix/Rebuild

## Short Term Planned Features
- SHA1 optional scan (instead of CRC32 + FileSize comparison)
- Retain SHA1 in cache until zip file change
- Better Dat Managing (with gui)
- Multiple Source Dir
- Merged Fix/Rebuild
- Non-Merged Fix/Rebuild
- CHD support
- Software List support
- More options (what to fix, how to scan, ...)
- Enhanced Gui with Report

## Middle Term Planned Features
- 7z support (at least decompression)
- Dir2Dat, Dat2Dat
- Multi Core support (at least for archive manipulation and checksum calculation)

## Long Term Planned Features and Ideas
- Other Dat formats
- Interoperability with other managers?
- Batch mode?
