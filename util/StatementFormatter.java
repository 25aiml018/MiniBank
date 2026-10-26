package util;

import model.Account;

// Practical 4: Builds multi-line account statement using StringBuilder
public class StatementFormatter {

    public static String buildStatement(Account account) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("           MINIBANK STATEMENT           \n");
        sb.append("========================================\n");
        sb.append("Account Number: ").append(account.getAccountNumber()).append("\n");
        sb.append("Account Holder: ").append(account.getOwnerName()).append("\n");
        sb.append("Current Balance: Rs. ").append(account.getBalance()).append("\n");
        sb.append("Interest Rate:  ").append(account.interestRate()).append("%\n");
        sb.append("Active Status:  ").append(account.isActive() ? "Active" : "Inactive").append("\n");
        sb.append("========================================");
        return sb.toString();
    }
}
