#!/bin/bash

cd ../../

mvn clean -Dmaven.test.skip=true install

cp -f leaf-server/target/leaf-server-*.jar scripts/docker/

rm -f leaf-server-.jar