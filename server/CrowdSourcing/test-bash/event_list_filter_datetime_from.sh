#!/bin/bash

BASE_URL="http://localhost:8080/CrowsSourcing"
SERVICE_PATH="event/list"

read -d '' JSON << EOF
{
   "datetime": {
      "from": "2015-12-05T10:32:00"
   }
}
EOF

#echo "$JSON"
#exit 0

curl -i \
     -d "$JSON" \
     $BASE_URL/$SERVICE_PATH

