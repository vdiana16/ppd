package org.example;

import java.util.concurrent.locks.Lock;

public class WorkerThread extends  Thread {
    private final Queue sharedQueue;
    private final LinkedList acumulatedResult;
    private final LinkedList sortedResult;
    private volatile boolean keepRunning = true;

    public WorkerThread(Queue sharedQueue, LinkedList acumulatedResult) {
        this.sharedQueue = sharedQueue;
        this.acumulatedResult = acumulatedResult;
        this.sortedResult = null;
    }

    public WorkerThread(LinkedList acumulatedResult, LinkedList sortedResult) {
        this.sharedQueue = null;
        this.acumulatedResult = acumulatedResult;
        this.sortedResult = sortedResult;
    }

    @Override
    public void run() {
        if (sharedQueue != null) {
            runAcumulationPhase();
        } else {
            runSortingPhase();
        }
    }

    private void runAcumulationPhase() {
        while (keepRunning) {
            Node currentNode = sharedQueue.dequeue();

            if (currentNode == QueueImpl.POISON_PILL) {
                sharedQueue.enqueue(currentNode);
                keepRunning = false;
                continue;
            }

            Node existingNode = acumulatedResult.findByID(currentNode.getId());
            if (existingNode == null) {
                acumulatedResult.addOrUpdateByID(currentNode.getId(), currentNode.getGrade());
            }
            else {
                Lock nodeLock = existingNode.getLock();
                nodeLock.lock();
                try {
                    double newGrade = existingNode.getGrade() + currentNode.getGrade();
                    existingNode.setGrade(newGrade);
                } finally {
                    nodeLock.unlock();
                }
            }
        }
    }

    private void runSortingPhase() {
        while (true) {
            Node nodeToMove = acumulatedResult.extractFirstNode();

            if (nodeToMove == null) {
                break;
            }

            sortedResult.addSortedByGradeDescending(nodeToMove);
        }
    }
}
