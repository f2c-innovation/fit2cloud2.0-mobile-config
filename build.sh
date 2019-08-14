#!/bin/bash

mvn clean package -Dmaven.test.skip=true

docker build -t registry.fit2cloud.com/innovation/mobile-config:master .
docker push registry.fit2cloud.com/innovation/mobile-config:master
