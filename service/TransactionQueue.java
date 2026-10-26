package service;

import java.util.LinkedList;
import java.util.Queue;

// Practical 10: Producer-Consumer buffer using wait() and notify()
public class TransactionQueue {

    private final Queue<String> buffer = new LinkedList<>();
    private final int capacity;

    public TransactionQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void produce(String transaction) throws InterruptedException {
        while (buffer.size() == capacity) {
            wait();
        }
        buffer.add(transaction);
        System.out.println("[Producer] Added to queue: " + transaction);
        notify(); // Wake up waiting consumer
    }

    public synchronized String consume() throws InterruptedException {
        while (buffer.isEmpty()) {
            wait();
        }
        String tx = buffer.poll();
        System.out.println("[Consumer] Processed from queue: " + tx);
        notify(); // Wake up waiting producer
        return tx;
    }
}
