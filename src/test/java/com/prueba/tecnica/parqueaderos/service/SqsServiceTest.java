package com.prueba.tecnica.parqueaderos.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.prueba.tecnica.parqueaderos.config.AWSClient;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
@RunWith(MockitoJUnitRunner.class)
public class SqsServiceTest {
    @Mock
    private AWSClient awsClient;
    @Mock
    private AmazonSQS amazonSQS;
    @InjectMocks()
    private SqsService sqsService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(awsClient.getAWSCredentials()).thenReturn(null);
        sqsService = new SqsService(awsClient);
    }

    @Test
    public void testGetMessages() {
        String queueUrl = "testQueueUrl";
        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult()
                .withMessages(new Message(), new Message());
        when(amazonSQS.receiveMessage(Mockito.any(ReceiveMessageRequest.class)))
                .thenReturn(receiveMessageResult);
        GetQueueAttributesResult attributesResult = new GetQueueAttributesResult()
                .withAttributes(Collections.singletonMap("ApproximateNumberOfMessages", "5"));
        when(amazonSQS.getQueueAttributes(any(GetQueueAttributesRequest.class)))
                .thenReturn(attributesResult);

        List<Message> messages = sqsService.getMessages(queueUrl);

        assertEquals(6, messages.size());
        //verify(amazonSQS, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
    }

    @Test
    public void testGetMessagesCount() {
        String queueUrl = "testQueueUrl";
        GetQueueAttributesResult attributesResult = new GetQueueAttributesResult()
                .withAttributes(Collections.singletonMap("ApproximateNumberOfMessages", "5"));
        when(amazonSQS.getQueueAttributes(any(GetQueueAttributesRequest.class)))
                .thenReturn(attributesResult);

        int messagesCount = sqsService.getMessagesCount(queueUrl);

        assertEquals(5, messagesCount);
        verify(amazonSQS, times(1)).getQueueAttributes(any(GetQueueAttributesRequest.class));
    }

    @Test
    public void testDeleteMessages() {
        String queueUrl = "testQueueUrl";
        Message message1 = new Message().withMessageId("1").withReceiptHandle("handle1");
        Message message2 = new Message().withMessageId("2").withReceiptHandle("handle2");
        List<Message> messages = List.of(message1, message2);

        sqsService.deleteMessages(messages, queueUrl);

        verify(amazonSQS, times(1)).deleteMessageBatch(any(DeleteMessageBatchRequest.class));
    }
}
