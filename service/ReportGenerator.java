package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// Practical 11: NIO DirectoryStream, line-by-line BufferedReader, file attributes & end-of-day report
public class ReportGenerator {

    public static String generateReport(Path logsDir, Path reportFile) throws IOException {
        if (logsDir == null || reportFile == null) {
            throw new IllegalArgumentException("logsDir and reportFile cannot be null");
        }

        if (!Files.exists(logsDir)) {
            Files.createDirectories(logsDir);
        }
        if (reportFile.getParent() != null) {
            Files.createDirectories(reportFile.getParent());
        }

        long totalDeposits = 0;
        long totalWithdrawals = 0;
        int totalTransactions = 0;
        int processedFilesCount = 0;
        List<String> fileAuditDetails = new ArrayList<>();

        // Walk the logs directory using Files.newDirectoryStream
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsDir)) {
            for (Path entry : stream) {
                // Supplementary: skip files that are not regular files, empty, or do not end with .log
                if (!Files.isRegularFile(entry)) {
                    continue;
                }
                String filename = entry.getFileName().toString();
                if (!filename.endsWith(".log")) {
                    continue;
                }

                BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);
                if (attrs.size() == 0) {
                    continue; // Skip empty log files
                }

                processedFilesCount++;
                fileAuditDetails.add(String.format("File: %s | Size: %d bytes | Last Modified: %s",
                        filename, attrs.size(), attrs.lastModifiedTime()));

                // Read line-by-line with BufferedReader
                try (BufferedReader reader = Files.newBufferedReader(entry, StandardCharsets.UTF_8)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) continue;

                        totalTransactions++;
                        String[] tokens = line.split("\\s+");
                        if (tokens.length >= 3) {
                            String type = tokens[0].toUpperCase();
                            try {
                                long amount;
                                // Handle TRANSFER from -> to amount format
                                if (type.equals("TRANSFER") && tokens.length >= 5) {
                                    amount = Long.parseLong(tokens[3]);
                                    // Transfer can be treated as internal movement or separate
                                } else {
                                    amount = Long.parseLong(tokens[2]);
                                }

                                if (type.startsWith("DEPOSIT")) {
                                    totalDeposits += amount;
                                } else if (type.startsWith("WITHDRAW")) {
                                    totalWithdrawals += amount;
                                }
                            } catch (NumberFormatException ignored) {
                                // Ignore non-numeric amount tokens
                            }
                        }
                    }
                }
            }
        }

        long netChange = totalDeposits - totalWithdrawals;

        StringBuilder sb = new StringBuilder();
        sb.append("=====================================================\n");
        sb.append("           MINIBANK END-OF-DAY SUMMARY REPORT        \n");
        sb.append("=====================================================\n");
        sb.append("Generated At: ").append(Instant.now()).append("\n");
        sb.append("Log Files Processed: ").append(processedFilesCount).append("\n");
        sb.append("Total Transaction Records: ").append(totalTransactions).append("\n");
        sb.append("-----------------------------------------------------\n");
        sb.append("Total Deposits:     Rs. ").append(totalDeposits).append("\n");
        sb.append("Total Withdrawals:  Rs. ").append(totalWithdrawals).append("\n");
        sb.append("Net Balance Change: Rs. ").append(netChange).append("\n");
        sb.append("-----------------------------------------------------\n");
        sb.append("Audit Trail (File Attributes):\n");
        if (fileAuditDetails.isEmpty()) {
            sb.append("  (No non-empty .log files found)\n");
        } else {
            for (String detail : fileAuditDetails) {
                sb.append("  * ").append(detail).append("\n");
            }
        }
        sb.append("=====================================================\n");

        String reportContent = sb.toString();

        // Write summary report to file using NIO Files.writeString
        Files.writeString(
                reportFile,
                reportContent,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        return reportContent;
    }
}
