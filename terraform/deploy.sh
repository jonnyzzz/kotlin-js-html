#!/usr/bin/env bash

cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
set -e -x -u

if [[ "${1:-empty}" == *rec* ]] ; then
  echo "Building reception lambda..."
  ./../aws-reception-lambda/build.sh
fi

if [[ "${1:-empty}" == *build* ]] ; then
  echo "Building builder lambda..."
  ./../aws-builder-lambda/build.sh push
fi

terraform apply -auto-approve

