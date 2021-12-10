#!/usr/bin/env bash

target=data
if [ $# -ge 1 ]; then
    target=$1
fi

if [ -f ${target} ]; then
    echo "Please save/remove previous data folder:${target}."
    exit -1
else
    mkdir ${target}
fi

for i in 28 29 30 31
do
    zipfile=compute${i}-log.tar.bz2
    ssh compute${i} tar -jcf ${zipfile} flink-1.14.0/log
    scp compute${i}:${zipfile} ${target}/${zipfile}
    ssh compute${i} "rm -f ${zipfile}; rm -rf flink-1.14.0/log/*"
done
