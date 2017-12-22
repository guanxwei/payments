#!/bin/bash

urlrelease="http://mvn.hz.netease.com/artifactory/libs-releases"
idrelease="repo"

urlsnapshot="http://mvn.hz.netease.com/artifactory/libs-snapshots"
idsnapshot="snapshots"

url=$urlsnapshot
repid=$idsnapshot

model="paymentsplatform-account"
version="0.0.3-SNAPSHOT"
release="1.0.0"

stream="stream"

mvn deploy:deploy-file \
    -Durl=$url \
    -DrepositoryId=$repid \
    -DgroupId=com.netease.cloudmusic \
    -DgeneratePom=false \
    -DartifactId=paymentsplatform \
    -Dversion=$version \
    -Dpackaging=pom \
    -Dfile=pom.xml

cd $model
mvn clean package source:jar -DskipTests
cd ..

mvn deploy:deploy-file \
    -Durl=$url \
    -DrepositoryId=$repid \
    -DgroupId=com.netease.cloudmusic \
    -DgeneratePom=false \
    -DartifactId=$model-api \
    -Dversion=$version \
    -Dpackaging=jar \
    -Dfile=$model/$model-api/target/$model-api-$version.jar\
    -Dsources=$model/$model-api/target/$model-api-$version-sources.jar \
    -DpomFile=$model/$model-api/pom.xml

cd $stream
mvn clean package source:jar -DskipTests
cd ..

mvn deploy:deploy-file \
    -Durl=$url \
    -DrepositoryId=$repid \
    -DgroupId=com.netease.cloudmusic \
    -DgeneratePom=false \
    -DartifactId=$stream \
    -Dversion=$version \
    -Dpackaging=jar \
    -Dfile=$stream/target/$stream-$version.jar\
    -Dsources=$stream/target/$stream-$version-sources.jar \
    -DpomFile=$stream/pom.xml