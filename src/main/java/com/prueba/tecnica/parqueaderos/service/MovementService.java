package com.prueba.tecnica.parqueaderos.service;

import com.prueba.tecnica.parqueaderos.dto.MovementDTO;
import com.prueba.tecnica.parqueaderos.general.constants.Constants;
import com.prueba.tecnica.parqueaderos.general.exception.TransactionConflictException;
import com.prueba.tecnica.parqueaderos.general.utils.JSONUtils;
import com.prueba.tecnica.parqueaderos.model.Movements;
import com.prueba.tecnica.parqueaderos.repository.MovementRepository;
import com.amazonaws.services.sqs.model.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class MovementService {
    @Autowired
    MovementRepository movementRepository;
    @Autowired
    MessagesService messagesService;

    private static final Logger logger = LogManager.getLogger(MovementService.class);
    public Iterable<Movements> getAll(){
        return movementRepository.findAll();
    }

    public Movements entryParking(MovementDTO movementDTO){
        TimeZone timeZone = TimeZone.getTimeZone("America/Bogota");
        Date localDate = new Date(System.currentTimeMillis() + timeZone.getRawOffset());
        logger.info("Intentando realizar la entrada al parqueadero para la placa: {}", movementDTO.getPlate());
        if(movementRepository.findByPlateAndState(movementDTO.getPlate(), movementDTO.getState()) != null && movementDTO.getState() != 0){
            Movements movements = new Movements(
                    movementDTO.getVehicleType(),
                    movementDTO.getPlate(),
                    localDate,
                    Constants.ENTRY_STATE
            );
            logger.info("Entrada creada con éxito. ID de movimiento: {}", movementDTO.getMovementId());
            return movementRepository.save(movements);
        }
        logger.error("No se pudo realizar la entrada al parqueadero ya que el veiculo de placa {} ya tiene una transaccion abierta", movementDTO.getPlate());
        throw new TransactionConflictException("Ya existe una transacción abierta para la placa : " + movementDTO.getPlate());
    }
    public List<Message> entryParkingByQueue(List<Message> messages){
        List<Message> messagesToDelete = new ArrayList<>();
        for (Message message : messages){
            try {
                MovementDTO movementDTO = Optional.ofNullable(JSONUtils.jsonToObject(message.getBody(), MovementDTO.class))
                        .orElseThrow(() -> new ClassCastException(messagesService.getCannotCastMessage()));
                logger.info("Se recibe el mensaje con placa: {} y estado: {}", movementDTO.getPlate(), movementDTO.getState());
                Movements movement = entryFromQueue(movementDTO);
                Movements movementValidation = movementRepository.findByPlateAndState(movementDTO.getPlate(), movementDTO.getState());
                if(movementValidation == null){
                    movementRepository.save(movement);
                    messagesToDelete.add(message);
                    logger.info("Se crea la entrada al parqueadero para el vehiculo con placa : {}", movementDTO.getPlate());
                }else{
                    logger.info("Ya existe una entrada para el vehiculo de placa : {}", movementDTO.getPlate());
                }
            }
            catch (RuntimeException e){
                logger.error("Error procesando el mensaje: {}", message);
                e.printStackTrace();
            }
        }
        return messagesToDelete;
    }
    public Movements entryFromQueue(MovementDTO movementDTO){
        TimeZone timeZone = TimeZone.getTimeZone("America/Bogota");
        Date localDate = new Date(System.currentTimeMillis() + timeZone.getRawOffset());
        return Movements.builder()
                .vehicleType(movementDTO.getVehicleType())
                .plate(movementDTO.getPlate())
                .entryDate(localDate)
                .state(Constants.ENTRY_STATE)
                .build();
    }
    public Movements exitParking(MovementDTO movementDTO){
        TimeZone timeZone = TimeZone.getTimeZone("America/Bogota");
        Date localDate = new Date(System.currentTimeMillis() + timeZone.getRawOffset());
        Movements movements = movementRepository.findByPlateAndState( movementDTO.getPlate(), movementDTO.getState());
        if(movements != null && movements.getState() !=Constants.EXIT_STATE){
            movements.setExitDate(localDate);
            movements.setServiceTime(calculateTimeService(movements));
            movements.setState(Constants.EXIT_STATE);
            return movementRepository.save(movements);
        }
        throw new IllegalArgumentException("El movimiento no existe en la base de datos.");
    }
    public List<Message> exitParkingByQueue(List<Message> messages){
        TimeZone timeZone = TimeZone.getTimeZone("America/Bogota");
        Date localDate = new Date(System.currentTimeMillis() + timeZone.getRawOffset());
        List<Message> messagesToDelete = new ArrayList<>();
        for (Message message : messages){
            try {
                MovementDTO movementDTO = Optional.ofNullable(JSONUtils.jsonToObject(message.getBody(), MovementDTO.class))
                        .orElseThrow(() -> new ClassCastException(messagesService.getCannotCastMessage()));
                Movements movement = movementRepository.findByPlateAndState(movementDTO.getPlate(), movementDTO.getState());
                if(movement!= null && movement.getState()== Constants.ENTRY_STATE){
                    movement.setExitDate(localDate);
                    movement.setServiceTime(calculateTimeService(movement));
                    movement.setState(Constants.EXIT_STATE);
                    movementRepository.save(movement);
                    messagesToDelete.add(message);
                }
            }
            catch (RuntimeException e){
                e.printStackTrace();
            }
        }
        return messagesToDelete;
    }
    public String calculateTimeService(Movements movement){
        TimeZone timeZone = TimeZone.getTimeZone("America/Bogota");
        Date localDate = new Date(System.currentTimeMillis() + timeZone.getRawOffset());
        Date entryDate = movement.getEntryDate();
        Date exitDate = movement.getExitDate();
        if (exitDate == null) {
            exitDate = localDate;
        }
        long timeDifferenceMillis = exitDate.getTime() - entryDate.getTime();
        long days = TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifferenceMillis) % 60;
        return days + " días, " + hours + " horas, " + minutes + " minutos, " + seconds + " segundos";
    }

    public Map<String, String> calculateAverageTimeByVehicleType() {
        TimeZone timeZone = TimeZone.getTimeZone("America/Bogota");
        Date localDate = new Date(System.currentTimeMillis() + timeZone.getRawOffset());
        List<Movements> allMovements = (List<Movements>) getAll();
        Map<String, Long> totalTimeByType = new HashMap<>();
        Map<String, Integer> countByType = new HashMap<>();

        for (Movements movements : allMovements) {
            String vehicleType = movements.getVehicleType();
            Date entryDate = movements.getEntryDate();
            Date exitDate = movements.getExitDate();
            if (exitDate == null) {
                exitDate = localDate;
            }
            long timeDifferenceMillis = exitDate.getTime() - entryDate.getTime();
            totalTimeByType.put(vehicleType, totalTimeByType.getOrDefault(vehicleType, 0L) + timeDifferenceMillis);
            countByType.put(vehicleType, countByType.getOrDefault(vehicleType, 0) + 1);
        }
        Map<String, String> averageTimeByType = new HashMap<>();
        for (Map.Entry<String, Long> entry : totalTimeByType.entrySet()) {
            String vehicleType = entry.getKey();
            long totalTimeMillis = entry.getValue();
            int count = countByType.get(vehicleType);
            long averageTimeMillis = totalTimeMillis / count;
            long days = TimeUnit.MILLISECONDS.toDays(averageTimeMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(averageTimeMillis) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(averageTimeMillis) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(averageTimeMillis) % 60;

            String averageTimeFormatted = days + " días, " + hours + " horas, " + minutes + " minutos, " + seconds + " segundos";

            averageTimeByType.put(vehicleType, averageTimeFormatted);
        }

        return averageTimeByType;
    }
    public MovementDTO findVehicleWithLongestStay() {

        List<Movements> allMovements = (List<Movements>) getAll();
        if (allMovements.isEmpty()) {
            return null;
        }
        Movements vehicleWithLongestStay = null;
        long longestStayMillis = 0;

        for (Movements movements : allMovements) {

            long stayMillis = calculateTimeDifference(movements);

            if (stayMillis > longestStayMillis) {
                longestStayMillis = stayMillis;
                vehicleWithLongestStay = movements;
            }
        }
        if (vehicleWithLongestStay == null) {
            return null;
        }

        MovementDTO vehicleDTO = new MovementDTO();
        vehicleDTO.setMovementId(vehicleWithLongestStay.getMovementId());
        vehicleDTO.setVehicleType(vehicleWithLongestStay.getVehicleType());
        vehicleDTO.setPlate(vehicleWithLongestStay.getPlate());
        vehicleDTO.setEntryDate(vehicleWithLongestStay.getEntryDate());
        vehicleDTO.setExitDate(vehicleWithLongestStay.getExitDate());
        vehicleDTO.setServiceTime(calculateTimeService(vehicleWithLongestStay));

        return vehicleDTO;
    }
    private long calculateTimeDifference( Movements movement){
        TimeZone timeZone = TimeZone.getTimeZone("America/Bogota");
        Date localDate = new Date(System.currentTimeMillis() + timeZone.getRawOffset());
        Date entryDate = movement.getEntryDate();
        Date exitDate = movement.getExitDate();
        if(exitDate == null){
            exitDate = localDate;
        }
        return exitDate.getTime() - entryDate.getTime();
    }

}
