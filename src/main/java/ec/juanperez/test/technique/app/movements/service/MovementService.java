package ec.juanperez.test.technique.app.movements.service;

import ec.juanperez.test.technique.app.movements.enums.MovementType;
import ec.juanperez.test.technique.app.movements.dto.MovementDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MovementService {

    MovementDTO create(MovementDTO movementDTO);
    MovementDTO update(Long idMovement, MovementDTO movementDTO);
    Optional<MovementDTO> findById(Long id);
    List<MovementDTO> findAll();
    void delete(Long id);
    MovementDTO registerMovementByType(Long accountId, MovementType type, BigDecimal value);
    Map<String, List<MovementDTO>> getMovementsByCustomerIdAndDates(Long customerId, LocalDateTime startTime, LocalDateTime endTime);
    Optional<BigDecimal> getCurrentBalance(Long accountId);
}
