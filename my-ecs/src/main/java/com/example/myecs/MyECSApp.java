package com.example.myecs;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MyECSApp {

    private final AmazonSQS sqs;
    private final String queueUrl;
    private final ExecutorService executorService;
    private final BlockingQueue<Message> messageQueue;
    private volatile boolean isShutdownRequested;
    Integer counter = 0;

    public MyECSApp() {
        this.sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion("ap-southeast-1")
                .build();
        this.queueUrl = "https://sqs.ap-southeast-1.amazonaws.com/259070268966/EcsQueue";
        this.executorService = Executors.newFixedThreadPool(3);
        this.messageQueue = new LinkedBlockingQueue<>();
        this.isShutdownRequested = false;
        // this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void processMessage(String message) {
        try {
            System.out.println("Processing message: " + message);

            if (message.equals("test1") || message.equals("test3") || message.equals("test5")) {
                System.out.println("message is equal to " + message + " sleeping for 2 mins");
                Thread.sleep(120000); // sleep for 2 mins
            } else if (message.equals("test7") || message.equals("test9")) {
                System.out.println("message is equal to " + message + " running intensive algo");
                intensiveCPUAlgo(message);
            }

            
        } catch (Exception e) {
            System.out.println("errrr" + e.getMessage());
        }

    }

    public void intensiveCPUAlgo(String message) {
        // Simulate resource-intensive operation
        for (int i = 0; i < 1000000000; i++) {
            // CPU-intensive operation (e.g., multiplying and dividing numbers)
            double result1 = Math.pow(i, 2) / Math.sqrt(i);
            double result2 = Math.sin(result1) * Math.cos(result1);
            double result3 = Math.log(result2 + 1);

            // Perform more complex computations based on previous results
            double finalResult = Math.pow(result1, result2) / Math.sqrt(result3);

            System.out.println("CPU-intensive result: " + finalResult +" of message "+ message);
        }
    }

    public void receiveAndProcessMessagesConcurrently() {
        while (true) {
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMaxNumberOfMessages(3) // Adjust the batch size to the number of threads
                    .withVisibilityTimeout(120)
                    .withWaitTimeSeconds(20);

            List<Message> receivedMessages = sqs.receiveMessage(receiveMessageRequest).getMessages();

            // Put received messages into the blocking queue
            messageQueue.addAll(receivedMessages);
            if (receivedMessages.isEmpty()) {
                System.out.println("No messages in the SQS queue.");
                if (receivedMessages.size() == -1) {
                    break; // addrss sonar lint warning
                }
            } else {
                // System.out.println("Received messages: " + receivedMessages);
                System.out.println("receivedMessages.size() - " + receivedMessages.size());
                for (int i = 0; i < Math.min(receivedMessages.size(), 3); i++) {
                    executorService.submit(() -> {
                        try {
                            counter++;
                            // Dequeue a message from the blocking queue
                            Message receivedMessage = messageQueue.take();

                            String messageBody = receivedMessage.getBody();
                            String receiptHandle = receivedMessage.getReceiptHandle();
                            System.out.println("executorService.submit(() -> counter " + counter);
                            System.out.println("messageBody: " + messageBody);

                            // Process the received message
                            processMessage(messageBody);
                            System.out.println("done processing message -> " + messageBody);
                            // Delete received message from queue
                            sqs.deleteMessage(new DeleteMessageRequest(queueUrl, receiptHandle));
                            System.out.println("message " + messageBody + " deleted");

                        } catch (Exception e) {
                            System.out.println("Error processing message: " + e.getMessage());
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            }
        }
        // Gracefully shut down the executorService
        // executorService.shutdown();
    }

    public void shutdown() {
        isShutdownRequested = true;
    }

    public static void main(String[] args) {
        System.out.println("Starting my ECS app POC!");
        MyECSApp ecsTask = new MyECSApp();
        ecsTask.receiveAndProcessMessagesConcurrently();
        System.out.println("exiting task");
    }
}