package service;

import exception.BankException;
import model.Account;

// Practical 10: Safe transfer service avoiding deadlock by consistent lock ordering
public class BankService {

    // Deadlock-free transfer by locking accounts in consistent lexicographical order of accountNumber
    public static void safeTransfer(Account from, Account to, long amount) throws BankException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Accounts cannot be null");
        }

        // Determine consistent lock order based on accountNumber
        Account firstLock = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? from : to;
        Account secondLock = (firstLock == from) ? to : from;

        synchronized (firstLock) {
            synchronized (secondLock) {
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
        System.out.println("[SafeTransfer] Rs. " + amount + " transferred from " +
                from.getAccountNumber() + " to " + to.getAccountNumber());
    }
}
