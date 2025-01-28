#!/bin/bash

export HOST_UID=$(id -u)
export HOST_GID=$(id -g)

docker build --build-arg UID=$HOST_UID --build-arg GID=$HOST_GID -t randomland .

docker run --name RandomLand --rm -p 8080:8080 randomland