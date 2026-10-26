package model;

// Practical 6: Interface with default yearlyInterest method
public interface InterestBearing {
    default double yearlyInterest() {
        return (interestRate() * getBalance()) / 100.0;
    }

    double interestRate();
    long getBalance();
}