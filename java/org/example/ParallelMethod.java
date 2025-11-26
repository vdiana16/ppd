package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelMethod {

    private final int numberOfReaders;
    private final int numberOfWorkers;
    private final int numberOfThreads;
    private final List<String> fileNames;
    private final String outputFileName;

    private LinkedList resultList;
    private UnboundedQueue sharedQueue;

    public ParallelMethod(int numberOfThreads, int numberOfReaders, List<String> fileNames, String outputFileName) {
        this.numberOfThreads = numberOfThreads;
        this.numberOfReaders = numberOfReaders;
        this.numberOfWorkers = this.numberOfThreads - this.numberOfReaders;
        this.fileNames = fileNames;
        this.outputFileName = outputFileName;

        this.resultList = new ThreadSafeLinkedListImpl();
        this.sharedQueue = new UnboundedQueueImpl();
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
            Node current = resultList.get();
            while (current != null) {
                bw.write(current.toString());
                bw.newLine();
                current = current.getNext();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Implements parallel processing logic (Method B1).
     */
    public void processingParallel() {
        //System.out.println("=== Starting Parallel Processing (Readers: " + numberOfReaders + ", Workers: " + numberOfWorkers + ") ===");

        AtomicInteger activeReaderCount = new AtomicInteger(numberOfReaders);

        List<List<String>> fileAssignments = splitFiles(fileNames, numberOfReaders);

        Thread[] readerThreads = new Thread[numberOfReaders];
        Thread[] workerThreads = new Thread[numberOfWorkers];

        for (int i = 0; i < numberOfReaders; i++) {
            ReaderThread reader = new ReaderThread(fileAssignments.get(i), sharedQueue, activeReaderCount, numberOfWorkers);
            reader.setName("Reader-" + (i + 1));
            readerThreads[i] = reader;
            reader.start();
        }

        for (int i = 0; i < numberOfWorkers; i++) {
            WorkerThread worker = new WorkerThread(sharedQueue, resultList);
            worker.setName("Worker-" + (i + 1));
            workerThreads[i] = worker;
            worker.start();
        }

        // Asteapta ca toti Cititorii sa termine (si sa trimita Poison Pill)
        for (Thread t : readerThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Asteapta ca toti Lucratorii sa termine (dupa ce au primit Poison Pill)
        for (Thread t : workerThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        //System.out.println("=== All threads have completed. Writing results to file: " + outputFileName + " ===");
        writeToFile();
    }
}