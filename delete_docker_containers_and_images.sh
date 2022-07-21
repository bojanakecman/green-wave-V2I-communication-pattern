#### Delete created containers and images ####
#!/bin/bash
docker rm $(docker ps -a -q) -v -f
docker rmi actor-registry-service:latest
docker rmi api-gateway:latest
docker rmi status-tracking-service:latest
docker rmi actor-control-service:latest
docker rmi actor-simulator:latest
docker rmi mongo:latest
docker rmi rabbitmq:latest
docker rmi client:latest