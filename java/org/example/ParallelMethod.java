package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelMethod {

    private final int numberOfReaders;
    private final int numberOfWorkers;
    private final int numberOfThreads;
    private final List<String> fileNames;
    private final String outputFileName;

    private final LinkedList accumulatedResult;
    private final LinkedList sortedResult;
    private final Queue sharedQueue;

    public ParallelMethod(int numberOfThreads, int numberOfReaders, List<String> fileNames, String outputFileName) {
        this.numberOfThreads = numberOfThreads;
        this.numberOfReaders = numberOfReaders;
        this.numberOfWorkers = this.numberOfThreads - this.numberOfReaders;
        this.fileNames = fileNames;
        this.outputFileName = outputFileName;

        this.accumulatedResult = new LinkedListImpl();
        this.sortedResult = new LinkedListImpl();
        this.sharedQueue = new QueueImpl();
    }

    /**
     * Splits a list of file names into N sublists (assignments)
     * distributing the remainder (rest) evenly among the first threads.
     */
    private List<List<String>> splitFiles(List<String> allFiles, int numSplits) {
        List<List<String>> assignments = new ArrayList<>(numSplits);

        if (allFiles.isEmpty() || numSplits <= 0) {
            for(int i = 0; i < numSplits; i++) {
                assignments.add(new ArrayList<>());
            }
            return assignments;
        }

        int totalFiles = allFiles.size();
        int filesPerSplit = totalFiles / numSplits;
        int rest = totalFiles % numSplits;

        int currentFileIndex = 0;

        for (int i = 0; i < numSplits; i++) {
            int splitSize = filesPerSplit + (i < rest ? 1 : 0);

            List<String> sublist;

            if (splitSize > 0) {
                int endIndex = Math.min(currentFileIndex + splitSize, totalFiles);
                sublist = new ArrayList<>(allFiles.subList(currentFileIndex, endIndex));
                currentFileIndex = endIndex;
            } else {
                sublist = new ArrayList<>();
            }

            assignments.add(sublist);
        }

        return assignments;
    }

    /**
     * Write the results to the output file.
     */
    private void writeToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName, true))) {
            Node current = sortedResult.get();
            while (current != null) {
                bw.write(current.toString());
                bw.newLine();
                current = current.getNext();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ReaderTask implements Runnable {
        private final String file;
        private final Queue queue;
        private final AtomicInteger remainingTasks;
        private final int workerCount;

        public ReaderTask(String file, Queue queue, AtomicInteger remainingTasks, int workerCount) {
            this.file = file;
            this.queue = queue;
            this.remainingTasks = remainingTasks;
            this.workerCount = workerCount;
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.replace("(", "").replace(")", "").split(", ");
                    int id = Integer.parseInt(parts[0]);
                    double grade = Double.parseDouble(parts[1]);
                    queue.enqueue(new Node(id, grade));
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error reading file " + file + ": " + e.getMessage());
            } finally {
                if (remainingTasks.decrementAndGet() == 0) {
                    for (int i = 0; i < workerCount; i++) {
                        queue.enqueue(QueueImpl.POISON_PILL);
                    }
                }
            }
        }
    }

    /**
     * Implements parallel processing logic (Method B1).
     */
    public void processingParallel() {
        // --- Faza 1: Citire (Producător - Thread Pool) și Acumulare (Worker) ---        AtomicInteger activeReaderCount = new AtomicInteger(numberOfReaders);
        ExecutorService readerExecutor = Executors.newFixedThreadPool(numberOfReaders);
        AtomicInteger remainingReaderTasks = new AtomicInteger(fileNames.size());

        // Trimiterea task-urilor de citire în pool
        for (String fileName : fileNames) {
            readerExecutor.submit(new ReaderTask(fileName, sharedQueue, remainingReaderTasks, numberOfWorkers));
        }

        // Thread-urile Worker pentru Faza 1 (Acumulare)
        Thread[] workerThreads = new Thread[numberOfWorkers];

        for (int i = 0; i < numberOfWorkers; i++) {
            WorkerThread worker = new WorkerThread(sharedQueue, accumulatedResult);
            worker.setName("Worker-Phase1-" + (i + 1));
            workerThreads[i] = worker;
            worker.start();
        }

        // Așteaptă terminarea tuturor cititorilor
        for (Thread t : workerThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // --- Faza 2: Sortare (Consumator al listei de acumulare) ---
        Thread[] sorterThreads = new Thread[numberOfWorkers];

        for (int i = 0; i < numberOfWorkers; i++) {
            WorkerThread sorter = new WorkerThread(accumulatedResult, sortedResult);
            sorter.setName("Worker-Phase2-" + (i + 1));
            sorterThreads[i] = sorter;
            sorter.start();
        }

        // Așteaptă terminarea Workerilor din Faza 2
        for (Thread t : sorterThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        writeToFile();
    }
}