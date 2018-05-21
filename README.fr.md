# JRomManager

Un gestionnaire de Roms entièrement écrit en Java et distribué sous la license GPL v3

## Technical
_Prérequis de développement minimum_:
- Eclipse Oxygen pour Java avec le module WindowBuilder (module standard d'Eclipse)
- Java SDK 8
- Apache Commons Codec 1.11 
- Apache Commons IO 2.6
- Apache Commons Lang3 3.7
- Apache Commons Text 1.3
- Apache Commons Compress 1.16 (utilisé uniquement pour lister le contenu des fichiers 7zip)
- StreamEx 0.6.6
- SevenZipJBinding 9.20-2.00 (plus rapide que l'usage de la commande en ligne 7z.exe)

_Prérequis d'utilisation minimum_:
- 1Go de Ram libre (2Go ou plus si utilisation des Software Lists, l'option multicoeur, la compression 7z en mode ultra, ...)
- Tout OS avec un Java 8 (minimum) installé
- (optionnel) les programmes 7zip ou p7zip en ligne de commande si vous avez besoin de 7z et uniquement si SevenZipJBinding ne fonctionne sur votre plateforme
- ~~(optionnel) le programme trrntzip si vous voulez torrentzipper vos fichiers~~ (désormais intégré via jtrrntzip)

_Comportement comparé aux autres gestionnaires de Roms_
- Par défaut, le mode Split (séparé) peut être différent de ClrMamePro, c'est parce que JRomManager est respecteux de l'attribut "merge", alors que ClrMamePro va splitter (separer) dès qu'une rom est dans un set parent avec le même CRC même si aucun attribut "merge" a été initialisé! La difference est spécialement visible pour les Software Lists où l'attribut merge n'existe pas du tout dans le DTD, donc dans ce cas le mode "Split Merged" sera le même que d'utiliser le mode "Non Merged" pour JRomManager. RomCenter est également respectueux de l'attribut merge. Pour reproduire le même comportement que ClrMamePro vous devrez donc selectionner l'option "implicit merge" (fusionnage implicite) dans les préférences du profil courant

## Fonctionalités actuelles
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

## Fonctionalités planifiées à court terme

## Fonctionalités planifiées à moyen terme
- Dir2Dat
- Plus de traductions

## Fonctionalités planifiées sur le long terme et idées
- Interopérabilité avec d'autres gestionnaires?
- Support de Torrent7Z ?
- Mode Batch?

## Fonctionalités non envisagées
- Support des CRC inversés/complémentés: obsolète remplacé par l'attribut "status" depuis Mame 0.68
- Ancien format non XML de ClrMamePro: considéré comme déprécié par l'auteur de ClrMamePro lui même
- CHD verification: les données ne seront pas vérifiées, seule l'entête du CHD est lue pour obtenir le md5/sha1, peut être que la taille des données sera verifié ultérieuremnet mais pas de hash ni de chdman (impliquerait une dépendance binaire ou de le reecrire en java)
- Retaillage des Roms

## Problèmes connus
- ~~N'essayez jamais de lancer de multiples instances de JRomManager, car 7zJBinding a un problème avec ça, et aussi parce que vous pourriez avoir d'étranges comportements~~ *A été mis en oeuvre un dispositif pour empêcher de lancer plus d'une instance*
- JRomManager est encore en phase de developpement précoce, ne venez donc pas vous plaindre si votre jeu de roms a été cassé et que vous n'aviez pas fait de backup
- la fonctionnalité Zip intégrée dans java peut laisser des entrées de répertoire inutiles dans l'index du zip, c'est un fonctionnement considéré normal, et de plus c'est l'implémentation la plus rapide disponible, ça ne va pas déranger Mame ou n'importe quel autre émulateur, mais clrmamepro se plaindra de répertoires inutiles, et de plus trrntzip retirera ces entrées commen attendu :wink:
