#!/bin/bash

DATE=$(date +%Y%m%d-%H%M%S)
DIRNAME=crowd-sourcing
FILENAME=$DIRNAME-$DATE.zip

pushd ..
zip -v -r -9 --exclude=*.DS_Store* $FILENAME $DIRNAME
popd
