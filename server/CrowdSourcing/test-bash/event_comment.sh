#!/bin/bash

. global_settings.sh

SERVICE_PATH="event/comment"
UUID="5ed32063-45bd-4ac2-9f68-0bf8efa38814"

read -d '' JSON << EOF
{
   "user": {
      "id": "franta"
   },
   "text": "A new comment",
   "datetime": "$(date +'%Y-%m-%dT%H:%M:%SZ')"

}
EOF

#echo "$JSON"
#exit 0

exit curl -i \
     -d "$JSON" \
     $BASE_URL/$SERVICE_PATH/$UUID

