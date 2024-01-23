# lamda-ecs-poc
 lambda to sqs to ecs poc


Follo below steps to deploy changes on image:
1. build docker image
2. tag image
3. deploy image
4. go to task where the container is uploaded
5. create new revision
6. go to cluster.
7. updated service then select then created revision.

Docker commands used:
-login validation
aws ecr get-login-password --region ap-southeast-1 | docker login --username AWS --password-stdin 259070268966.dkr.ecr.ap-southeast-1.amazonaws.com/ecstest

-build java program to image
docker build -t myimage:latest .

-tag image
docker tag myimage:latest 259070268966.dkr.ecr.ap-southeast-1.amazonaws.com/ecstest

-push to ecr
docker push 259070268966.dkr.ecr.ap-southeast-1.amazonaws.com/ecstest
