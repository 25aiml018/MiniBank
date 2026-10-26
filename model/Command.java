package model;

// Practical 4: Record Command with type, accountNumber, and amount
public record Command(TransactionType type, String accountNumber, long amount) {
}
