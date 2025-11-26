package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ReaderThread extends Thread {
    private final List<String> fileNames;
    private final Queue sharedQueue; // Changed to Queue
    private final AtomicInteger activeReaderCount;
    private final int workerCount;

    public ReaderThread(List<String> fileNames, Queue sharedQueue, AtomicInteger activeReaderCount, int workerCount) {
        this.fileNames = fileNames;
        this.sharedQueue = sharedQueue;
        this.activeReaderCount = activeReaderCount;
        this.workerCount = workerCount;
    }

    @Override
    public void run() {
        for (String fileName : fileNames) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String trimmedLine = line.trim();
                    if(trimmedLine.startsWith("(")) {
                        String[] parts = trimmedLine.replace("(", "").replace(")", "").split(", ");
                        if (parts.length == 2) {
                            int id = Integer.parseInt(parts[0].trim());
                            double grade = Double.parseDouble(parts[1].trim());
                            Node node = new Node(id, grade);
                            sharedQueue.enqueue(node); // Enqueue operation handles internal synchronization
                        }
                    }
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println(Thread.currentThread().getName() + " Error in file " + fileName + ": " + e.getMessage());
            }
        }

        // Signal end of Phase 1 (Reading)
        if (activeReaderCount.decrementAndGet() == 0) {
            // Last reader sends PHASE1_PILLs for each worker
            for (int i = 0; i < workerCount; i++) {
                sharedQueue.enqueue(QueueImpl.PHASE1_PILL);
            }
        }
    }
}