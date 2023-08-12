#!/bin/sh

docker compose down
docker volume rm c-campus-backend_mysql_data
docker builder prune -y