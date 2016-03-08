#!/bin/bash

. global_settings.sh

SERVICE_PATH="event/list"

read -d '' JSON << EOF
{
   "filter": {
      "priority": [
         "high", "urgent"
      ]
   }
}
EOF

#echo "$JSON"
#exit 0

curl -i \
     -d "$JSON" \
     $BASE_URL/$SERVICE_PATH

