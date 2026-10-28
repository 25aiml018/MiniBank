package service;

import model.Account;

import java.util.*;

// Practical 12: Collections Framework - HashMap, ArrayList, Comparator, Comparable, and TreeMap
public class AccountStore {

    // Primary storage: HashMap keyed by account number
    private final Map<String, Account> accounts = new HashMap<>();

    // Transaction history kept in an ArrayList<String>
    private final List<String> transactionHistory = new ArrayList<>();

    // Practical 12 requirement: add(Account account)
    public void add(Account account) {
        if (account == null || account.getAccountNumber() == null) {
            throw new IllegalArgumentException("Account or account number cannot be null");
        }
        accounts.put(account.getAccountNumber(), account);
        recordTransaction("Registered Account: " + account.getAccountNumber() + " (" + account.getOwnerName() + ")");
    }

    // Practical 12 requirement: get(String accountNumber)
    public Account get(String accountNumber) {
        return accounts.get(accountNumber);
    }

    // Practical 12 requirement: getAll() returning collection/list
    public ArrayList<Account> getAll() {
        return new ArrayList<>(accounts.values());
    }

    // Transaction history helpers
    public void recordTransaction(String entry) {
        transactionHistory.add(entry);
    }

    public ArrayList<String> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    // Practical 12 requirement: sortedByBalance() using Comparator (highest balance first)
    public ArrayList<Account> sortedByBalance() {
        ArrayList<Account> list = new ArrayList<>(accounts.values());
        list.sort(Comparator.comparingLong(Account::getBalance).reversed());
        return list;
    }

    // Supplementary: returns all accounts with balance above a given threshold
    public ArrayList<Account> findAccountsWithBalanceAbove(long minBalance) {
        ArrayList<Account> result = new ArrayList<>();
        for (Account acc : accounts.values()) {
            if (acc.getBalance() > minBalance) {
                result.add(acc);
            }
        }
        return result;
    }

    // Supplementary: Use a TreeMap so accounts are kept in sorted key order (by account number)
    public TreeMap<String, Account> getAllSortedByAccountNumber() {
        return new TreeMap<>(accounts);
    }

    public int size() {
        return accounts.size();
    }
}
