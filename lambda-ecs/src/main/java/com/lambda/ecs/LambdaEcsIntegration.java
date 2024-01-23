package com.lambda.ecs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.ContainerOverride;
import software.amazon.awssdk.services.ecs.model.KeyValuePair;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;
import software.amazon.awssdk.services.ecs.model.TaskOverride;
import software.amazon.awssdk.services.ecs.model.NetworkConfiguration;
import software.amazon.awssdk.services.ecs.model.AssignPublicIp;
import software.amazon.awssdk.services.ecs.model.AwsVpcConfiguration;

public class LambdaEcsIntegration implements RequestHandler<String, String> {
	@Override
	public String handleRequest(String input, Context context) {
		LambdaLogger logger = context.getLogger();

		String[] arrayStr = input.split(", ");
		logger.log("arrayStr ->" + arrayStr);
		try (EcsClient ecsClient = EcsClient.create()) {
			String taskDefinition = "arn:aws:ecs:ap-southeast-1:259070268966:task-definition/myecstask:1";
			Integer counter = 0;
			for (String param : arrayStr) {
				counter++;
				logger.log("param ->" + param);
				logger.log("param" + counter + " -> " + param);

				NetworkConfiguration networkConfiguration = NetworkConfiguration.builder()
						.awsvpcConfiguration(AwsVpcConfiguration.builder()
								.subnets("subnet-0c6835c3ebb338a63", "subnet-00d2430104bf5e711",
										"subnet-0cdc57ce35428187d")
								.securityGroups("sg-07caf6fcbd3c2a3ad")
								.assignPublicIp(AssignPublicIp.ENABLED)
								.build())
						.build();

				// Define container override for the RunTaskRequest
				ContainerOverride containerOverride = ContainerOverride.builder()
						.name("myecstest") // Specify the name of the container to override
						.command("java", "-jar", "ecs-1.0-SNAPSHOT.jar") // Override the command for the container
						.environment( // Override environment variables for the container
								KeyValuePair.builder().name("PARAM1").value(param).build())
						// KeyValuePair.builder().name("VAR2").value("value2").build())
						.build();

				// Create the RunTaskRequest with overrides
				RunTaskRequest runTaskRequest = RunTaskRequest.builder()
						.taskDefinition("myecstask") // Specify the task definition ARN or family and revision
						.launchType("FARGATE")
						.cluster("lambda2ec2cluster")
						.count(1)
						.networkConfiguration(networkConfiguration)
						.overrides(TaskOverride.builder().containerOverrides(containerOverride).build())
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
				logger.log("logger: runTaskResponse.toString() - > " + runTaskResponse.toString());
				logger.log("runTaskResponse - > " + runTaskResponse);
			}

			return "ECS task invoked successfully!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Exception ->" + e.getMessage();
		}
	}
}