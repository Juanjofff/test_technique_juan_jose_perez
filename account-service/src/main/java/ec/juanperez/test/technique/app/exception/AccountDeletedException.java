package ec.juanperez.test.technique.app.accounts.exception;

public class AccountDeletedException extends RuntimeException {

    public AccountDeletedException(String message) {
        super(message);
    }

    public AccountDeletedException(Long id) {
        super("Account with id: " + id + " is deleted");
    }
}
