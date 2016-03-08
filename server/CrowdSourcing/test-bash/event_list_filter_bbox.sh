#!/bin/bash

. global_settings.sh

SERVICE_PATH="event/list"

read -d '' JSON << EOF
{
   "filter": {
      "bbox": {
         "lat-min": 23,
         "lon-min": 33.1,
         "lat-max": 24,
         "lon-max": 33.1,
         "crs": "epsg:4326"
      }
   }
}
EOF

#echo "$JSON"
#exit 0

curl -i \
     -d "$JSON" \
     $BASE_URL/$SERVICE_PATH

