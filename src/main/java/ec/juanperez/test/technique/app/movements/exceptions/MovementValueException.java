package ec.juanperez.test.technique.app.movements.exceptions;

import java.math.BigDecimal;

public class MovementValueException extends RuntimeException{

    public MovementValueException(BigDecimal value) {
        super("Movement value is less than or equal to zero: " + value);
    }
}
