#!/bin/bash

docker build -t eu.gcr.io/dse20-group-group-02/api-gateway ./api-gateway
docker build -t eu.gcr.io/dse20-group-group-02/actor-registry-service ./actor-registry-service
docker build -t eu.gcr.io/dse20-group-group-02/actor-control-service ./actor-control-service
docker build -t eu.gcr.io/dse20-group-group-02/status-tracking-service ./status-tracking-service
docker build -t eu.gcr.io/dse20-group-group-02/actor-simulator ./actor-simulator
docker build -t eu.gcr.io/dse20-group-group-02/client ./client
docker pull mongo
docker pull rabbitmq

docker tag mongo:latest eu.gcr.io/dse20-group-group-02/mongo
docker tag rabbitmq:latest eu.gcr.io/dse20-group-group-02/rabbitmq

docker push eu.gcr.io/dse20-group-group-02/mongo
docker push eu.gcr.io/dse20-group-group-02/rabbitmq
docker push eu.gcr.io/dse20-group-group-02/api-gateway ./api-gateway
docker push eu.gcr.io/dse20-group-group-02/actor-registry-service ./actor-registry-service
docker push eu.gcr.io/dse20-group-group-02/actor-control-service ./actor-control-service
docker push eu.gcr.io/dse20-group-group-02/status-tracking-service ./status-tracking-service
docker push eu.gcr.io/dse20-group-group-02/actor-simulator ./actor-simulator
docker push eu.gcr.io/dse20-group-group-02/client ./client
