#! /usr/bin/env bash

mongo -u admin -p password --authenticationDatabase admin chirper \
  --eval 'db.chirps.createIndex({"userId": 1, "createdAt": 1, "_id": 1}, {"name": "byUserAndCreation"})'
mongo -u admin -p password --authenticationDatabase admin chirper \
  --eval 'db.chirps.createIndex({"relation.kind:": 1, "relation.parentId": 1, "createdAt": 1, "_id": 1}, {"name": "byRelation"})'
