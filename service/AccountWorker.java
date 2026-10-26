package service;

import exception.InvalidAmountException;
import model.Account;

// Practical 9: Runnable worker that repeatedly deposits into an account
public class AccountWorker implements Runnable {

    private final Account account;
    private final int times;
    private final long amount;

    public AccountWorker(Account account, int times, long amount) {
        this.account = account;
        this.times = times;
        this.amount = amount;
    }

    @Override
    public void run() {
        for (int i = 0; i < times; i++) {
            try {
                account.deposit(amount);
            } catch (InvalidAmountException e) {
                System.out.println("Deposit error: " + e.getMessage());
            }
        }
    }
}
