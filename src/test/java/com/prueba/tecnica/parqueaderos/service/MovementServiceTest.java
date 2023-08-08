package com.prueba.tecnica.parqueaderos.service;

import com.amazonaws.services.sqs.model.Message;
import com.prueba.tecnica.parqueaderos.dto.MovementDTO;
import com.prueba.tecnica.parqueaderos.general.constants.Constants;
import com.prueba.tecnica.parqueaderos.general.utils.JSONUtils;
import com.prueba.tecnica.parqueaderos.model.Movements;
import com.prueba.tecnica.parqueaderos.repository.MovementRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class MovementServiceTest {
    @Mock
    private MovementRepository movementRepository;

    @InjectMocks()
    private MovementService movementService;

    MovementDTO movementDTO = new MovementDTO();

    Movements movement = new Movements();

    List<Movements> movementsList = new ArrayList<>();

    List<Message> messages = new ArrayList<>();

    Message message = new Message();

    @BeforeEach
    public void setUp(){
        movementDTO.setMovementId("abcd1234");
        movementDTO.setVehicleType("AutoTest");
        movementDTO.setPlate("ABC123");
        movementDTO.setEntryDate(new Date());
        movementDTO.setExitDate(new Date());
        movementDTO.setServiceTime("1 hora");

        movement.setMovementId("abcd1234");
        movement.setVehicleType("AutoTest");
        movement.setPlate("ABC123");
        movement.setEntryDate(new Date());
        movement.setExitDate(new Date());
        movement.setServiceTime("1 hora");

        message.setBody(JSONUtils.objectToJson(movementDTO));
        messages.add(message);

        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAll(){
        when(movementRepository.findAll()).thenReturn(movementsList);
        List<Movements> response = (List<Movements>) movementService.getAll();
        assertEquals(movementsList.size(),response.size());
    }

    @Test
    public void testEntryParkingSuccessfulEntry(){
        movementDTO.setState(1);
        when(movementRepository.findByPlateAndState(any(),any())).thenReturn(movement);
        when(movementRepository.save(any())).thenReturn(movement);
        Movements result  = movementService.entryParking(movementDTO);
        Assertions.assertEquals(movementDTO.getPlate(), result.getPlate());
        Assertions.assertEquals(movementDTO.getServiceTime(), result.getServiceTime());

    }
    @Test
    public void testEntryParkingByQueue_EntryCreated(){

        when(movementRepository.findByPlateAndState(any(),any())).thenReturn(null);
        when(movementRepository.save(any())).thenReturn(movement);
        List<Message> result = movementService.entryParkingByQueue(messages);
        Assertions.assertEquals(messages.size(),result.size());
    }

    @Test
    public void testExitParkingSuccessful(){
        movementDTO.setState(Constants.ENTRY_STATE);
        movement.setState(Constants.ENTRY_STATE);
        when(movementRepository.findByPlateAndState(any(),any())).thenReturn(movement);
        when(movementRepository.save(any())).thenReturn(movement);
        Movements result = movementService.exitParking(movementDTO);
        Assertions.assertEquals(result.getState(),Constants.EXIT_STATE);
    }

    @Test
    public void testExitParkingByQueueSuccessful(){
        movement.setState(Constants.ENTRY_STATE);
        when(movementRepository.findByPlateAndState(any(), any())).thenReturn(movement);
        when(movementRepository.save(any())).thenReturn(movement);
        List<Message> result = movementService.exitParkingByQueue(messages);
        Assertions.assertEquals(messages.size(),result.size());
    }
    @Test
    public void testCalculateAverageTimeByVehicleType_NoMovements_EmptyResult() {

        when(movementRepository.findAll()).thenReturn(new ArrayList<>());
        Map<String, String> averageTimes = movementService.calculateAverageTimeByVehicleType();
        assertEquals(0, averageTimes.size());
    }
    @Test
    public void testCalculateAverageTimeByVehicleType_MovementsWithDifferentTypes_CorrectResult() {

        String vehicleType1 = "Car";
        String vehicleType2 = "Motorcycle";
        long entryTimeMillis = System.currentTimeMillis() - 2 * 60 * 60 * 1000;
        long exitTimeMillis = System.currentTimeMillis();

        Movements movement1 = new Movements();
        movement1.setVehicleType(vehicleType1);
        movement1.setEntryDate(new Date(entryTimeMillis));
        movement1.setExitDate(new Date(exitTimeMillis));

        Movements movement2 = new Movements();
        movement2.setVehicleType(vehicleType2);
        movement2.setEntryDate(new Date(entryTimeMillis));
        movement2.setExitDate(new Date(exitTimeMillis));

        List<Movements> movementsList = new ArrayList<>();
        movementsList.add(movement1);
        movementsList.add(movement2);

        when(movementRepository.findAll()).thenReturn(movementsList);
        Map<String, String> averageTimes = movementService.calculateAverageTimeByVehicleType();

        Assertions.assertEquals(2, averageTimes.size());
        Assertions.assertTrue(averageTimes.containsKey(vehicleType1));
        Assertions.assertTrue(averageTimes.containsKey(vehicleType2));
    }

    @Test
    public void testFindVehicleWithLongestStay_NoMovements_ReturnNull() {

        when(movementRepository.findAll()).thenReturn(movementsList);

        MovementDTO result = movementService.findVehicleWithLongestStay();

        Assertions.assertNull(result);
    }

    @Test
    public void testFindVehicleWithLongestStay_MultipleMovements_CorrectResult() {

        long entryTimeMillis = System.currentTimeMillis() - 3 * 60 * 60 * 1000;
        long exitTimeMillis = System.currentTimeMillis();


        Movements shortStayMovement = new Movements();
        shortStayMovement.setEntryDate(new Date(entryTimeMillis));
        shortStayMovement.setExitDate(new Date(exitTimeMillis - 2 * 60 * 60 * 1000));

        Movements longStayMovement = new Movements();
        longStayMovement.setEntryDate(new Date(entryTimeMillis));
        longStayMovement.setExitDate(new Date(exitTimeMillis - 60 * 60 * 1000));

        movementsList.add(shortStayMovement);
        movementsList.add(longStayMovement);


        when(movementRepository.findAll()).thenReturn(movementsList);

        MovementDTO result = movementService.findVehicleWithLongestStay();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(longStayMovement.getMovementId(), result.getMovementId());
    }

}
