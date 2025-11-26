package org.example;

import java.util.concurrent.locks.ReentrantLock;

public class WorkerThread extends  Thread {
    private final Queue sharedQueue;
    private final LinkedListImpl resultUnsorted; // Phase 1 target
    private final SortedLinkedListImpl resultSorted; // Phase 2 target
    private volatile boolean keepRunning = true;

    public WorkerThread(Queue sharedQueue, LinkedListImpl resultUnsorted, SortedLinkedListImpl resultSorted) {
        this.sharedQueue = sharedQueue;
        this.resultUnsorted = resultUnsorted;
        this.resultSorted = resultSorted;
    }

    @Override
    public void run() {
        Node currentNode = null;

        // --- Phase 1: Aggregation ---
        while (keepRunning) {
            try {
                currentNode = sharedQueue.dequeue();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                return;
            }

            if (currentNode == QueueImpl.PHASE1_PILL) {
                sharedQueue.enqueue(currentNode); // Propagate the signal
                break; // Exit Phase 1 loop
            }

            // Logic for Phase 1: Add/Update node in the Unsorted List
            Node existingNode = resultUnsorted.findByID(currentNode.getId());

            if (existingNode == null) {
                resultUnsorted.addFirst(currentNode.getId(), currentNode.getGrade());
            } else {
                // Requirement 2: Fine-Grain Synchronization on update
                existingNode.lock();
                try {
                    double newGrade = existingNode.getGrade() + currentNode.getGrade();
                    existingNode.setGrade(newGrade);
                } finally {
                    existingNode.unlock();
                }
            }
        }

        // --- Phase 2: Sorting ---
        while (keepRunning) {
            try {
                currentNode = sharedQueue.dequeue();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                return;
            }

            if (currentNode == QueueImpl.PHASE2_PILL) {
                sharedQueue.enqueue(currentNode); // Propagate the final signal
                keepRunning = false;
                return;
            }

            // Logic for Phase 2: Insert node into the Sorted List
            // Requirement 6 is implemented via SortedLinkedListImpl.insertSortedDesc
            resultSorted.insertSortedDesc(currentNode);
        }
    }
}