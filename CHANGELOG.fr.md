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
- Option pour ignorer les fichiers inconnus
- Option pour ignorer les archives inutiles
- Option pour ignorer les entrées d'archives inutiles
- Option pour faire du fusionnage implicite (comme ClrMamePro)
- Drag & Drop sur les contrôles des répertoires sources/destinations
- Ajout d'un filtre de lignes inutiles à l'import xml à partir de l'executable MAME
- Code supplémentaire pour gérer les DATs malformés ou incomplets
- Correction de bogues
## Release v1.1 build 3
- Ajout du support du filtre
- Correction de bogues
## Release v1.0 build 2
- Version initiale
