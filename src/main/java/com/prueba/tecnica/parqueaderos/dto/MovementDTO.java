package com.prueba.tecnica.parqueaderos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MovementDTO {

    private String movementId;
    private String vehicleType;
    private String plate;
    private Date entryDate;
    private Date exitDate;
    private String serviceTime;
    private Integer state;
}
