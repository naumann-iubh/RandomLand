#!/bin/bash

docker build -t randomland .

docker run --name RandomLand --rm -p 8080:8080 randomland