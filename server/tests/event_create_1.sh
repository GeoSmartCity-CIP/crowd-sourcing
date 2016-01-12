#!/bin/bash

ATTACHMENT_1="@checkmark-512.xxx"
PART_PREFIX="part://"

read -d '' JSON << EOF
{
   "id": "$(uuidgen)",
   "description": "An event with 1 attachment",
   "media": [{"mime-type": "image/jpeg", "uri": "${PART_PREFIX}1"}],
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
     http://localhost:8080/CrowdSourcing/event/create


