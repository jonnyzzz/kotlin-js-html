#!/usr/bin/env bash

cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
set -e -x -u

echo "Building lambda..."
../aws-reception-lamnda/build.sh


terraform apply

