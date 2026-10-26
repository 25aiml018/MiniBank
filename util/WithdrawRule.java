package util;

import model.Account;

// Practical 6 & 7: Functional interface for custom withdrawal rules
@FunctionalInterface
public interface WithdrawRule {
    boolean allow(Account account, long amount);
}