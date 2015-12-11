#!/bin/bash

BASE_URL="http://localhost:8080/CrowsSourcing"
SERVICE_PATH="event/comment"
UUID="5ed32063-45bd-4ac2-9f68-0bf8efa38814"

read -d '' JSON << EOF
{
   "user": {
      "id": "franta",
      "email": "franta@nowhere.com"
   },
   "text": "A new comment",
   "datetime": "$(date +'%Y-%m-%dT%H:%M:%SZ')"

}
EOF

#echo "$JSON"
#exit 0

curl -i \
     -d "$JSON" \
     $BASE_URL/$SERVICE_PATH/$UUID

