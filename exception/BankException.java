package exception;

// Practical 8: Checked base exception for all bank errors
public class BankException extends Exception {
    public BankException(String message) {
        super(message);
    }
}
