package com.lambda.ecs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.AssignPublicIp;
import software.amazon.awssdk.services.ecs.model.ContainerOverride;
import software.amazon.awssdk.services.ecs.model.KeyValuePair;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;

public class LambdaEcsIntegration0 implements RequestHandler<String, String> {
    @Override
    public String handleRequest(String input, Context context) {
        LambdaLogger logger = context.getLogger();

        String[] arrayStr = input.split(", ");
        logger.log("arrayStr ->" + arrayStr); 
        try (EcsClient ecsClient = EcsClient.create()) {

            for (String param : arrayStr) {
                logger.log("param ->" + param);
                RunTaskRequest runTaskRequest = RunTaskRequest.builder()
                        .taskDefinition("myecstask") // Specify the task definition ARN or family and revision
                        .launchType("FARGATE")
                        .cluster("lambda2ec2cluster")
                        .count(1)
                        .networkConfiguration(networkConfig -> networkConfig
                                .awsvpcConfiguration(awsvpcConfig -> awsvpcConfig
                                        .subnets("subnet-0c6835c3ebb338a63",
                                                "subnet-00d2430104bf5e711",
                                                "subnet-0cdc57ce35428187d")
                                        .securityGroups("sg-07caf6fcbd3c2a3ad")
                                        .assignPublicIp(AssignPublicIp.ENABLED)
                                        .build())
                                .build())
                        .overrides(overrides -> overrides
                                .containerOverrides(ContainerOverride.builder()
                                        .name("myecstest")
                                        .command("java", "-jar", "ecs-1.0-SNAPSHOT.jar")
                                        .environment(KeyValuePair.builder().name("PARAM1").value(param).build())
                                        .build())
                                .build())
                        .build();

                logger.log("Attempting to run ECS task with request: " + runTaskRequest);

                RunTaskResponse runTaskResponse = ecsClient.runTask(runTaskRequest);

                if (!runTaskResponse.failures().isEmpty()) {
                    logger.log("Failed to run ECS task. Failures: " + runTaskResponse.failures());
                    return "Failed to run ECS task. Check CloudWatch Logs for details.";
                }

                logger.log("ECS task started successfully. Task ARN: " + runTaskResponse.tasks().get(0).taskArn());
                logger.log("Cluster ARN: " + runTaskResponse.tasks().get(0).clusterArn());
                logger.log("Container Instance ARN: " + runTaskResponse.tasks().get(0).containerInstanceArn());
                logger.log("Task Definition ARN: " + runTaskResponse.tasks().get(0).taskDefinitionArn());
                logger.log("runTaskResponse.toString() - > " + runTaskResponse.toString());
                logger.log("runTaskResponse - > " + runTaskResponse);
            }

            return "ECS task invoked successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception ->" + e.getMessage();
        }
    }
}