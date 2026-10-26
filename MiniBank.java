import static model.MenuOption.*;

import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.*;
import service.*;
import util.*;

import java.util.Scanner;

public class MiniBank {

    public static void main(String[] args) {
        // Practical 1: Application Header using BankInfo record
        BankInfo bankInfo = new BankInfo("MiniBank", "Main Branch, Anand");
        System.out.println("==================================================");
        System.out.println("Welcome to " + bankInfo.name() + " (" + bankInfo.branch() + ")");
        System.out.println("==================================================");

        // If user wants interactive menu, run it:
        if (args.length > 0 && args[0].equalsIgnoreCase("menu")) {
            runMenuShell();
            return;
        }

        // Otherwise, run through demonstrations of Practical 1 to Practical 10:

        System.out.println("\n--- Practical 1: Menu Shell Preview ---");
        simulateMenuOption(OPEN_ACCOUNT);
        simulateMenuOption(DEPOSIT);
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
        // toString
        System.out.println("Account toString(): " + accounts[0]);

        // equals & hashCode
        Account copyAccount = new SavingsAccount(accounts[0].getAccountNumber(), "Rudra Copy", 10000, 2000);
        System.out.println("accounts[0].equals(copyAccount): " + accounts[0].equals(copyAccount));

        // instanceof check
        if (accounts[0] instanceof Account) {
            System.out.println("accounts[0] is an instance of Account");
        }

        // Nested Address & Cloneable
        Customer.Address addr = new Customer.Address("12 Station Road", "Anand", "388001");
        cust1.setAddress(addr);
        Customer clonedCust = cust1.clone();
        System.out.println("Original Customer: " + cust1);
        System.out.println("Cloned Customer:   " + clonedCust);

        System.out.println("\n--- Practical 4: Regex Validator, CommandParser & Statement ---");
        // Regex validations (one valid, one invalid each)
        System.out.println("Mobile Valid (9876543210): " + Validator.isValidMobile("9876543210"));
        System.out.println("Mobile Invalid (12345):    " + Validator.isValidMobile("12345"));
        System.out.println("Email Valid (user@bank.com): " + Validator.isValidEmail("user@bank.com"));
        System.out.println("Email Invalid (bad-email):   " + Validator.isValidEmail("bad-email"));
        System.out.println("PAN Valid (ABCDE1234F):    " + Validator.isValidPan("ABCDE1234F"));
        System.out.println("PAN Invalid (12345):       " + Validator.isValidPan("12345"));
        System.out.println("IFSC Valid (SBIN0001234):  " + Validator.isValidIfsc("SBIN0001234"));
        System.out.println("IFSC Invalid (SBI):        " + Validator.isValidIfsc("SBI"));

        // CommandParser
        Command cmd = CommandParser.parse("DEPOSIT AC0001 500");
        System.out.println("Parsed Command -> Type: " + cmd.type() + ", Account: " + cmd.accountNumber() + ", Amount: " + cmd.amount());

        // StatementFormatter
        System.out.println(StatementFormatter.buildStatement(accounts[0]));

        System.out.println("\n--- Practical 5: Polymorphism & Pattern Matching instanceof ---");
        for (Account acc : accounts) {
            System.out.println(acc.getOwnerName() + " Interest Rate: " + acc.interestRate() + "%");

            // Pattern matching instanceof
            if (acc instanceof SavingsAccount sa) {
                System.out.println("  -> Handled specially as SavingsAccount (Min Balance: Rs. " + sa.getMinBalance() + ")");
            }
        }

        System.out.println("\n--- Practical 6: Interfaces, Anonymous Class & Lambda ---");
        // Default method from InterestBearing interface
        System.out.println("accounts[0] Yearly Interest: Rs. " + accounts[0].yearlyInterest());

        // WithdrawRule with Anonymous Class
        WithdrawRule rule1 = new WithdrawRule() {
            @Override
            public boolean allow(Account account, long amount) {
                return account.canWithdraw(amount);
            }
        };
        System.out.println("Anonymous class rule check: " + rule1.allow(accounts[0], 1000));

        // WithdrawRule with Lambda Expression
        WithdrawRule rule2 = (account, amount) -> account.canWithdraw(amount);
        System.out.println("Lambda expression rule check: " + rule2.allow(accounts[1], 1000));

        System.out.println("\n--- Practical 7: Custom Annotations & Reflection Validator ---");
        Account testAccount = new SavingsAccount("Test User", -5000, 1000);
        String[] errors = AnnotationValidator.validate(testAccount);
        for (String err : errors) {
            System.out.println("Validation Error: " + err);
        }

        System.out.println("\n--- Practical 8: Exception Handling, Transfer & Try-With-Resources ---");
        // Try-with-resources with AutoCloseable BankSession
        try (BankSession session = new BankSession("Rudra")) {
            session.logActivity("Executing transfer");
            accounts[0].transfer(accounts[1], 500);
        } catch (BankException e) {
            System.out.println("Banking Error caught: " + e.getMessage());
        } finally {
            System.out.println("Finally block executed after transfer.");
        }

        // Test InsufficientFundsException
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
            System.out.println("Thread lifecycle: " + threads[i].getName() + " State: " + threads[i].getState());
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
        // 1. Thread Pool with ExecutorService
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

        // 2. Simple Producer-Consumer using wait() & notify()
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

        // 3. Safe Transfer avoiding deadlock
        try {
            BankService.safeTransfer(accounts[0], accounts[1], 100);
        } catch (BankException e) {
            System.out.println("Transfer error: " + e.getMessage());
        }

        System.out.println("\nAll practical demonstrations completed successfully!");
        System.out.println("Tip: Run 'java MiniBank menu' to start the interactive console menu.");
    }

    // Practical 1: Menu helper for switch expression
    private static void simulateMenuOption(MenuOption option) {
        String message = switch (option) {
            case OPEN_ACCOUNT -> "Open Account — to be implemented in a later lab";
            case DEPOSIT -> "Deposit — to be implemented in a later lab";
            case WITHDRAW -> "Withdraw — to be implemented in a later lab";
            case TRANSFER -> "Transfer — to be implemented in a later lab";
            case EXIT -> "Goodbye! Thank you for using MiniBank.";
        };
        System.out.println("[" + option + "] -> " + message);
    }

    // Practical 1: Interactive Menu Shell
    public static void runMenuShell() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n========== MINIBANK MENU ==========");
            System.out.println("1. Open Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Exit");
            System.out.print("Enter your choice (1-5): ");

            if (!scanner.hasNextInt()) {
                System.out.println("Please enter a valid number!");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            MenuOption option = switch (choice) {
                case 1 -> OPEN_ACCOUNT;
                case 2 -> DEPOSIT;
                case 3 -> WITHDRAW;
                case 4 -> TRANSFER;
                case 5 -> EXIT;
                default -> null;
            };

            if (option == null) {
                System.out.println("Invalid option! Please select between 1 and 5.");
                continue;
            }

            simulateMenuOption(option);
            if (option == EXIT) {
                running = false;
            }
        }
    }
}