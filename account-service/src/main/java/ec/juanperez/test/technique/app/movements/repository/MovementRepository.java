package ec.juanperez.test.technique.app.movements.repository;

import ec.juanperez.test.technique.app.movements.dto.MovementDTO;
import ec.juanperez.test.technique.app.movements.model.Movements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovementRepository extends JpaRepository<Movements, Long> {

    @Query(value = "SELECT m.saldo " +
            "FROM movimientos m " +
            "WHERE m.id_cuenta = :accountId " +
            "ORDER BY m.fecha_movimiento DESC " +
            "LIMIT 1", nativeQuery = true)
    Optional<BigDecimal> balanceLastMovement(@Param("accountId") Long accountId);

    @Query(" select m from Movements m join fetch m.account ")
    List<Movements> findAllWithAccount();

    @Query(" select new ec.juanperez.test.technique.app.movements.dto.MovementDTO( " +
            " m.movementType, " +
            " m.value, " +
            " m.fechaMovimiento, " +
            " m.balance " +
            " )" +  
            " from Movements m " +
            " join Account a on m.account.id = a.id " +
            " where m.account.id = :accountId " +
            " and m.fechaMovimiento between :startDate and :endDate " +
            " order by m.fechaMovimiento ")
    List<MovementDTO> findMovementsByAccount(@Param("accountId") Long accountId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query(" select m from Movements m join fetch m.account where m.id = :id ")
    Optional<Movements> findByIdWithAccount(@Param("id") Long id);
}
