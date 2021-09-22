#!/usr/bin/env bash

cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
set -e -x -u

DIR=$(pwd)
BUILD_DIR="$DIR/build-local"

rm -rf "$BUILD_DIR" || true
mkdir "$BUILD_DIR" || true

go version

NAME=lambda-reception
go build -o "$BUILD_DIR/$NAME" s3.go settings.go s3-debug.go

export KTJS_BUCKET=jetsite-kotlin-js-html-hackathon21-kt-js-hackathon21
export AWS_REGION=eu-central-1

"$BUILD_DIR/$NAME"


