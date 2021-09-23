#!/usr/bin/env bash

cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
set -e -x -u

rm -rf build || true
mkdir build || true

go version

NAME=lambda-get
GOOS=linux GOARCH=amd64 go build -o "build/$NAME" *.go

cd build
zip "$NAME.zip" -- *
cd ..

