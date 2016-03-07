#!/bin/bash

PART_PREFIX="part://"

ATTACHMENT_1="@checkmark-512.png"
ATTACHMENT_2="@close-512.png"

read -d '' JSON << EOF
{
   "id": "$(uuidgen)",
   "description": "An event with 2 attachments",
   "media": [
      {"type": "image/jpeg", "uri": "${PART_PREFIX}1"},
      {"type": "image/jpeg", "uri": "${PART_PREFIX}2"}
   ],
   "user": {
      "id": "karel",
      "email": "karel@unckle.com",
      "role": "admin",
      "organization": "family"
   },
   "location": {
      "lat": 23.43,
      "lon": 33.1,
      "crs": "epsg:4326"
   },
   "priority": "normal",
   "datetime": "$(date +'%Y-%m-%dT%H:%M:%SZ')",
   "status": "submitted"
}
EOF

#echo "$JSON"
#exit 0

curl -i \
     -F event="$JSON" \
     -F ${PART_PREFIX}1=$ATTACHMENT_1 \
     -F ${PART_PREFIX}2=$ATTACHMENT_2 \
     http://localhost:8080/CrowdSourcing/event/create


