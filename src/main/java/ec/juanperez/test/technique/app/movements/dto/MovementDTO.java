package ec.juanperez.test.technique.app.movements.dto;

import ec.juanperez.test.technique.app.movements.enums.MovementType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MovementDTO implements Serializable {
    private Long id;
    private MovementType movementType;
    private BigDecimal value;
    private LocalDateTime fechaMovimiento;
    private BigDecimal balance;
    private Long accountId;
    private String accountNumber;

    public MovementDTO(MovementType movementType,
                       BigDecimal value,
                       LocalDateTime fechaMovimiento,
                       BigDecimal balance){
        this.movementType = movementType;
        this.value = value;
        this.fechaMovimiento = fechaMovimiento;
        this.balance = balance;
    }
}
