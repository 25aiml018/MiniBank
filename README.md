# MiniBank

MiniBank is a modular Java console banking application built incrementally through Object-Oriented Programming (OOP) practical labs.

## Project Structure

```
MiniBank/
├── MiniBank.java                # Main application class & lab demonstration runner
├── MiniBank.jar                 # Runnable JAR package
├── model/
│   ├── BankInfo.java            # Record containing bank name and branch
│   ├── MenuOption.java          # Enum representing core banking actions
│   ├── TransactionType.java     # Enum for transaction types (DEPOSIT, WITHDRAW, TRANSFER)
│   ├── Command.java             # Record for parsed banking commands
│   ├── Customer.java            # Customer model with nested Address & Cloneable
│   ├── Account.java             # Abstract Account base class (encapsulation & synchronization)
│   ├── SavingsAccount.java      # Savings account with min balance & 4% interest
│   ├── CurrentAccount.java      # Current account with overdraft limit & 0% interest
│   ├── FixedDepositAccount.java # Fixed deposit account with locked funds & 7% interest
│   ├── Transactable.java        # Interface for transaction operations
│   ├── InterestBearing.java     # Interface with default yearly interest method
│   ├── Premium.java             # Marker interface
│   └── annotation/
│       ├── Id.java              # Marker annotation for account identifier
│       ├── Positive.java        # Custom validation annotation for positive values
│       └── MaxLength.java       # Custom validation annotation for string length
├── util/
│   ├── Validator.java           # Compiled regex validators (mobile, email, PAN, IFSC)
│   ├── CommandParser.java       # Parser splitting line input into Command records
│   ├── StatementFormatter.java  # Statement builder using StringBuilder
│   ├── WithdrawRule.java        # Functional interface for withdrawal rules
│   └── AnnotationValidator.java # Reflection-based validator for custom annotations
├── exception/
│   ├── BankException.java       # Checked base exception for all banking errors
│   ├── InsufficientFundsException.java # Exception with shortfall details
│   ├── AccountNotFoundException.java   # Exception for missing accounts
│   └── InvalidAmountException.java     # Exception for non-positive transaction amounts
└── service/
    ├── BankSession.java         # AutoCloseable session for try-with-resources
    ├── AccountWorker.java       # Runnable task for concurrent deposits
    ├── TransactionProcessor.java # ExecutorService thread pool processor
    ├── TransactionQueue.java    # Producer-Consumer queue with wait() and notify()
    └── BankService.java         # Deadlock-free transfer using ordered locking
```

## Practicals Covered

- **Practical 1:** Project setup, `MiniBank` main class, `BankInfo` record header, `MenuOption` enum, and console menu shell with `switch` expression.
- **Practical 2:** `Customer` (with auto-generated ID) and `Account` classes with encapsulation, constructors, `deposit()`, and `withdraw()`.
- **Practical 3:** `toString()`, `equals()` and `hashCode()` based on `accountNumber`, nested `Customer.Address` static class, `clone()` implementation, and `instanceof`.
- **Practical 4:** Regular expression validations (Mobile, Email, PAN, IFSC), `TransactionType` enum, `Command` record, `CommandParser`, and `StatementFormatter`.
- **Practical 5:** Abstract `Account` class, polymorphic subclasses (`SavingsAccount`, `CurrentAccount`, `FixedDepositAccount`), and pattern matching with `instanceof`.
- **Practical 6:** `Transactable` and `InterestBearing` interfaces, `@FunctionalInterface` `WithdrawRule` (anonymous class + lambda), `Premium` marker interface, packaging, and runnable JAR.
- **Practical 7:** Custom annotations (`@Id`, `@Positive`, `@MaxLength`), reflection in `AnnotationValidator`, and validation error handling.
- **Practical 8:** Custom checked exception hierarchy (`BankException`, `InsufficientFundsException`, `InvalidAmountException`), `transfer()` with try-catch-finally, and `AutoCloseable` `BankSession` with try-with-resources.
- **Practical 9:** Multithreading with `AccountWorker` (`Runnable`), observing thread lifecycle, and thread synchronization (`synchronized`).
- **Practical 10:** Transaction engine with `TransactionProcessor` (`ExecutorService`), producer-consumer coordination with `wait()`/`notify()`, and deadlock prevention using ordered locking.

## How to Compile & Run

### Compile All Files
```bash
javac $(find . -name "*.java")
```

### Run Demonstration (Practicals 1 to 10)
```bash
java MiniBank
```

### Run Interactive Menu Shell
```bash
java MiniBank menu
```

### Run via Runnable JAR
```bash
java -jar MiniBank.jar
```
