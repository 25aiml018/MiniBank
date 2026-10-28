import static model.MenuOption.*;

import exception.AccountNotFoundException;
import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.*;
import service.*;
import util.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

// Practical 1 to 14: Comprehensive MiniBank console banking application
public class MiniBank {

    public static void main(String[] args) {
        // Practical 1: Application Header using BankInfo record
        BankInfo bankInfo = new BankInfo("MiniBank", "Main Branch, Anand");
        System.out.println("==================================================");
        System.out.println("Welcome to " + bankInfo.name() + " (" + bankInfo.branch() + ")");
        System.out.println("==================================================");

        // If user wants interactive menu, run it:
        if (args.length > 0 && args[0].equalsIgnoreCase("menu")) {
            runInteractiveMenu();
            return;
        }

        // If user wants to run automated tests:
        if (args.length > 0 && args[0].equalsIgnoreCase("test")) {
            runAutomatedTests();
            return;
        }

        // Otherwise, run through demonstrations of Practicals 1 to 14:
        runAllPracticalDemonstrations();
    }

    public static void runAllPracticalDemonstrations() {
        System.out.println("\n--- Practical 1: Menu Shell Preview & Bank Working Hours ---");
        simulateMenuOption(OPEN_ACCOUNT);
        simulateMenuOption(DEPOSIT);
        simulateMenuOption(WORKING_HOURS);
        simulateMenuOption(EXIT);

        System.out.println("\n--- Practical 2: Customer & Account (Encapsulation) ---");
        Customer cust1 = new Customer("Rudra Gharaliya", "rudra@example.com", "9876543210");
        System.out.println("Created: " + cust1);

        Account[] accounts = {
                new SavingsAccount("Rudra", 10000, 2000),
                new CurrentAccount("Rahul", 5000, 2000),
                new FixedDepositAccount("Amit", 20000)
        };

        try {
            accounts[0].deposit(1500);
            accounts[0].withdraw(500);
            accounts[1].deposit(2000);
        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        }

        for (Account acc : accounts) {
            System.out.println(acc.getAccountNumber() + " (" + acc.getOwnerName() + ") Balance: Rs. " + acc.getBalance());
        }

        System.out.println("\n--- Practical 3: toString, equals, Address & Clone ---");
        System.out.println("Account toString(): " + accounts[0]);

        Account copyAccount = new SavingsAccount(accounts[0].getAccountNumber(), "Rudra Copy", 10000, 2000);
        System.out.println("accounts[0].equals(copyAccount): " + accounts[0].equals(copyAccount));

        if (accounts[0] instanceof Account) {
            System.out.println("accounts[0] is an instance of Account");
        }

        Customer.Address addr = new Customer.Address("12 Station Road", "Anand", "388001");
        cust1.setAddress(addr);
        Customer clonedCust = cust1.clone();
        System.out.println("Original Customer: " + cust1);
        System.out.println("Cloned Customer:   " + clonedCust);

        System.out.println("\n--- Practical 4: Regex Validator, CommandParser & Statement ---");
        System.out.println("Mobile Valid (9876543210): " + Validator.isValidMobile("9876543210"));
        System.out.println("Mobile Invalid (12345):    " + Validator.isValidMobile("12345"));
        System.out.println("Email Valid (user@bank.com): " + Validator.isValidEmail("user@bank.com"));
        System.out.println("Email Invalid (bad-email):   " + Validator.isValidEmail("bad-email"));
        System.out.println("PAN Valid (ABCDE1234F):    " + Validator.isValidPan("ABCDE1234F"));
        System.out.println("PAN Invalid (12345):       " + Validator.isValidPan("12345"));
        System.out.println("IFSC Valid (SBIN0001234):  " + Validator.isValidIfsc("SBIN0001234"));
        System.out.println("IFSC Invalid (SBI):        " + Validator.isValidIfsc("SBI"));

        Command cmd = CommandParser.parse("DEPOSIT AC0001 500");
        System.out.println("Parsed Command -> Type: " + cmd.type() + ", Account: " + cmd.accountNumber() + ", Amount: " + cmd.amount());
        System.out.println(StatementFormatter.buildStatement(accounts[0]));

        System.out.println("\n--- Practical 5: Polymorphism & Pattern Matching instanceof ---");
        for (Account acc : accounts) {
            System.out.println(acc.getOwnerName() + " Interest Rate: " + acc.interestRate() + "%");
            if (acc instanceof SavingsAccount sa) {
                System.out.println("  -> Handled specially as SavingsAccount (Min Balance: Rs. " + sa.getMinBalance() + ")");
            }
        }

        System.out.println("\n--- Practical 6: Interfaces, Anonymous Class & Lambda ---");
        System.out.println("accounts[0] Yearly Interest: Rs. " + accounts[0].yearlyInterest());

        WithdrawRule rule1 = new WithdrawRule() {
            @Override
            public boolean allow(Account account, long amount) {
                return account.canWithdraw(amount);
            }
        };
        System.out.println("Anonymous class rule check: " + rule1.allow(accounts[0], 1000));

        WithdrawRule rule2 = (account, amount) -> account.canWithdraw(amount);
        System.out.println("Lambda expression rule check: " + rule2.allow(accounts[1], 1000));

        System.out.println("\n--- Practical 7: Custom Annotations & Reflection Validator ---");
        Account testAccount = new SavingsAccount("Test User", -5000, 1000);
        String[] errors = AnnotationValidator.validate(testAccount);
        for (String err : errors) {
            System.out.println("Validation Error: " + err);
        }

        System.out.println("\n--- Practical 8: Exception Handling, Transfer & Try-With-Resources ---");
        try (BankSession session = new BankSession("Rudra")) {
            session.logActivity("Executing transfer");
            accounts[0].transfer(accounts[1], 500);
        } catch (BankException e) {
            System.out.println("Banking Error caught: " + e.getMessage());
        } finally {
            System.out.println("Finally block executed after transfer.");
        }

        try {
            accounts[0].withdraw(50000);
        } catch (InsufficientFundsException e) {
            System.out.println("Caught InsufficientFundsException: Shortfall = Rs. " + e.getShortfall());
        } catch (InvalidAmountException e) {
            System.out.println("Caught InvalidAmountException: " + e.getMessage());
        }

        System.out.println("\n--- Practical 9: Multithreading & Synchronization ---");
        Account sharedAccount = new SavingsAccount("Shared User", 1000, 0);
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(new AccountWorker(sharedAccount, 100, 10), "Worker-" + (i + 1));
            threads[i].start();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Final Balance after 10 threads deposited (Synchronized): Rs. " + sharedAccount.getBalance());

        System.out.println("\n--- Practical 10: Thread Pool, Producer-Consumer & Safe Transfer ---");
        TransactionProcessor processor = new TransactionProcessor();
        for (int i = 0; i < 5; i++) {
            final int taskId = i + 1;
            processor.submit(() -> {
                try {
                    sharedAccount.deposit(50);
                    System.out.println("Task " + taskId + " deposited Rs. 50 in thread " + Thread.currentThread().getName());
                } catch (InvalidAmountException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            });
        }
        processor.stop();

        TransactionQueue queue = new TransactionQueue(2);
        Thread producer = new Thread(() -> {
            try {
                queue.produce("TXN-101: Deposit Rs. 500");
                queue.produce("TXN-102: Withdraw Rs. 200");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread consumer = new Thread(() -> {
            try {
                queue.consume();
                queue.consume();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        producer.start();
        consumer.start();
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            BankService.safeTransfer(accounts[0], accounts[1], 100);
        } catch (BankException e) {
            System.out.println("Transfer error: " + e.getMessage());
        }

        // ==========================================
        // PRACTICAL 11: Persistence & NIO Reporting
        // ==========================================
        System.out.println("\n--- Practical 11: Serialization, Transient Field, Log & NIO Report ---");
        Path p11Data = Paths.get("data", "accounts_demo.dat");
        Path p11Log = Paths.get("logs", "demo.log");
        Path p11Report = Paths.get("reports", "demo_summary.txt");

        try {
            // Set transient field before saving
            accounts[0].setSessionToken("SECRET_SESSION_TOKEN_123");
            System.out.println("Before saving, accounts[0] sessionToken: " + accounts[0].getSessionToken());

            // 1. Save array of accounts
            StatePersister.save(accounts, p11Data);
            System.out.println("Saved " + accounts.length + " accounts to " + p11Data);

            // 2. Reload array of accounts
            Account[] reloaded = StatePersister.load(p11Data);
            System.out.println("Reloaded " + reloaded.length + " accounts successfully.");
            System.out.println("After reload, transient sessionToken: " + reloaded[0].getSessionToken() + " (null proves transient works!)");

            // 3. Append to transaction log
            TransactionLog.append(p11Log, "DEPOSIT AC0001 500 [demo]");
            TransactionLog.append(p11Log, "WITHDRAW AC0002 200 [demo]");
            System.out.println("Appended entries to " + p11Log);

            // 4. Generate report with ReportGenerator
            String report = ReportGenerator.generateReport(Paths.get("logs"), p11Report);
            System.out.println(report);
        } catch (Exception e) {
            System.out.println("Practical 11 Demo Exception: " + e.getMessage());
        }

        // ==========================================
        // PRACTICAL 12: Collections Framework & ConcurrentHashMap
        // ==========================================
        System.out.println("\n--- Practical 12: Collections Framework (AccountStore, Comparator, Comparable) ---");
        AccountStore store = new AccountStore();
        Account accA = new SavingsAccount("User 5000", 5000, 1000);
        Account accB = new CurrentAccount("User 2000", 2000, 500);
        Account accC = new FixedDepositAccount("User 9000", 9000);

        store.add(accA);
        store.add(accB);
        store.add(accC);

        System.out.println("Fetched by ID (accB): " + store.get(accB.getAccountNumber()));

        // Sorted by balance with Comparator
        System.out.println("Accounts sorted by balance (Highest to Lowest):");
        for (Account a : store.sortedByBalance()) {
            System.out.println("  " + a.getAccountNumber() + " | " + a.getOwnerName() + " | Rs. " + a.getBalance());
        }

        // Comparable compareTo check
        System.out.println("Comparable check accA.compareTo(accB): " + accA.compareTo(accB));

        // Supplementary: Accounts with balance above 3000
        System.out.println("Accounts with balance > 3000: " + store.findAccountsWithBalanceAbove(3000).size());

        // ConcurrentHashMap in TransactionProcessor
        TransactionProcessor concurrentProcessor = new TransactionProcessor();
        concurrentProcessor.registerAccount(accA);
        System.out.println("Registered accA in ConcurrentHashMap: " + concurrentProcessor.getAccount(accA.getAccountNumber()).getAccountNumber());
        concurrentProcessor.stop();

        // ==========================================
        // PRACTICAL 13: Generic Repository<T> with Reflection
        // ==========================================
        System.out.println("\n--- Practical 13: Generic Repository<T> (@Id Reflection, @SafeVarargs, Bounded Avg) ---");
        Repository<Account> accountRepo = new Repository<>();
        accountRepo.saveAll(accA, accB, accC);
        System.out.println("Saved " + accountRepo.count() + " accounts via @SafeVarargs saveAll().");
        System.out.println("findById(" + accA.getAccountNumber() + "): " + accountRepo.findById(accA.getAccountNumber()));

        // Reused for Customer!
        Repository<Customer> customerRepo = new Repository<>();
        Customer cust2 = new Customer("Anita Sharma", "anita@bank.com", "9823456789");
        customerRepo.save(cust2);
        System.out.println("Generic Repository reused for Customer: " + customerRepo.findById(cust2.getCustomerId()));

        // Bounded type parameter average method
        Integer[] integerVals = {10, 20, 30, 40, 50};
        Double[] doubleVals = {12.5, 25.0, 37.5};
        System.out.println("Bounded generic average (Integer[]): " + Repository.average(integerVals));
        System.out.println("Bounded generic average (Double[]):  " + Repository.average(doubleVals));

        // ==========================================
        // PRACTICAL 14: Layered BankService Capstone & Automated Tests
        // ==========================================
        System.out.println("\n--- Practical 14: Capstone BankService & Batch Integration ---");
        BankService service = new BankService();
        try {
            Account user1 = service.openAccount("SAVINGS", "Kavita", 15000, 2000);
            Account user2 = service.openAccount("CURRENT", "Vijay", 8000, 3000);

            service.deposit(user1.getAccountNumber(), 3000);
            service.withdraw(user2.getAccountNumber(), 1000);
            service.transfer(user1.getAccountNumber(), user2.getAccountNumber(), 2000);

            System.out.println("Kavita Balance: Rs. " + user1.getBalance());
            System.out.println("Vijay Balance:  Rs. " + user2.getBalance());

            // Run batch of commands
            List<Command> batchCommands = List.of(
                    new Command(TransactionType.DEPOSIT, user1.getAccountNumber(), 500),
                    new Command(TransactionType.WITHDRAW, user2.getAccountNumber(), 300)
            );
            service.batchProcessCommands(batchCommands);
            System.out.println("Batch processing completed successfully.");

            // Save state
            service.saveState();
        } catch (Exception e) {
            System.out.println("BankService Error: " + e.getMessage());
        } finally {
            service.shutdown();
        }

        System.out.println("\n==================================================");
        System.out.println("All Practical 1-14 demonstrations completed successfully!");
        System.out.println("Run 'java MiniBank menu' to start the interactive banking shell.");
        System.out.println("Run 'java MiniBank test' to run the automated JUnit test suite.");
        System.out.println("==================================================");
    }

    // Practical 1: Menu helper for switch expression
    private static void simulateMenuOption(MenuOption option) {
        String message = switch (option) {
            case OPEN_ACCOUNT -> "Open Account — create Savings, Current, or Fixed Deposit";
            case DEPOSIT -> "Deposit — credit funds to an account";
            case WITHDRAW -> "Withdraw — debit funds with overdraft/minimum balance checks";
            case TRANSFER -> "Transfer — deadlock-free fund transfer between accounts";
            case STATEMENT -> "Statement — view formatted transaction & balance summary";
            case LIST_ACCOUNTS -> "List Accounts — view all accounts sorted by balance";
            case BATCH_TRANSACTIONS -> "Batch Transactions — concurrent batch transaction processing";
            case GENERATE_REPORT -> "Generate Report — NIO directory stream end-of-day summary";
            case SEARCH_ACCOUNT -> "Search Account — find accounts by customer name";
            case WORKING_HOURS -> BankService.getWorkingHours();
            case EXIT -> "Goodbye! Thank you for using MiniBank.";
        };
        System.out.println("[" + option + "] -> " + message);
    }

    // Practical 1 & 14: Interactive Menu backed by BankService and persistence
    public static void runInteractiveMenu() {
        BankService service = new BankService();
        service.loadState(); // Practical 11 & 14: restore persisted state

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("\nStarting MiniBank Interactive Console Banking...");

        while (running) {
            System.out.println("\n=================== MINIBANK MENU ===================");
            System.out.println(" 1. Open Account");
            System.out.println(" 2. Deposit Funds");
            System.out.println(" 3. Withdraw Funds");
            System.out.println(" 4. Transfer Funds (Deadlock-Free)");
            System.out.println(" 5. View Account Statement");
            System.out.println(" 6. List All Accounts (Sorted by Balance)");
            System.out.println(" 7. Run Concurrent Batch Transactions");
            System.out.println(" 8. Generate End-of-Day NIO Report");
            System.out.println(" 9. Search Accounts by Owner Name");
            System.out.println("10. View Bank Working Hours");
            System.out.println("11. Save & Exit");
            System.out.print("Enter your choice (1-11): ");

            if (!scanner.hasNextInt()) {
                System.out.println("Please enter a valid number!");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            MenuOption option = switch (choice) {
                case 1 -> OPEN_ACCOUNT;
                case 2 -> DEPOSIT;
                case 3 -> WITHDRAW;
                case 4 -> TRANSFER;
                case 5 -> STATEMENT;
                case 6 -> LIST_ACCOUNTS;
                case 7 -> BATCH_TRANSACTIONS;
                case 8 -> GENERATE_REPORT;
                case 9 -> SEARCH_ACCOUNT;
                case 10 -> WORKING_HOURS;
                case 11 -> EXIT;
                default -> null;
            };

            if (option == null) {
                System.out.println("Invalid option! Please enter a number between 1 and 11.");
                continue;
            }

            try {
                switch (option) {
                    case OPEN_ACCOUNT -> {
                        System.out.print("Enter Account Type (SAVINGS / CURRENT / FIXED): ");
                        String type = scanner.nextLine().trim();
                        System.out.print("Enter Owner Name: ");
                        String owner = scanner.nextLine().trim();
                        System.out.print("Enter Opening Balance (whole Rs.): ");
                        long initialBalance = Long.parseLong(scanner.nextLine().trim());

                        long extra = 0;
                        if (type.equalsIgnoreCase("SAVINGS")) {
                            System.out.print("Enter Minimum Balance required [default 1000]: ");
                            String in = scanner.nextLine().trim();
                            extra = in.isEmpty() ? 1000 : Long.parseLong(in);
                        } else if (type.equalsIgnoreCase("CURRENT")) {
                            System.out.print("Enter Overdraft Limit [default 5000]: ");
                            String in = scanner.nextLine().trim();
                            extra = in.isEmpty() ? 5000 : Long.parseLong(in);
                        }

                        Account created = service.openAccount(type, owner, initialBalance, extra);
                        System.out.println(">> Account created successfully! Account Number: " + created.getAccountNumber());
                    }

                    case DEPOSIT -> {
                        System.out.print("Enter Account Number (e.g. AC0001): ");
                        String accNo = scanner.nextLine().trim();
                        System.out.print("Enter Deposit Amount (Rs.): ");
                        long amount = Long.parseLong(scanner.nextLine().trim());
                        service.deposit(accNo, amount);
                        Account acc = service.findAccount(accNo);
                        System.out.println(">> Deposit successful! New Balance: Rs. " + acc.getBalance());
                    }

                    case WITHDRAW -> {
                        System.out.print("Enter Account Number: ");
                        String accNo = scanner.nextLine().trim();
                        System.out.print("Enter Withdrawal Amount (Rs.): ");
                        long amount = Long.parseLong(scanner.nextLine().trim());
                        service.withdraw(accNo, amount);
                        Account acc = service.findAccount(accNo);
                        System.out.println(">> Withdrawal successful! New Balance: Rs. " + acc.getBalance());
                    }

                    case TRANSFER -> {
                        System.out.print("Enter Source Account Number: ");
                        String fromAcc = scanner.nextLine().trim();
                        System.out.print("Enter Destination Account Number: ");
                        String toAcc = scanner.nextLine().trim();
                        System.out.print("Enter Transfer Amount (Rs.): ");
                        long amount = Long.parseLong(scanner.nextLine().trim());
                        service.transfer(fromAcc, toAcc, amount);
                        System.out.println(">> Transfer completed safely without deadlock!");
                    }

                    case STATEMENT -> {
                        System.out.print("Enter Account Number: ");
                        String accNo = scanner.nextLine().trim();
                        System.out.println(service.statement(accNo));
                    }

                    case LIST_ACCOUNTS -> {
                        ArrayList<Account> accounts = service.listAccountsSortedByBalance();
                        if (accounts.isEmpty()) {
                            System.out.println(">> No accounts registered yet.");
                        } else {
                            System.out.println("\n--- All Accounts (Sorted by Balance) ---");
                            for (Account a : accounts) {
                                System.out.println(a.getAccountNumber() + " | " + a.getOwnerName() +
                                        " | Balance: Rs. " + a.getBalance() + " | Rate: " + a.interestRate() + "%");
                            }
                        }
                    }

                    case BATCH_TRANSACTIONS -> {
                        ArrayList<Account> all = service.listAccounts();
                        if (all.size() < 2) {
                            System.out.println(">> Need at least 2 accounts to run batch test. Please open accounts first.");
                        } else {
                            String a1 = all.get(0).getAccountNumber();
                            String a2 = all.get(1).getAccountNumber();
                            System.out.println(">> Submitting batch transactions on thread pool...");
                            List<Command> batch = List.of(
                                    new Command(TransactionType.DEPOSIT, a1, 100),
                                    new Command(TransactionType.DEPOSIT, a2, 200),
                                    new Command(TransactionType.WITHDRAW, a1, 50)
                            );
                            service.batchProcessCommands(batch);
                            System.out.println(">> Batch transactions completed!");
                        }
                    }

                    case GENERATE_REPORT -> {
                        System.out.println(service.generateReport());
                    }

                    case SEARCH_ACCOUNT -> {
                        System.out.print("Enter Customer Name to search: ");
                        String query = scanner.nextLine().trim();
                        List<Account> results = service.searchByOwnerName(query);
                        if (results.isEmpty()) {
                            System.out.println(">> No accounts found matching '" + query + "'.");
                        } else {
                            System.out.println(">> Matching Accounts (" + results.size() + "):");
                            for (Account a : results) {
                                System.out.println("   * " + a);
                            }
                        }
                    }

                    case WORKING_HOURS -> {
                        System.out.println(BankService.getWorkingHours());
                    }

                    case EXIT -> {
                        System.out.println("Persisting system state to disk before exiting...");
                        service.saveState();
                        System.out.println("State saved. Goodbye! Thank you for using MiniBank.");
                        running = false;
                    }
                }
            } catch (InsufficientFundsException e) {
                System.out.println(">> Withdrawal Failed: " + e.getMessage() + " (Shortfall: Rs. " + e.getShortfall() + ")");
            } catch (BankException e) {
                System.out.println(">> Banking Operation Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println(">> Unexpected Error: " + e.getMessage());
            }
        }

        service.shutdown();
    }

    // Practical 14: Built-in automated test executor
    public static void runAutomatedTests() {
        System.out.println("==================================================");
        System.out.println("         RUNNING MINIBANK JUNIT 5 TESTS           ");
        System.out.println("==================================================");

        try {
            org.junit.platform.console.ConsoleLauncher.main(new String[]{
                    "execute",
                    "--class-path", ".",
                    "--select-class", "test.MiniBankTest"
            });
        } catch (Throwable t) {
            System.out.println("JUnit ConsoleLauncher executed. Status: " + t.getMessage());
        }
    }
}