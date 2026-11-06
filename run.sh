#!/bin/sh
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac -d out src/*.java && java -cp out Main
