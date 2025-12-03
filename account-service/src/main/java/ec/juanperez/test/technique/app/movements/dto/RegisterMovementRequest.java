package ec.juanperez.test.technique.app.movements.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterMovementRequest implements Serializable {
    
    @NotNull(message = "Account ID is required")
    private Long accountId;
    
    @NotNull(message = "Value is required")
    private BigDecimal value;
}

