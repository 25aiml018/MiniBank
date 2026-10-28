# MiniBank

**MiniBank** is an enterprise-grade, modular Java console banking application developed incrementally across all 14 Object-Oriented Programming (OOP) practical laboratories. It demonstrates core software engineering principles including encapsulation, inheritance, polymorphism, interface-driven design, reflection, custom annotations, structured exception handling, multithreading & concurrency, object serialization, NIO.2 file handling, the Java Collections Framework, generic data repositories, and JUnit 5 unit testing.

---

## Architecture & Layered Design

```
MiniBank/
├── MiniBank.java                    # Application entry point, interactive menu & lab runner
├── MiniBank.jar                     # Runnable JAR build artifact
├── lib/
│   └── junit-platform-console-standalone-1.10.2.jar # JUnit 5 test engine & launcher
├── model/
│   ├── BankInfo.java                # Record: Bank name and branch header
│   ├── MenuOption.java              # Enum: Banking menu actions
│   ├── TransactionType.java         # Enum: DEPOSIT, WITHDRAW, TRANSFER
│   ├── Command.java                 # Record: Parsed command tokens
│   ├── Customer.java                # Model: Encapsulation, nested Address, Cloneable, Serializable, @Id
│   ├── Account.java                 # Abstract Model: Base account, Comparable, Serializable, @Id, @Positive
│   ├── SavingsAccount.java          # Model: Min balance constraint & 4.0% interest
│   ├── CurrentAccount.java          # Model: Overdraft facility & 0.0% interest
│   ├── FixedDepositAccount.java     # Model: Locked withdrawal & 7.0% interest
│   ├── Transactable.java            # Interface: deposit and withdraw contract
│   ├── InterestBearing.java         # Interface: default yearlyInterest() calculation
│   ├── Premium.java                 # Marker Interface: Premium classification
│   └── annotation/
│       ├── Id.java                  # Custom Annotation: Entity identifier for Repository reflection
│       ├── Positive.java            # Custom Annotation: Number validation rule
│       ├── MaxLength.java           # Custom Annotation: String length constraint
│       └── Audit.java               # Custom Annotation: Service method auditing
├── util/
│   ├── Validator.java               # Compiled Regex Validators: Mobile, Email, PAN, IFSC
│   ├── CommandParser.java           # Tokenizer: Parses CLI strings into Command records
│   ├── StatementFormatter.java      # Builder: Multi-line formatted statements via StringBuilder
│   ├── WithdrawRule.java            # Functional Interface: Lambda & anonymous rule validation
│   └── AnnotationValidator.java     # Reflection: Inspects @Positive & @MaxLength at runtime
├── exception/
│   ├── BankException.java           # Checked base exception for all banking operations
│   ├── InsufficientFundsException.java # Failure with exact shortfall computation
│   ├── AccountNotFoundException.java   # Missing account lookups
│   └── InvalidAmountException.java     # Non-positive or invalid amounts
├── service/
│   ├── BankService.java             # Facade & Service Layer: Wires repository, validation, exceptions & persistence
│   ├── Repository.java              # Generic Repository<T> with reflection for @Id & bounded average
│   ├── AccountStore.java            # Collections Storage: HashMap, ArrayList history, Comparator & TreeMap
│   ├── StatePersister.java          # Persistence: ObjectOutputStream/ObjectInputStream serialization
│   ├── TransactionLog.java          # Audit: Append-only transaction logging via NIO Files.write
│   ├── ReportGenerator.java         # Reporting: DirectoryStream, BufferedReader & BasicFileAttributes
│   ├── TransactionProcessor.java    # Concurrency: Thread pool (ExecutorService) & ConcurrentHashMap
│   ├── AccountWorker.java           # Concurrency: Runnable task for multithreaded transactions
│   ├── TransactionQueue.java        # Concurrency: Producer-Consumer coordination with wait()/notify()
│   └── BankSession.java             # Resource Management: AutoCloseable for try-with-resources
└── test/
    └── MiniBankTest.java            # Automated JUnit 5 test suite validating all core requirements
```

---

## Practicals Covered (1 through 14)

### Practical 1: Console Shell, Records & Enums
- `BankInfo`: Java record encapsulating bank metadata (`name`, `branch`), displayed as an application header.
- `MenuOption`: Java enum representing banking actions (`OPEN_ACCOUNT`, `DEPOSIT`, `WITHDRAW`, `TRANSFER`, `STATEMENT`, `LIST_ACCOUNTS`, `BATCH_TRANSACTIONS`, `GENERATE_REPORT`, `SEARCH_ACCOUNT`, `WORKING_HOURS`, `EXIT`).
- Switch expressions (`switch (option) { ... }`) mapping choices cleanly.
- Supplementary: Bank working hours schedule and invalid choice re-prompting.

### Practical 2: Encapsulation & Identity Generation
- `Customer`: Private fields (`name`, `email`, `mobile`), auto-generated customer ID (`CUST101`) via synchronized static counter.
- `Account`: Private fields (`accountNumber`, `ownerName`, `balance`, `active`), auto-generated account numbers (`AC0001`).
- Constructor chaining (`this(ownerName, 0)`).
- Controlled state mutation exclusively through `deposit()` and `withdraw()`, preventing direct access.

### Practical 3: Object Methods, Nested Classes & Clone
- Overridden `toString()`, `equals()`, and `hashCode()` based on unique `accountNumber`.
- Static nested class `Customer.Address` (`line`, `city`, `pincode`).
- Deep cloning via `implements Cloneable` and `clone()`.
- Type checking with `instanceof`.

### Practical 4: Regular Expressions, Command Parsing & StringBuilder
- `Validator`: Compiled regular expressions (`Pattern`) for:
  - Mobile numbers: `^[6-9]\d{9}$`
  - Email addresses: `^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\.[A-Za-z]{2,})$`
  - PAN cards: `^[A-Z]{5}[0-9]{4}[A-Z]$`
  - IFSC codes: `^[A-Z]{4}0[A-Z0-9]{6}$`
- `CommandParser`: Splits lines like `DEPOSIT AC0001 500` into `Command` records.
- `StatementFormatter`: Assembles multi-line account statements efficiently using `StringBuilder`.

### Practical 5: Inheritance, Abstract Classes & Polymorphism
- Abstract base class `Account` with abstract methods: `double interestRate()` and `boolean canWithdraw(long amount)`.
- Polymorphic subtypes:
  - `SavingsAccount`: 4.0% interest rate, enforces minimum balance (`minBalance`).
  - `CurrentAccount`: 0.0% interest rate, allows overdraft facility (`overdraftLimit`).
  - `FixedDepositAccount`: 7.0% interest rate, locks funds (`canWithdraw` returns `false`).
- Runtime polymorphism and Java pattern matching with `instanceof`.

### Practical 6: Interfaces, Lambdas & Modular Packaging
- `Transactable`: Defines `deposit(long)` and `withdraw(long)`.
- `InterestBearing`: Interface with default method `yearlyInterest()` computing compound interest.
- `WithdrawRule`: `@FunctionalInterface` implemented both via anonymous inner class and lambda expression `(account, amount) -> ...`.
- `Premium`: Marker interface (empty interface pattern).
- Modular packages (`model`, `util`, `service`, `exception`, `test`) and static imports.

### Practical 7: Custom Annotations & Runtime Reflection
- Custom annotations with meta-annotations (`@Target`, `@Retention(RUNTIME)`):
  - `@Id`: Marks identifier fields for reflection.
  - `@Positive`: Enforces positive numerical values with default error message `"must be > 0"`.
  - `@MaxLength`: Enforces maximum string length constraints.
- `AnnotationValidator`: Traverses class hierarchies reflectively using `getDeclaredFields()`, `isAnnotationPresent()`, `setAccessible(true)`, and `get(obj)` to detect rule violations at runtime.

### Practical 8: Structured Exception Handling & Custom Hierarchy
- Checked exception hierarchy rooted at `BankException`:
  - `InsufficientFundsException`: Carries exact `shortfall` amount.
  - `AccountNotFoundException`: Thrown on invalid lookups.
  - `InvalidAmountException`: Rejects negative or zero transaction amounts.
- `transfer()` with `try-catch-finally` ensuring state integrity.
- `AutoCloseable` `BankSession` managing resources via `try-with-resources`.

### Practical 9: Multithreading, Thread Safety & Synchronization
- `AccountWorker`: Implements `Runnable` executing batch deposits.
- Thread lifecycle tracking (`NEW`, `RUNNABLE`, `TERMINATED`) and coordinating with `join()`.
- Race condition demonstration: Unsynchronized updates produce balance errors; synchronized methods guarantee 100% thread safety and accuracy.

### Practical 10: Thread Pools, Coordination & Deadlock Avoidance
- `TransactionProcessor`: Managed thread pool using `ExecutorService` (`Executors.newFixedThreadPool(4)`).
- `TransactionQueue`: Inter-thread producer-consumer communication using `wait()` and `notify()`.
- Deadlock prevention in `safeTransfer()`: Always locks the account with the smaller `accountNumber` lexicographically first, eliminating circular hold-and-wait conditions.

### Practical 11: Serialization, Transient Fields, Logs & NIO Reporting
- `Serializable` implementation across `Account`, `Customer`, `Address`, and account subclasses with explicit `serialVersionUID = 1L`.
- `StatePersister`: Saves and reloads account arrays using `ObjectOutputStream` and `ObjectInputStream` inside `try-with-resources`.
- Transient demonstration: Field `private transient String sessionToken` resets to `null` upon deserialization.
- `TransactionLog`: Append-only audit log using NIO `Files.write(...)` with `StandardOpenOption.CREATE` and `APPEND`.
- `ReportGenerator`: Walks `logs/` directory using `Files.newDirectoryStream`, skips empty or non-log files, reads records line-by-line via `BufferedReader`, computes deposits/withdrawals/net change, inspects `BasicFileAttributes` (size, modification time), and writes an end-of-day summary report.

### Practical 12: Collections Framework & Concurrent Collections
- `AccountStore`:
  - Primary store: `HashMap<String, Account>` keyed by account number.
  - Transaction history: `ArrayList<String>`.
  - Balance sorting: `sortedByBalance()` using `Comparator.comparingLong(Account::getBalance).reversed()`.
  - Account ordering: `Account` implements `Comparable<Account>` via `compareTo()`.
  - Key ordering: `TreeMap<String, Account>` sorting accounts by ID.
  - Filtering: `findAccountsWithBalanceAbove(long threshold)`.
- `TransactionProcessor`: Utilizes `ConcurrentHashMap<String, Account>` for safe multithreaded account registry access.

### Practical 13: Generic Repository with Reflection
- Generic class `Repository<T>` backing any model entity with CRUD operations:
  - `T save(T entity)`: Finds the `@Id` annotated field at runtime via reflection across class hierarchy and keys the internal `HashMap`.
  - `T findById(String id)`
  - `ArrayList<T> findAll()`
  - `boolean delete(String id)`
  - `int count()`
- `@SafeVarargs public final void saveAll(T... items)`.
- Bounded generic utility method: `public static <N extends Number> double average(N[] values)`.
- Reused without modification for both `Repository<Account>` and `Repository<Customer>`.

### Practical 14: Layered Integration, Interactive Banking Shell & JUnit 5 Testing
- `BankService`: Wires `Repository<Account>`, `Validator`, exception hierarchy, `TransactionProcessor`, `TransactionLog`, `StatePersister`, and `ReportGenerator`.
- Interactive Console Banking:
  - Restores saved state from `data/accounts.dat` on startup.
  - Allows account creation, deposits, withdrawals, transfers, statement view, sorting, batch processing, and reporting.
  - Automatically persists all state on exit.
- Reflective `@Audit` logging.
- Comprehensive automated JUnit 5 test suite with `@Test` assertions validating deposit increases, overdraft rejections, concurrent deposits, state persistence, reflection, and deadlock freedom.
- Packaged as a standalone runnable JAR (`MiniBank.jar`).

---

## How to Compile, Run & Test

### 1. Compile All Sources
```bash
javac -cp ".:lib/*" $(find . -name "*.java")
```

### 2. Run Comprehensive Demonstration (Practicals 1 to 14)
Executes all concepts sequentially with formatted console output:
```bash
java -cp ".:lib/*" MiniBank
```

### 3. Run Interactive Banking Shell
Launches the full interactive console application:
```bash
java -cp ".:lib/*" MiniBank menu
```

### 4. Run Automated JUnit 5 Tests
Runs the built-in test suite:
```bash
java -cp ".:lib/*" MiniBank test
```
*Alternatively, run directly via the JUnit Platform Console Launcher:*
```bash
java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path . --select-class test.MiniBankTest
```

### 5. Build and Run Standalone JAR
```bash
# Package into executable JAR
jar cfe MiniBank.jar MiniBank $(find . -name "*.class")

# Run JAR
java -jar MiniBank.jar
```

---

## Automated Test Verification

The test suite in [`test/MiniBankTest.java`](file:///Users/rudragharaliya/Desktop/MiniBank/test/MiniBankTest.java) validates:
1. `testDepositIncreasesBalance()`: Verifies positive balance increment.
2. `testWithdrawalOverLimitThrowsException()`: Confirms `InsufficientFundsException` carrying positive shortfall when min balance is violated.
3. `testConcurrentDepositsKeepBalanceCorrect()`: Verifies 10 concurrent threads depositing funds simultaneously maintain mathematical balance integrity without race conditions.
4. `testStatePersistenceSaveAndLoad()`: Confirms binary object serialization and restoration without data loss.
5. `testGenericRepositoryWithReflection()`: Asserts generic `Repository<T>` reflects on `@Id` for both `Account` and `Customer`.
6. `testDeadlockFreeTransfer()`: Verifies high-volume bidirectional transfers execute without thread deadlock.
7. `testRegexValidator()`: Tests regex matching for Indian mobile numbers, emails, PAN cards, and IFSC codes.
8. `testAnnotationValidatorNegativeBalance()`: Validates that reflection accurately catches violations of custom `@Positive`.
9. `testBoundedGenericAverage()`: Asserts bounded generic method `<N extends Number> double average(N[] values)`.
