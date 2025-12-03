package ec.juanperez.test.technique.app.movements.exceptions;

public class MovementNotFoundException extends RuntimeException{

    public MovementNotFoundException(String message) {
        super(message);
    }

    public MovementNotFoundException(Long id) {
        super("Movement not found with id: " + id);
    }
}
