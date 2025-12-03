package ec.juanperez.test.technique.app.customers.exception;

public class CustomerDeletedException extends RuntimeException {
    
    public CustomerDeletedException(String message) {
        super(message);
    }
    
    public CustomerDeletedException(Long id) {
        super("Customer with id: " + id + " is deleted");
    }
}

