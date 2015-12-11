#!/bin/bash

BASE_URL="http://localhost:8080/CrowsSourcing"
SERVICE_PATH="event/list"

read -d '' JSON << EOF
{
   "datetime": {
      "from": "2014-12-03T00:00:00",
      "to": "2015-12-03T00:00:00"
   }
}
EOF

#echo "$JSON"
#exit 0

curl -i \
     -d "$JSON" \
     $BASE_URL/$SERVICE_PATH

