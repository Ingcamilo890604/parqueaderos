package com.prueba.tecnica.parqueaderos.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import com.google.common.collect.Lists;
import com.prueba.tecnica.parqueaderos.config.AWSClient;
import com.prueba.tecnica.parqueaderos.general.constants.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class SqsService {
    private static final Logger logger = LogManager.getLogger(SqsService.class);
    private final AWSClient awsClient;
    public AmazonSQS amazonSQS;

    public SqsService(AWSClient awsClient, AmazonSQS amazonSQS) {
        this.awsClient = awsClient;
        this.amazonSQS = amazonSQS;
    }

    public AmazonSQS getAmazonSQS() {
        if(amazonSQS == null) {
            amazonSQS = AmazonSQSClientBuilder.standard()
                    .withRegion(AWSClient.REGION)
                    .withCredentials(awsClient.getAWSCredentials()).build();
        }
        return amazonSQS;
    }

    public List<Message> getMessages(String queueUrl) {
        logger.info("Obteniendo mensajes de SQS.");
        int messagesCount = getMessagesCount(queueUrl);
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl)
                .withMaxNumberOfMessages(Constants.MAX_NUMBER_OF_MESSAGES);
        List<Message> messages = new LinkedList<>();
        int count = 0;
        while(messages.size() < messagesCount) {
            List<Message> list = getAmazonSQS().receiveMessage(receiveMessageRequest).getMessages();
            if(count + list.size() != count) {
                count = count + list.size();
            } else {
                logger.info("Se detiene obtención de mensajes porque el número de mensajes obtenidos en esta iteración fue cero");
                break;
            }
            messages.addAll(list);
        }
        logger.info("Finaliza la obtención de mensajes sqs");
        return messages;
    }

    public int getMessagesCount(String queueUrl) {
        GetQueueAttributesRequest getQueueAttributesRequest = new GetQueueAttributesRequest(queueUrl)
                .withAttributeNames(Constants.ALL);
        GetQueueAttributesResult getQueueAttributesResult = getAmazonSQS().getQueueAttributes(getQueueAttributesRequest);
        int messagesCount = Integer.parseInt(getQueueAttributesResult.getAttributes().get(Constants.APROXIMATE_NUMBER_OF_MESSAGES));
        return messagesCount;
    }

    public void deleteMessages(List<Message> messages, String queueUrl) {
        List<List<Message>> messagesPartition = Lists.partition(messages, Constants.MAX_NUMBER_OF_MESSAGES);
        for(List<Message> partition: messagesPartition) {
            List<DeleteMessageBatchRequestEntry> deleteMessageBatchRequestEntries = partition
                    .stream()
                    .map(m -> new DeleteMessageBatchRequestEntry().withId(m.getMessageId()).withReceiptHandle(m.getReceiptHandle()))
                    .toList();
            DeleteMessageBatchRequest deleteMessageBatchRequest = new DeleteMessageBatchRequest()
                    .withQueueUrl(queueUrl)
                    .withEntries(deleteMessageBatchRequestEntries);
            getAmazonSQS().deleteMessageBatch(deleteMessageBatchRequest);
        }
    }
}
