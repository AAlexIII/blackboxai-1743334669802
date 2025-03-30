#!/bin/bash

# Создаем необходимые директории
mkdir -p app/src/main/java/com/useractionrecorder
mkdir -p app/src/main/res/anim
mkdir -p app/src/main/res/layout
mkdir -p app/src/main/res/values
mkdir -p app/src/main/res/values-night
mkdir -p app/src/main/res/xml

# Перемещаем файлы исходного кода
mv src/main/java/com/useractionrecorder/* app/src/main/java/com/useractionrecorder/

# Перемещаем ресурсы
mv res/anim/* app/src/main/res/anim/
mv res/layout/* app/src/main/res/layout/
mv res/values/* app/src/main/res/values/
mv res/values-night/* app/src/main/res/values-night/
mv res/xml/* app/src/main/res/xml/

# Перемещаем AndroidManifest.xml
mv AndroidManifest.xml app/src/main/

# Удаляем пустые директории
rm -rf src
rm -rf res