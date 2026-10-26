package model;

import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.annotation.Id;
import model.annotation.Positive;

// Practical 2, 3, 5, 6, 7, 8, 9: Abstract Account with encapsulation, polymorphism, and synchronization
public abstract class Account implements Transactable, InterestBearing {

    private static int accountCounter = 0;

    @Id
    private final String accountNumber;

    private String ownerName;

    @Positive
    private long balance;

    private boolean active;

    // Helper to auto-generate account numbers like "AC0001"
    private static synchronized String generateAccountNumber() {
        accountCounter++;
        return String.format("AC%04d", accountCounter);
    }

    // Constructor taking ownerName and opening balance
    public Account(String ownerName, long balance) {
        this.accountNumber = generateAccountNumber();
        this.ownerName = ownerName;
        this.balance = balance;
        this.active = true;
    }

    // Constructor taking only ownerName (calls first constructor with balance 0)
    public Account(String ownerName) {
        this(ownerName, 0);
    }

    // Constructor with explicit String account number
    public Account(String accountNumber, String ownerName, long balance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
        this.active = true;
    }

    // Overload with int account number for compatibility
    public Account(int accountNumber, String ownerName, long balance) {
        this(String.format("AC%04d", accountNumber), ownerName, balance);
    }

    // Public getters
    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public long getBalance() {
        return balance;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Practical 8 & 9: Synchronized deposit with validation
    @Override
    public synchronized void deposit(long amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive: " + amount);
        }
        this.balance += amount;
    }

    // Practical 8 & 9: Synchronized withdraw with custom exception
    @Override
    public synchronized boolean withdraw(long amount) throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive: " + amount);
        }
        if (!canWithdraw(amount)) {
            long shortfall = amount - balance;
            throw new InsufficientFundsException("Insufficient funds! Shortfall is: " + shortfall, shortfall);
        }
        this.balance -= amount;
        return true;
    }

    // Practical 8: Transfer between accounts using try-catch-finally
    public void transfer(Account to, long amount) throws BankException {
        try {
            this.withdraw(amount);
            to.deposit(amount);
            System.out.println("Transferred Rs. " + amount + " from " + this.accountNumber + " to " + to.accountNumber);
        } catch (BankException e) {
            System.out.println("Transfer failed: " + e.getMessage());
            throw e;
        } finally {
            System.out.println("Transfer operation completed between " + this.accountNumber + " and " + to.accountNumber);
        }
    }

    // Practical 5: Abstract methods for polymorphism
    public abstract double interestRate();

    public abstract boolean canWithdraw(long amount);

    // Practical 3: toString() returning readable line
    @Override
    public String toString() {
        return "Account Number: " + accountNumber + ", Owner Name: " + ownerName + ", Balance: " + balance;
    }

    // Practical 3: equals() based on accountNumber
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account other = (Account) o;
        return accountNumber != null && accountNumber.equals(other.accountNumber);
    }

    // Practical 3: hashCode() based on accountNumber
    @Override
    public int hashCode() {
        return accountNumber != null ? accountNumber.hashCode() : 0;
    }
}