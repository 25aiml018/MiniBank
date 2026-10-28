package service;

import model.Account;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Practical 10 & 12: Thread-pool transaction processor with ConcurrentHashMap shared storage
public class TransactionProcessor {

    private final ExecutorService executor;

    // Practical 12 requirement: ConcurrentHashMap for thread-safe concurrent access
    private final ConcurrentHashMap<String, Account> sharedAccounts = new ConcurrentHashMap<>();

    public TransactionProcessor() {
        this.executor = Executors.newFixedThreadPool(4);
    }

    public TransactionProcessor(int poolSize) {
        this.executor = Executors.newFixedThreadPool(poolSize);
    }

    public void registerAccount(Account account) {
        if (account != null && account.getAccountNumber() != null) {
            sharedAccounts.put(account.getAccountNumber(), account);
        }
    }

    public void registerAll(Collection<Account> accounts) {
        if (accounts != null) {
            for (Account acc : accounts) {
                registerAccount(acc);
            }
        }
    }

    public Account getAccount(String accountNumber) {
        return sharedAccounts.get(accountNumber);
    }

    public ConcurrentHashMap<String, Account> getSharedAccounts() {
        return sharedAccounts;
    }

    public void submit(Runnable task) {
        executor.execute(task);
    }

    // Process a batch of transactions and wait for all to complete
    public void processBatch(List<Runnable> tasks) throws InterruptedException {
        if (tasks == null || tasks.isEmpty()) return;

        CountDownLatch latch = new CountDownLatch(tasks.size());
        for (Runnable task : tasks) {
            executor.execute(() -> {
                try {
                    task.run();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(10, TimeUnit.SECONDS);
    }

    public void stop() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
