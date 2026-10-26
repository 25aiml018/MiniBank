package service;

// Practical 8: AutoCloseable session demonstrating try-with-resources
public class BankSession implements AutoCloseable {

    private final String sessionUser;

    public BankSession(String sessionUser) {
        this.sessionUser = sessionUser;
        System.out.println("[BankSession] Opened session for: " + sessionUser);
    }

    public void logActivity(String activity) {
        System.out.println("[BankSession] Activity: " + activity);
    }

    @Override
    public void close() {
        System.out.println("[BankSession] Closed session cleanly for: " + sessionUser);
    }
}
