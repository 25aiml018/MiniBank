package test;

import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;
import util.AnnotationValidator;
import util.Validator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Practical 14: JUnit 5 test suite validating MiniBank core capabilities
public class MiniBankTest {

    private BankService bankService;
    private Path tempDir;
    private Path dataFile;
    private Path logsDir;
    private Path reportFile;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("minibank_test_");
        dataFile = tempDir.resolve("accounts.dat");
        logsDir = tempDir.resolve("logs");
        reportFile = tempDir.resolve("report.txt");
        Files.createDirectories(logsDir);

        bankService = new BankService(dataFile, logsDir, reportFile);
    }

    @AfterEach
    public void tearDown() throws IOException {
        bankService.shutdown();
        if (Files.exists(tempDir)) {
            try (var stream = Files.walk(tempDir)) {
                stream.sorted(java.util.Comparator.reverseOrder())
                      .forEach(path -> {
                          try {
                              Files.deleteIfExists(path);
                          } catch (IOException ignored) {}
                      });
            }
        }
    }

    // 1. Practical 14 Requirement: Deposit increases balance
    @Test
    public void testDepositIncreasesBalance() throws BankException {
        Account account = bankService.openAccount("SAVINGS", "Rudra", 5000, 1000);
        long initialBalance = account.getBalance();

        bankService.deposit(account.getAccountNumber(), 2500);

        assertEquals(initialBalance + 2500, account.getBalance(), "Balance must increase by deposited amount");
    }

    // 2. Practical 14 Requirement: Withdrawal over limit throws InsufficientFundsException
    @Test
    public void testWithdrawalOverLimitThrowsException() throws BankException {
        Account account = bankService.openAccount("SAVINGS", "Amit", 2000, 1000);

        // Attempting to withdraw 1500 would leave balance at 500 which is less than minBalance 1000
        InsufficientFundsException thrown = assertThrows(
                InsufficientFundsException.class,
                () -> bankService.withdraw(account.getAccountNumber(), 1500),
                "Should throw InsufficientFundsException when minimum balance rule is violated"
        );

        assertTrue(thrown.getShortfall() > 0, "Shortfall must be positive");
        assertEquals(2000, account.getBalance(), "Balance must remain unchanged after failed withdrawal");
    }

    // 3. Practical 14 Requirement: Many concurrent deposits keep the balance correct
    @Test
    public void testConcurrentDepositsKeepBalanceCorrect() throws Exception {
        Account account = bankService.openAccount("SAVINGS", "ConcurrentTester", 0, 0);

        int numThreads = 10;
        int depositsPerThread = 500;
        long depositAmount = 2;

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new AccountWorker(account, depositsPerThread, depositAmount));
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        long expectedBalance = (long) numThreads * depositsPerThread * depositAmount;
        assertEquals(expectedBalance, account.getBalance(), "Synchronized deposits must match exact total");
    }

    // 4. Practical 14 Supplementary: State persistence save and load preserves data
    @Test
    public void testStatePersistenceSaveAndLoad() throws Exception {
        Account acc1 = new SavingsAccount("User A", 10000, 2000);
        Account acc2 = new CurrentAccount("User B", 5000, 1000);
        Account[] original = {acc1, acc2};

        Path saveFile = tempDir.resolve("test_persistence.dat");
        StatePersister.save(original, saveFile);

        assertTrue(Files.exists(saveFile), "Persisted file must exist");

        Account[] restored = StatePersister.load(saveFile);
        assertEquals(2, restored.length);
        assertEquals(acc1.getAccountNumber(), restored[0].getAccountNumber());
        assertEquals(acc1.getBalance(), restored[0].getBalance());
        assertEquals(acc2.getAccountNumber(), restored[1].getAccountNumber());
        assertEquals(acc2.getBalance(), restored[1].getBalance());
    }

    // 5. Practical 13: Generic Repository with reflection on @Id
    @Test
    public void testGenericRepositoryWithReflection() {
        Repository<Account> repo = new Repository<>();
        Account a1 = new SavingsAccount("Alice", 3000, 500);
        Account a2 = new CurrentAccount("Bob", 6000, 1000);

        repo.saveAll(a1, a2);

        assertEquals(2, repo.count());
        assertEquals(a1, repo.findById(a1.getAccountNumber()));
        assertEquals(a2, repo.findById(a2.getAccountNumber()));

        // Also verify generic repository works for Customer
        Repository<Customer> custRepo = new Repository<>();
        Customer c1 = new Customer("Test Customer", "cust@bank.com", "9876543210");
        custRepo.save(c1);

        assertNotNull(c1.getCustomerId());
        assertEquals(c1, custRepo.findById(c1.getCustomerId()));
    }

    // 6. Practical 10 & 14: Deadlock-free safe transfer
    @Test
    public void testDeadlockFreeTransfer() throws Exception {
        Account a1 = bankService.openAccount("SAVINGS", "Party A", 10000, 0);
        Account a2 = bankService.openAccount("SAVINGS", "Party B", 10000, 0);

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                try {
                    bankService.transfer(a1.getAccountNumber(), a2.getAccountNumber(), 10);
                } catch (BankException ignored) {}
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                try {
                    bankService.transfer(a2.getAccountNumber(), a1.getAccountNumber(), 10);
                } catch (BankException ignored) {}
            }
        });

        t1.start();
        t2.start();

        t1.join(5000);
        t2.join(5000);

        assertFalse(t1.isAlive(), "Thread 1 must finish without deadlock");
        assertFalse(t2.isAlive(), "Thread 2 must finish without deadlock");
        assertEquals(20000, a1.getBalance() + a2.getBalance(), "Total money in system must be conserved");
    }

    // 7. Practical 4: Regex Validator tests
    @Test
    public void testRegexValidator() {
        assertTrue(Validator.isValidMobile("9876543210"));
        assertFalse(Validator.isValidMobile("12345"));

        assertTrue(Validator.isValidEmail("user@bank.com"));
        assertFalse(Validator.isValidEmail("bad-email"));

        assertTrue(Validator.isValidPan("ABCDE1234F"));
        assertFalse(Validator.isValidPan("12345"));

        assertTrue(Validator.isValidIfsc("SBIN0001234"));
        assertFalse(Validator.isValidIfsc("SBI"));
    }

    // 8. Practical 7: Annotation reflection validator detects negative balance
    @Test
    public void testAnnotationValidatorNegativeBalance() {
        Account invalidAccount = new SavingsAccount("Invalid User", -500, 0);
        String[] errors = AnnotationValidator.validate(invalidAccount);

        assertTrue(errors.length > 0, "Should detect negative balance with @Positive annotation");
        assertTrue(errors[0].contains("must be > 0"), "Error message should match @Positive default");
    }

    // 9. Practical 13: Bounded generic average method
    @Test
    public void testBoundedGenericAverage() {
        Integer[] ints = {10, 20, 30, 40};
        assertEquals(25.0, Repository.average(ints), 0.001);

        Double[] doubles = {1.5, 2.5, 5.0};
        assertEquals(3.0, Repository.average(doubles), 0.001);
    }
}
