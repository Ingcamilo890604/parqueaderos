package com.prueba.tecnica.parqueaderos.controller;

import com.prueba.tecnica.parqueaderos.dto.MovementDTO;
import com.prueba.tecnica.parqueaderos.general.exception.TransactionConflictException;
import com.prueba.tecnica.parqueaderos.model.Movements;
import com.prueba.tecnica.parqueaderos.service.MovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class MovementController {

    @Autowired
    MovementService movementService;

    @GetMapping("getAll")
    public ResponseEntity<Iterable<Movements>> getAll(){
        return ResponseEntity.ok(movementService.getAll());
    }

    @PostMapping("/entryParking")
    public ResponseEntity<Movements> create(@RequestBody MovementDTO movementDTO){
        try{
            Movements movement = movementService.entryParking(movementDTO);
            return ResponseEntity.ok(movement);
        }catch (TransactionConflictException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/exitParking")
    public ResponseEntity<Movements> close(@RequestBody MovementDTO movementDTO){
        try{
            Movements movement =movementService.exitParking(movementDTO);
            return ResponseEntity.ok(movement);
        }catch (IllegalArgumentException e){
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/average-time-by-vehicle-type")
    public Map<String, String> getAverageTimeByVehicleType() {
        return movementService.calculateAverageTimeByVehicleType();
    }
    @GetMapping("/vehicle/longest-stay")
    public ResponseEntity<MovementDTO> getVehicleWithLongestStay() {
        MovementDTO vehicleWithLongestStay = movementService.findVehicleWithLongestStay();
        if (vehicleWithLongestStay == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vehicleWithLongestStay);
    }
}
