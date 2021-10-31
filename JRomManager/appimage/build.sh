#!/bin/bash

HERE="$(dirname "$(readlink -f "${0}")")"

pushd "$HERE/../build"

rm -rf AppDir
mkdir -p AppDir/usr
mkdir -p AppDir/opt/jrommanager

cp -rp jpackage/JRomManager/* AppDir/opt/jrommanager/
cp -p jpackage/JRomManager/lib/JRomManager.png AppDir/
cp -p "$HERE/jrommanager.desktop" AppDir/

cp -p "$HERE/AppRun" AppDir/
chmod +x AppDir/AppRun

rm -f *.AppImage
wget https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage
chmod +x appimagetool-x86_64.AppImage
./appimagetool-x86_64.AppImage -n -v AppDir

popd
