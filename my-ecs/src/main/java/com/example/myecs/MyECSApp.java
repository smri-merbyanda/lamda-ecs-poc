package com.example.myecs;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;

import java.util.List;

public class MyECSApp {

    private final AmazonSQS sqs;
    private final String queueUrl;

    public MyECSApp() {
        this.sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion("ap-southeast-1")
                .build();
        this.queueUrl = "https://sqs.ap-southeast-1.amazonaws.com/259070268966/EcsQueue";
    }

    public void processMessage(String message) {
        System.out.println("Processing message: " + message);

        // Simulate resource-intensive operation
        for (int i = 0; i < 1000000000; i++) {
            // More CPU-intensive operation (e.g., multiplying and dividing numbers)
            double result = Math.pow(i, 2) / Math.sqrt(i);
            System.out.println("result of message " + message + " :" + result);
        }

        // actual processing logic
    }

    public void receiveAndProcessMessages() {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(queueUrl)
                .withMaxNumberOfMessages(1)
                .withVisibilityTimeout(30) // Set an appropriate value based on your processing time
                .withWaitTimeSeconds(20); // Set a non-zero value to enable long polling

        System.out.println("receiveMessageRequest - " + receiveMessageRequest);

        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();

        System.out.println("Received messages: " + messages);

        for (Message receivedMessage : messages) {

            String messageBody = receivedMessage.getBody();
            String receiptHandle = receivedMessage.getReceiptHandle();

            System.out.println("messageBody: " + messageBody);
            System.out.println("receiptHandle: " + receiptHandle);

            try {
                // Process the received message
                processMessage(messageBody);

                // Delete received message from queue
                sqs.deleteMessage(new DeleteMessageRequest(queueUrl, receiptHandle));
            } catch (Exception e) {
                System.err.println("Error processing message: " + e.getMessage());
                // can throw the exception to trigger a retry later on if needed
            }
        }

        if (messages.isEmpty()) {
            System.out.println("No messages in the SQS queue.");
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting my ECS app POC!");
        System.out.println("change 3 log test");
        MyECSApp ecsTask = new MyECSApp();
        int counter = 0;
        // Run continuously
        boolean keepRunning = true;
        while (keepRunning) {
            counter++;
            System.out.println("Running receiveAndProcessMessages...->" + counter);
            ecsTask.receiveAndProcessMessages();
            try {
                // Sleep for some time before polling again
                Thread.sleep(1000); // Adjust the sleep time as needed
            } catch (InterruptedException e) {
                // Handle interruptions gracefully
                Thread.currentThread().interrupt();
                keepRunning = false;
            }
        }
    }
}
