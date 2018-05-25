## Release v1.4 build 15
- Added update check at start with ChangeLog diff (from current to latest) shown in a MessageDialog (English only)
- Added one-click updater: just click the link in the MessageDialog from update checked, and it will download and update by itself, then restart using [JUpdater](https://github.com/optyfr/JUpdater)
- Added Jar Installer: just download the Jar version from [github](https://github.com/optyfr/JRomManager/releases/latest) and run it with Java
- Added System's filter new shortcuts menu
- Fixed software list refresh bug in profile viewer
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
