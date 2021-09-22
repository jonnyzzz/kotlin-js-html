#!/usr/bin/env bash

set -e -x -u

rm -rf build || true
mkdir build || true

go version

GOOS=linux go build -o build/lambda-recetion main.go

