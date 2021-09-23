#!/usr/bin/env bash

cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
set -e -x -u

java -version
node -v
npm -v
env | sort

which java
ls -lah "$(which java)"

chmod +x ./gradlew
./gradlew tasks

./gradlew \
    kotlinNodeJsSetup \
    kotlinNpmCachesSetup \
    kotlinNpmInstall \
    kotlinYarnSetup \
    fullDistBuild

./gradlew --stop


