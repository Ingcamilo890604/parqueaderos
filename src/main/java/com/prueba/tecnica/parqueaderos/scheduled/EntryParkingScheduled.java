package com.prueba.tecnica.parqueaderos.scheduled;

import com.amazonaws.services.datasync.model.TaskSchedule;
import com.amazonaws.services.sqs.model.Message;
import com.prueba.tecnica.parqueaderos.config.AWSClient;
import com.prueba.tecnica.parqueaderos.service.MovementService;
import com.prueba.tecnica.parqueaderos.service.SqsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntryParkingScheduled extends TaskSchedule {
    @Autowired
    private final MovementService movementService;
    @Autowired
    private final SqsService sqsService;

    protected EntryParkingScheduled(MovementService movementService, SqsService sqsService) {
        super();
        this.movementService = movementService;
        this.sqsService = sqsService;
    }

    @Scheduled(fixedDelay = 500)
    public void executeTaskGetTransactionsFromSqs() {
        entryParkingTask();
    }

    protected void entryParkingTask() {
        List<Message> messages = sqsService.getMessages(AWSClient.ENTRY_PARKING_URL);
        List<Message> messagesToDelete = movementService.entryParkingByQueue(messages);
        sqsService.deleteMessages(messagesToDelete, AWSClient.ENTRY_PARKING_URL);
    }
}

