#! /usr/bin/env bash

export TOKEN=$(curl -s            \
  --data "grant_type=password"    \
  --data "client_id=waes-client"  \
  --data "username=tester"        \
  --data "password=t3ster"        \
  http://localhost:9999/auth/realms/waes/protocol/openid-connect/token | jq -r .access_token)

