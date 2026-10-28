package service;

import model.Account;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

// Practical 11: Object serialization & deserialization using ObjectOutputStream and ObjectInputStream
public class StatePersister {

    // Saves an array of Account objects to a file using ObjectOutputStream inside try-with-resources
    public static void save(Account[] accounts, Path file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            oos.writeObject(accounts);
        }
    }

    // Loads an array of Account objects from a file using ObjectInputStream inside try-with-resources
    public static Account[] load(Path file) throws IOException, ClassNotFoundException {
        if (file == null || !Files.exists(file)) {
            return new Account[0];
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof Account[] accounts) {
                return accounts;
            } else if (obj instanceof List<?> list) {
                return list.toArray(new Account[0]);
            } else {
                throw new IOException("Unexpected data format in persisted file: " + obj.getClass());
            }
        }
    }

    // Overloaded helper for collections
    public static void save(Collection<Account> accounts, Path file) throws IOException {
        if (accounts == null) {
            save(new Account[0], file);
        } else {
            save(accounts.toArray(new Account[0]), file);
        }
    }

    // Overloaded helper returning List
    public static List<Account> loadList(Path file) throws IOException, ClassNotFoundException {
        return Arrays.asList(load(file));
    }
}
