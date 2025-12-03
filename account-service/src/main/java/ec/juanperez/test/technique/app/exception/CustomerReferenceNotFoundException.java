package ec.juanperez.test.technique.app.exception;

public class CustomerReferenceNotFoundException extends RuntimeException {
    public CustomerReferenceNotFoundException(Long customerId) {
        super("Customer reference not found with id: " + customerId);
    }
}

