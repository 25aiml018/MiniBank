package exception;

// Practical 8: Exception thrown when transaction amount is non-positive
public class InvalidAmountException extends BankException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
