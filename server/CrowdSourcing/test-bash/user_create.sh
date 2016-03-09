#!/bin/bash

. global_settings.sh

SERVICE_PATH="user/register"

read -d '' JSON << EOF
{
   "id": "mole",
   "email": "mole@molehill.garden",
   "role": "driller",
   "organization": "Earch Moving",
   "password": "tunnel"
}
EOF

curl -i \
     -d "$JSON" \
     $BASE_URL/$SERVICE_PATH

