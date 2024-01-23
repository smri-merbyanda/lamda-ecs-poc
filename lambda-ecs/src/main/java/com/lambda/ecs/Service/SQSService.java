package com.lambda.ecs.Service;

import java.util.List;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SQSService {

    private final AmazonSQS sqsClient;

    public SQSService() {

        sqsClient = AmazonSQSClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }

    public void sendMessageToSQS(String queueName, String messageBody) {
        String queueUrl = getQueueUrl(queueName);
        // SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, messageBody);
        SendMessageRequest sendMessageRequest = new SendMessageRequest("https://sqs.ap-southeast-1.amazonaws.com/259070268966/EcsQueue", messageBody);
        sqsClient.sendMessage(sendMessageRequest);
    }

    private String getQueueUrl(String queueName) {
        try {
            GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest().withQueueName(queueName);
            GetQueueUrlResult getQueueUrlResult = sqsClient.getQueueUrl(getQueueUrlRequest);
            return getQueueUrlResult.getQueueUrl();
        } catch (QueueDoesNotExistException ex) {
            throw new RuntimeException("Queue does not exist: " + ex.getMessage(), ex);
        }
    }

    public void sendBatchMessagesToSQS(String queueName, List<SendMessageBatchRequestEntry> entries,
            LambdaLogger logger) {
        try {
            String queueUrl = getQueueUrl(queueName);
            if (queueUrl != null) {
                SendMessageBatchRequest sendMessageBatchRequest = new SendMessageBatchRequest(queueUrl, entries);
                SendMessageBatchResult sendMessageBatchResult = sqsClient.sendMessageBatch(sendMessageBatchRequest);

                List<BatchResultErrorEntry> failedEntries = sendMessageBatchResult.getFailed();
                if (!failedEntries.isEmpty()) {
                    logger.log("send batch messages failed entries: " + failedEntries);
                }
            }
        } catch (Exception e) {
            logger.log("error in sending batch message  " + e.getMessage());
        }
    }

}