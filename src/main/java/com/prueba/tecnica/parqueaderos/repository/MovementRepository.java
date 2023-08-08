package com.prueba.tecnica.parqueaderos.repository;

import com.prueba.tecnica.parqueaderos.model.Movements;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@EnableScan
@Repository
public interface MovementRepository extends CrudRepository<Movements, String> {
    boolean existsByPlate(String plate);
    Movements findByMovementId ( String movementId);
    Movements findByPlateAndState(String plate, Integer state);
}
