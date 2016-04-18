#!/bin/bash

. global_settings.sh

SERVICE_PATH="event/change"
UUID="5ed32063-45bd-4ac2-9f68-0bf8efa38814"

read -d '' JSON << EOF
{
   "id": "88c828ad-b245-471e-91c2-30e7cafcd4ed",
   "user": {
      "id": "mole",
      "password": "tunnel"
   },
   "priority": "high",
   "status": "solved"
}
EOF

#echo "$JSON"
#exit 0

curl -i \
     -d "$JSON" \
     $BASE_URL/$SERVICE_PATH/$UUID

