#! /usr/bin/env bash

mongo -u admin -p password --authenticationDatabase admin cmpdb \
  --eval 'db.comparisons.createIndex({"userId": 1, "cmpId": 1, "_id": 1}, {"name": "byUserAndComparison"})'
