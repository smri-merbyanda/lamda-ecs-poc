package com.example.myecs.Service;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;

public class SQSService {
    
    private final AmazonSQS sqsClient;

    public SQSService() {
        sqsClient = AmazonSQSClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }

    public String getQueueUrl(String queueName) {
        try {
            GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest().withQueueName(queueName);
            GetQueueUrlResult getQueueUrlResponse = sqsClient.getQueueUrl(getQueueUrlRequest);
            return getQueueUrlResponse.getQueueUrl();
        } catch (QueueDoesNotExistException ex) {
            throw new RuntimeException("Queue does not exist: " + ex.getMessage(), ex);
        }
    }

    public void deleteMessage(String queueUrl, String receiptHandle) {
        DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest()
                .withQueueUrl(queueUrl)
                .withReceiptHandle(receiptHandle);

        sqsClient.deleteMessage(deleteMessageRequest);
    }
}
