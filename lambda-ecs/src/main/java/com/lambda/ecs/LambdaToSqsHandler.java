package com.lambda.ecs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.lambda.ecs.Service.SQSService;

public class LambdaToSqsHandler extends SQSService implements RequestHandler<String, String> {
    @Override
    public String handleRequest(String input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("LambdaToSqsHandler!");
        String jsonStr = "";
        sendMessageToSQS("EcsQueue", input);
        return "Success!";
    }
}