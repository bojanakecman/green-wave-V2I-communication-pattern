Distributed Systems Engineering SS20
===================================

In order to run locally:
-----------------------------

- change directory to actor-simulator and build image with `docker build -t actor-simulator .`
- change to project root and run `docker-compose up` to start all services and frontend
- open web UI on http://localhost:4200/
- start simulation with `docker run --network dse20_default actor-simulator`


In order to run on GCP:
-----------------------------

Note: Our deployment use static external IP (region us-east) for API gateway, this IP is configured in k8s/services.yaml.
(more: https://cloud.google.com/kubernetes-engine/docs/tutorials/configuring-domain-name-static-ip#step_2a_using_a_service) 


- create cluster and configure kubectl locally (https://cloud.google.com/kubernetes-engine/docs/quickstart)
- if not already pushed to container registry, first set up (https://cloud.google.com/container-registry/docs/pushing-and-pulling) and then use build_and_push_docker_to_gcp.sh
- (optional) if you changed static external IP, set in environment.ts IP of API Gateway, then build and push client module (use commands for client from shell script from step above)
- deploy and expose to cluster using `kubectl apply -f k8s/`
- open web app on IP of client from cluster
- to restart simulator `kubectl get pods` and then `kubectl delete pod actor-simulator-<id>`