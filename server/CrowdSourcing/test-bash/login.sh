#!/bin/bash

read -d '' JSON << EOF
{
   "user": {
      "id": "kret",
      "password": "hohoho"
   }
}
EOF

#echo "$JSON"
#exit 0

curl -i \
     -d "$JSON" \
     http://localhost:8080/CrowdSourcing/login


