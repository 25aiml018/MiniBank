package service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

// Practical 11: Append-only transaction log using NIO Files.write with CREATE and APPEND
public class TransactionLog {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Practical 11 requirement: append(Path file, String line) using Files.write with CREATE and APPEND
    public static void append(Path file, String line) throws IOException {
        if (file == null || line == null) {
            throw new IllegalArgumentException("File and line cannot be null");
        }
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }

        Files.write(
                file,
                Collections.singletonList(line),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND
        );
    }

    // Convenience helper to log formatted transaction
    public static void log(Path file, String type, String accountNumber, long amount) throws IOException {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String entry = String.format("%s %s %d [%s]", type.toUpperCase(), accountNumber, amount, timestamp);
        append(file, entry);
    }

    public static void log(Path file, String type, String fromAccount, String toAccount, long amount) throws IOException {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String entry = String.format("%s %s -> %s %d [%s]", type.toUpperCase(), fromAccount, toAccount, amount, timestamp);
        append(file, entry);
    }
}
