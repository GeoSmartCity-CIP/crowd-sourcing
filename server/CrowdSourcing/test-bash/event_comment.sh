#!/bin/bash

. global_settings.sh

SERVICE_PATH="event/comment"
UUID="9a5d8daa-911c-4831-a774-699a52ab02ca"

read -d '' JSON << EOF
{
   "user": {
      "id": "kret",
      "password": "hohoho"
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

