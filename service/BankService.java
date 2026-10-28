package service;

import exception.AccountNotFoundException;
import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.*;
import model.annotation.Audit;
import util.StatementFormatter;
import util.Validator;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Practical 10 & 14: Central BankService wiring Repository, Validator, Exceptions, ThreadPool & Audit logging
public class BankService {

    private final Repository<Account> repository;
    private final TransactionProcessor processor;
    private final Path dataPath;
    private final Path logsDir;
    private final Path currentLogPath;
    private final Path reportPath;

    public BankService() {
        this(
                Paths.get("data", "accounts.dat"),
                Paths.get("logs"),
                Paths.get("reports", "eod_summary_report.txt")
        );
    }

    public BankService(Path dataPath, Path logsDir, Path reportPath) {
        this.repository = new Repository<>();
        this.processor = new TransactionProcessor(4);
        this.dataPath = dataPath;
        this.logsDir = logsDir;
        this.currentLogPath = logsDir.resolve("transactions.log");
        this.reportPath = reportPath;
    }

    // Practical 14: openAccount
    @Audit(action = "OPEN_ACCOUNT")
    public Account openAccount(Account account) throws BankException {
        if (account == null) {
            throw new BankException("Account cannot be null");
        }
        if (account.getBalance() < 0) {
            throw new InvalidAmountException("Initial balance cannot be negative: " + account.getBalance());
        }

        repository.save(account);
        processor.registerAccount(account);
        auditMethodCall("openAccount", account.getAccountNumber(), account.getOwnerName(), account.getBalance());

        try {
            TransactionLog.log(currentLogPath, "OPEN_ACCOUNT", account.getAccountNumber(), account.getBalance());
        } catch (IOException e) {
            System.err.println("Warning: Failed to write to transaction log: " + e.getMessage());
        }

        return account;
    }

    // Overload for creating from parameters
    @Audit(action = "OPEN_ACCOUNT")
    public Account openAccount(String type, String ownerName, long initialBalance, long extraParam) throws BankException {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new BankException("Owner name cannot be empty");
        }
        if (initialBalance < 0) {
            throw new InvalidAmountException("Initial balance cannot be negative");
        }

        Account account;
        String normalizedType = type.trim().toUpperCase();
        switch (normalizedType) {
            case "SAVINGS" -> account = new SavingsAccount(ownerName, initialBalance, extraParam > 0 ? extraParam : 1000);
            case "CURRENT" -> account = new CurrentAccount(ownerName, initialBalance, extraParam > 0 ? extraParam : 5000);
            case "FIXED", "FD", "FIXED_DEPOSIT" -> account = new FixedDepositAccount(ownerName, initialBalance);
            default -> throw new BankException("Unknown account type: " + type + ". Choose SAVINGS, CURRENT, or FIXED.");
        }

        return openAccount(account);
    }

    // Practical 14: deposit
    @Audit(action = "DEPOSIT")
    public void deposit(String accountNumber, long amount) throws BankException {
        Account account = findAccountOrThrow(accountNumber);
        account.deposit(amount);
        auditMethodCall("deposit", accountNumber, amount);

        try {
            TransactionLog.log(currentLogPath, "DEPOSIT", accountNumber, amount);
        } catch (IOException e) {
            System.err.println("Warning: Failed to write to transaction log: " + e.getMessage());
        }
    }

    // Practical 14: withdraw
    @Audit(action = "WITHDRAW")
    public void withdraw(String accountNumber, long amount) throws BankException {
        Account account = findAccountOrThrow(accountNumber);
        account.withdraw(amount);
        auditMethodCall("withdraw", accountNumber, amount);

        try {
            TransactionLog.log(currentLogPath, "WITHDRAW", accountNumber, amount);
        } catch (IOException e) {
            System.err.println("Warning: Failed to write to transaction log: " + e.getMessage());
        }
    }

    // Practical 14: transfer between two accounts with deadlock avoidance
    @Audit(action = "TRANSFER")
    public void transfer(String fromAccountNumber, String toAccountNumber, long amount) throws BankException {
        if (fromAccountNumber == null || toAccountNumber == null) {
            throw new BankException("Account numbers cannot be null");
        }
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new BankException("Cannot transfer to the same account");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be positive: " + amount);
        }

        Account from = findAccountOrThrow(fromAccountNumber);
        Account to = findAccountOrThrow(toAccountNumber);

        safeTransfer(from, to, amount);
        auditMethodCall("transfer", fromAccountNumber, toAccountNumber, amount);

        try {
            TransactionLog.log(currentLogPath, "TRANSFER", fromAccountNumber, toAccountNumber, amount);
        } catch (IOException e) {
            System.err.println("Warning: Failed to write to transaction log: " + e.getMessage());
        }
    }

    // Practical 10 & 14: Safe transfer using ordered locking to guarantee deadlock freedom
    public static void safeTransfer(Account from, Account to, long amount) throws BankException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Accounts cannot be null");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be positive: " + amount);
        }

        Account firstLock = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? from : to;
        Account secondLock = (firstLock == from) ? to : from;

        synchronized (firstLock) {
            synchronized (secondLock) {
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
    }

    // Practical 14: statement
    @Audit(action = "STATEMENT")
    public String statement(String accountNumber) throws BankException {
        Account account = findAccountOrThrow(accountNumber);
        auditMethodCall("statement", accountNumber);
        return StatementFormatter.buildStatement(account);
    }

    // Practical 14: listAccounts
    @Audit(action = "LIST_ACCOUNTS")
    public ArrayList<Account> listAccounts() {
        auditMethodCall("listAccounts");
        return repository.findAll();
    }

    // Practical 12 & 14: list sorted by balance (highest first)
    public ArrayList<Account> listAccountsSortedByBalance() {
        ArrayList<Account> list = repository.findAll();
        list.sort(Comparator.comparingLong(Account::getBalance).reversed());
        return list;
    }

    // Supplementary: search account by owner name
    public List<Account> searchByOwnerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        String query = name.trim().toLowerCase();
        List<Account> matches = new ArrayList<>();
        for (Account acc : repository.findAll()) {
            if (acc.getOwnerName() != null && acc.getOwnerName().toLowerCase().contains(query)) {
                matches.add(acc);
            }
        }
        return matches;
    }

    // Practical 14: batch processing using TransactionProcessor
    public void batchProcessCommands(List<Command> commands) throws InterruptedException {
        if (commands == null || commands.isEmpty()) return;

        List<Runnable> tasks = new ArrayList<>();
        for (Command cmd : commands) {
            tasks.add(() -> {
                try {
                    switch (cmd.type()) {
                        case DEPOSIT -> deposit(cmd.accountNumber(), cmd.amount());
                        case WITHDRAW -> withdraw(cmd.accountNumber(), cmd.amount());
                        default -> System.err.println("Batch command type not supported directly: " + cmd.type());
                    }
                } catch (BankException e) {
                    System.err.println("Batch transaction failed for " + cmd.accountNumber() + ": " + e.getMessage());
                }
            });
        }
        processor.processBatch(tasks);
    }

    // Practical 11 & 14: End-of-Day report generation
    public String generateReport() throws IOException {
        return ReportGenerator.generateReport(logsDir, reportPath);
    }

    // Practical 11 & 14: Persist accounts to disk on exit
    public void saveState() throws IOException {
        ArrayList<Account> all = repository.findAll();
        StatePersister.save(all, dataPath);
        System.out.println("[Persistence] Successfully saved " + all.size() + " accounts to " + dataPath);
    }

    // Practical 11 & 14: Load accounts from disk on startup
    public void loadState() {
        try {
            Account[] accounts = StatePersister.load(dataPath);
            for (Account acc : accounts) {
                repository.save(acc);
                processor.registerAccount(acc);
            }
            if (accounts.length > 0) {
                System.out.println("[Persistence] Successfully restored " + accounts.length + " accounts from " + dataPath);
            }
        } catch (Exception e) {
            System.out.println("[Persistence] No previous data loaded (" + e.getMessage() + "). Starting fresh.");
        }
    }

    public Account findAccount(String accountNumber) {
        return repository.findById(accountNumber);
    }

    private Account findAccountOrThrow(String accountNumber) throws AccountNotFoundException {
        Account account = repository.findById(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        return account;
    }

    public Repository<Account> getRepository() {
        return repository;
    }

    public TransactionProcessor getProcessor() {
        return processor;
    }

    public void shutdown() {
        processor.stop();
    }

    // Practical 1 Supplementary: Bank working hours
    public static String getWorkingHours() {
        return """
                --------------------------------------------------
                             MINIBANK WORKING HOURS
                --------------------------------------------------
                 Monday to Friday : 09:30 AM – 04:30 PM
                 Saturday         : 09:30 AM – 01:30 PM
                                    (2nd & 4th Saturday Holiday)
                 Sunday           : CLOSED
                --------------------------------------------------""";
    }

    // Practical 14 (Optional): Reflective audit logging
    private void auditMethodCall(String methodName, Object... params) {
        try {
            for (Method method : this.getClass().getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.isAnnotationPresent(Audit.class)) {
                    Audit audit = method.getAnnotation(Audit.class);
                    // System.out.println("[AUDIT LOG] Action: " + audit.action() + " | Method: " + methodName);
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }
}
