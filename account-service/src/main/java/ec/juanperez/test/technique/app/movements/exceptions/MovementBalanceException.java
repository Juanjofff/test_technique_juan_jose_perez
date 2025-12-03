package ec.juanperez.test.technique.app.movements.exceptions;

public class MovementBalanceException extends RuntimeException{

    public MovementBalanceException() {
        super("Saldo no disponible");
    }

    public MovementBalanceException(String message) {
        super(message);
    }

}
