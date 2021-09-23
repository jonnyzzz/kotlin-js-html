#!/usr/bin/env bash

cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
set -e -x -u

rm -rf build || true
mkdir -p build || true
mkdir -p build/project || true

## copy placeholder
cp -R "../placeholder-projects" "build/runner"


go version

NAME=lambda-builder
GOOS=linux GOARCH=amd64 go build -o "build/$NAME" *.go

### this file is created via Terraform, run terraform apply if you miss it
. ./generated_variables.sh

docker build -t $NAME .

if [[ "${1:-empty}" == *"push"* ]] ; then
  ## you need AWS CLI to run this
  aws ecr get-login-password --region "${AWS_REGION}" | docker login --username AWS --password-stdin "${ECR_URL}"

  docker tag  $NAME:latest "${ECR_URL}:${ECR_TAG}"
  docker push "${ECR_URL}:${ECR_TAG}"
fi
