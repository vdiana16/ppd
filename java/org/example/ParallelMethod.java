package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelMethod {

    private final int numberOfReaders;
    private final int numberOfWorkers;
    private final List<String> fileNames;
    private final String outputFileName;

    // Phase 1 output (Fine-Grain Synchronization Implemented)
    private final LinkedListImpl resultUnsorted;
    // Phase 2 output (Fine-Grain Synchronization Implemented)
    private final SortedLinkedListImpl resultSorted;
    // Bounded Queue with Condition Variables (Requirement 3 & 4)
    private final Queue sharedQueue;

    public ParallelMethod(int numberOfThreads, int numberOfReaders, List<String> fileNames, String outputFileName) {
        this.numberOfReaders = numberOfReaders;
        // Ensure at least 1 worker for processing
        this.numberOfWorkers = Math.max(1, numberOfThreads - numberOfReaders);
        this.fileNames = fileNames;
        this.outputFileName = outputFileName;

        this.resultUnsorted = new LinkedListImpl();
        this.resultSorted = new SortedLinkedListImpl(); // New list for sorting phase
        this.sharedQueue = new QueueImpl(); // Requirement 3 & 4
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
        // Requirement 1 & 6: Write the SORTED results
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName))) {
            Node current = resultSorted.get();
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
     * Implements parallel processing logic (Method B1) in two phases.
     */
    public void processingParallel() {
        System.out.println("=== Starting Parallel Processing (Readers: " + numberOfReaders + ", Workers: " + numberOfWorkers + ") ===");

        // ---------------------------------------------------------------------
        // --- Phase 1: Reading and Aggregation (Workers build resultUnsorted) ---
        // ---------------------------------------------------------------------
        System.out.println("--- Phase 1: Reading and Aggregation ---");

        AtomicInteger activeReaderCount = new AtomicInteger(numberOfReaders);
        List<List<String>> fileAssignments = splitFiles(fileNames, numberOfReaders);

        // Requirement 5: Use Executor for Reader threads
        ExecutorService readerExecutor = Executors.newFixedThreadPool(numberOfReaders);

        Thread[] workerThreads = new Thread[numberOfWorkers];

        // Start Worker threads (Consumers for Phase 1)
        for (int i = 0; i < numberOfWorkers; i++) {
            WorkerThread worker = new WorkerThread(sharedQueue, resultUnsorted, resultSorted);
            worker.setName("Worker-" + (i + 1));
            workerThreads[i] = worker;
            worker.start();
        }

        // Start Reader tasks (Producers for Phase 1)
        for (int i = 0; i < numberOfReaders; i++) {
            ReaderThread reader = new ReaderThread(fileAssignments.get(i), sharedQueue, activeReaderCount, numberOfWorkers);
            reader.setName("Reader-" + (i + 1));
            readerExecutor.submit(reader);
        }

        // Wait for all Reader threads to complete (they send PHASE1_PILL)
        readerExecutor.shutdown();
        try {
            readerExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        // ------------------------------------------------------------------
        // --- Phase 2: Main Thread as Producer, Workers as Consumers (Sorting) ---
        // ------------------------------------------------------------------
        System.out.println("--- Phase 2: Sorting the results ---");

        // 1. Main thread iterates over the Unsorted List (Producer for Phase 2)
        Node pred = resultUnsorted.getHeadSentinel();
        pred.lock();
        try {
            Node curr = pred.getNext();
            curr.lock();
            try {
                // Traverse and enqueue nodes for sorting
                while (curr != resultUnsorted.getTailSentinel()) {
                    // Create a deep copy of the node for safety (decoupling Phase 1 node from Phase 2 processing)
                    Node nodeForSorting = new Node(curr.getId(), curr.getGrade());

                    sharedQueue.enqueue(nodeForSorting);

                    // Hand-over-hand traversal
                    pred.unlock();
                    pred = curr;
                    curr = curr.getNext();
                    curr.lock();
                }

                // Note: Deletion from resultUnsorted is no longer strictly necessary if we only want sorting,
                // but for a true move/reuse scenario, deletion would happen here. Keeping deletion out for safety/simplicity
                // and focusing on correct sorting/output.

            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }

        // 2. Main thread sends PHASE2_PILLs to signal end of sorting tasks
        for (int i = 0; i < numberOfWorkers; i++) {
            sharedQueue.enqueue(QueueImpl.PHASE2_PILL);
        }

        // 3. Wait for all Worker threads to finish (they consume PHASE2_PILL)
        for (Thread t : workerThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        System.out.println("=== All threads have completed. Writing SORTED results to file: " + outputFileName + " ===");
        writeToFile();
    }
}