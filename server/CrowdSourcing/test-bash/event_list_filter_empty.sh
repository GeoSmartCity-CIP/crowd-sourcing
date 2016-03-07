#!/bin/bash

BASE_URL="http://localhost:8080/CrowdSourcing"
#BASE_URL="http://geo.mapshakers.com:8080/CrowdSourcing"
SERVICE_PATH="event/list"

read -d '' JSON << EOF
{
}
EOF

#echo "$JSON"
#exit 0

curl -i \
     -d "$JSON" \
     $BASE_URL/$SERVICE_PATH

