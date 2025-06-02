#!/bin/bash

SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")

cd "$SCRIPTPATH" || exit 1
mvn clean package -DskipTests || exit 1

cd "${SCRIPTPATH}/target" || exit 1

docker buildx build -f ./Dockerfile --tag nickas21/tb-lw-demo-client:latest .

read -r -p "Push image to Docker Hub? [Y/n]: " PUSH_CHOICE

if [[ "$PUSH_CHOICE" =~ ^[Yy]$ || -z "$PUSH_CHOICE" ]]; then
    docker push nickas21/tb-lw-demo-client:latest
else
    echo "Skipping push."
fi

# Приклад локального запуску контейнера для перевірки:
# docker run --rm -it nickas21/tb-lw-demo-client:latest

