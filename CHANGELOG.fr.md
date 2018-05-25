## Release v1.4 build 15
- Ajout de la vérification de la disponibilité d'une mise à jour au démarrage avec Historique des changement (entre la version actuelle et la dernière) le tout affiché dans boite de dialogue
- Ajout de la mise à jour en un clic: cliquez juste sur le lien dans la boite de dialogue de la recherche de mise à jour, et ça va télécharger et mettre à jour tout seul, puis redémarrer en utilisant [JUpdater](https://github.com/optyfr/JUpdater)
- Ajout d'un installateur JAR : téléchargez juste la version JAR à partir de [github](https://github.com/optyfr/JRomManager/releases/latest) et executez là avec votre runtime java
- Ajout de new raccourcis de menu dans les filtres Systemes
- Correction d'un bug de rafraichissement des listes de software dans la visionneuse de profil
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
