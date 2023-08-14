#!/bin/sh

./gradlew clean build -x test
docker build -t wndyd0131/c-campus .
docker push wndyd0131/c-campus