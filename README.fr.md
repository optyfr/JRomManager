# JRomManager

Un gestionnaire de Roms entièrement écrit en Java et distribué sous la license GPL v3

## Licenses
- **GPL-2** : JRomManager, JUpdater, JLauncher
- **GPL-2 avec l'exception classpath** : zipfs (tiré de Oracle OpenJDK9 )
- **MIT** : Jtrrntzip (basé sur trrntZipDN par Gordon J), commonmark (par Atlassian), minimal-json (par Ralf Sternberg)
- **LGPL 2.1 + restriction unRAR** : SevenZipJBinding (par Boris Brodski)
- **Apache 2.0** : StreamEx (par Tagir Valeev), Gradle Wrapper, et toutes les librairies "Apache commons"
- **BSD 3** : NanoHTTPD

## Technique
_Prérequis de développement minimum_:
- Eclipse Oxygen pour Java avec le module WindowBuilder (module standard d'Eclipse) et Gradle Buildship (dans Eclipse Marketplace)
- Java SDK 8
- Dependances Gradle (via dépôts Maven)
	- Apache Commons Codec 1.+ 
	- Apache Commons CLI 1.+ 
	- Apache Commons IO 2.+
	- Apache Commons Lang3 3.+
	- Apache Commons Text 1.+
	- Apache Commons Compress 1.+ (utilisé uniquement pour lister le contenu des fichiers 7zip)
	- StreamEx 0.7.+
	- SevenZipJBinding 16.20-2.01 (plus rapide que l'usage de la commande en ligne de 7zip)
	- NanoHTTPD 2.+
- Dépendances via sous-modules Git
	- [Jtrrntzip](https://github.com/optyfr/Jtrrntzip)
	- [JUpdater](https://github.com/optyfr/JUpdater)
	- [JLauncher](https://github.com/optyfr/JLauncher)
	- [JRomManager-WebClient](https://github.com/optyfr/JRomManager-WebClient)

_Prérequis d'utilisation minimum_:
- 1Go de Ram libre (2Go ou plus si utilisation des Software Lists, l'option multicoeur, la compression 7z en mode ultra, ...)
- Tout OS avec au moins Java 8 installé (version 64 bits requise pour avoir plus de 1Go)
- (optionnel) les programmes 7zip ou p7zip en ligne de commande si vous avez besoin de 7z et uniquement si SevenZipJBinding ne fonctionne sur votre plateforme
- ~~(optionnel) le programme trrntzip si vous voulez torrentzipper vos fichiers~~ (désormais intégré avec jtrrntzip)

_Comportement comparé aux autres gestionnaires de Roms_
- Par défaut, le mode Split (séparé) peut être différent de ClrMamePro, c'est parce que JRomManager est respecteux de l'attribut "merge", alors que ClrMamePro va splitter (separer) dès qu'une rom est dans un set parent avec le même CRC même si aucun attribut "merge" a été initialisé! La difference est spécialement visible pour les Software Lists où l'attribut merge n'existe pas du tout dans le DTD, donc dans ce cas le mode "Split Merged" sera le même que d'utiliser le mode "Non Merged" pour JRomManager. RomCenter est également respectueux de l'attribut merge. Pour reproduire le même comportement que ClrMamePro vous devrez donc selectionner l'option "implicit merge" (fusionnage implicite) dans les préférences du profil courant

_Instructions minimales pour la compilation en ligne de commande_:  

Si vous voulez juste recompiler les sources sans utiliser un IDE (Eclipse), voici les étapes à suivre...
- D'abord, vous aurez besoin de Java JDK 8 installé à partir du gestionnaire de package de votre système ou à partir de l'installateur officiel, ou au moins vous assurer que tous les binaires de votre java jdk seront accessibles à partir de votre $PATH ou %PATH% courant
  - [Requis] Si vous êtes sous Windows, vous aurez aussi besoin de Visual C++ 2017 installé, lequel sera nécessaire pour générer l'executable JLauncher (le plugin natif gradle detectera Visual C++ installé et génèrera en utilisant directement ce compilateur VC++)
  - [Optionnel] Si vous êtes sous Windows, et que vous voulez générer l'installateur MSI, vous aurez besoin du dernier WIX Toolset installé ainsi que son répertoire bin/ ajouté dans votre %PATH% (pas fait automatiquement à son installation) 
- Téléchargez et désarchivez `https://github.com/optyfr/JRomManager/releases/download/<version>/JRomManager-<version>.src.tar.gz` (utilisez sevenzip sous Windows pour ce faire); **ne pas télécharger `Source Code (zip)` ou `Source Code (tar.gz)`, ceux ci étant automatiquement générés par github et malheureusement ne contiennent pas du tout les sources des sous-modules requis**
- `cd JRomManager-<version>`
- lancer
  - Unix: `sh ./gradlew build`
  - Windows: `.\gradlew.bat build`
- Il s'agit du gradle-wrapper inclus, il téléchargera la bonne version de package binaire de gradle, puis compilera et empaquetera tout (void les sous-répertoires "build")

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
- Mise à jour facile en un clic
- Installer Jar

## Fonctionalités planifiées à court terme
- Créer des paquets binaires pour les diverses distributions Linux et les rendre disponibles dans leurs dépots (si possible)
- Créer un executable pour windows qui recherche un jre disponible, et adapte les arguments de lancement selon la version 32 ou 64 bits 
- Documentation des sources (+ génération javadoc)
- Clarifier les problèmes de licence
- Option de mise à jour automatique (et montre le rapport des changements après la mise à jour)

## Fonctionalités planifiées à moyen terme
- Dir2Dat
- Plus de traductions
- Filtrage plus fin des machines dans le visualiseur de profil (pour mode 1G1R, ...)

## Fonctionalités planifiées sur le long terme et idées
- Interopérabilité avec d'autres gestionnaires?
- Support de Torrent7Z ?
- Mode Batch?
- Gestion des Roms avec entête?

## Fonctionalités non envisagées
- Support des CRC inversés/complémentés: obsolète remplacé par l'attribut "status" depuis Mame 0.68
- Ancien format non XML de ClrMamePro: considéré comme déprécié par l'auteur de ClrMamePro lui même
- CHD verification: les données ne seront pas vérifiées, seule l'entête du CHD est lue pour obtenir le md5/sha1, peut être que la taille des données sera verifié ultérieuremnet mais pas de hash ni de chdman (impliquerait une dépendance binaire ou de le reecrire en java)
- Retaillage des Roms

## Problèmes connus
- ~~N'essayez jamais de lancer de multiples instances de JRomManager, car 7zJBinding a un problème avec ça, et aussi parce que vous pourriez avoir d'étranges comportements~~ *A été mis en oeuvre un dispositif pour empêcher de lancer plus d'une instance*
- JRomManager est encore en phase de developpement précoce, ne venez donc pas vous plaindre si votre jeu de roms a été cassé et que vous n'aviez pas fait de backup
- la fonctionnalité Zip intégrée dans java peut laisser des entrées de répertoire inutiles dans l'index du zip, c'est un fonctionnement considéré normal, et de plus c'est l'implémentation la plus rapide disponible, ça ne va pas déranger Mame ou n'importe quel autre émulateur, mais clrmamepro se plaindra de répertoires inutiles, et de plus trrntzip retirera ces entrées commen attendu :wink:
