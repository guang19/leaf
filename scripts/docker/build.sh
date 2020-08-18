#!/bin/bash

cd ../../

mvn clean -Dmaven.test.skip=true install

cp -f leaf-server/target/leaf-server-*.jar scripts/docker/leaf-server.jar

cd scripts/docker

sudo docker build -t leaf-server:1.0.2 .

rm -rf leaf-server.jar