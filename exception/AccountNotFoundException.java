package exception;

// Practical 8: Exception thrown when an account cannot be found
public class AccountNotFoundException extends BankException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
