package ec.juanperez.test.technique.app.accounts.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(Long id) {
        super("Account not found with id: " + id);
    }
}
