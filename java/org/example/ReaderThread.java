package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ReaderThread extends Thread {
    private final List<String> fileNames;
    private final UnboundedQueue sharedQueue;
    private final AtomicInteger activeReaderCount;
    private final int workerCount;

    public  ReaderThread(List<String> fileNames, UnboundedQueue sharedQueue, AtomicInteger activeReaderCount, int workerCount) {
        this.fileNames = fileNames;
        this.sharedQueue = (UnboundedQueueImpl) sharedQueue;
        this.activeReaderCount = activeReaderCount;
        this.workerCount = workerCount;
    }

    @Override
    public void run() {
        for (String fileName : fileNames) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.replace("(", "").replace(")", "").split(", ");
                    int id = Integer.parseInt(parts[0]);
                    double grade = Double.parseDouble(parts[1]);
                    Node node = new Node(id, grade);
                    sharedQueue.enqueue(node);
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println(Thread.currentThread().getName() + " Error in file " + fileName + ": " + e.getMessage());
            }
        }

        if (activeReaderCount.decrementAndGet() == 0) {
            synchronized (sharedQueue) {
                for (int i = 0; i < workerCount; i++) {
                    sharedQueue.enqueue(UnboundedQueueImpl.POISON_PILL);
                }
                sharedQueue.notifyAll();
            }
        }
    }


}
