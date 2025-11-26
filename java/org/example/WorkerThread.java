package org.example;

public class WorkerThread extends  Thread {
    private final UnboundedQueue sharedQueue;
    private final LinkedList result;
    private volatile boolean keepRunning = true;

    public WorkerThread(UnboundedQueue sharedQueue, LinkedList result) {
        this.sharedQueue = sharedQueue;
        this.result = result;
    }

    @Override
    public void run() {
        while (keepRunning) {
            Node currentNode = null;

            try {
                currentNode = sharedQueue.dequeue();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                return;
            }

            if (currentNode == UnboundedQueueImpl.POISON_PILL) {
                sharedQueue.enqueue(currentNode);
                keepRunning = false;
            }

            Node existingNode = result.findByID(currentNode.getId());
            if (existingNode == null) {
                result.addFirst(currentNode.getId(), currentNode.getGrade());
            }
            else {
                synchronized (existingNode) {
                    double newGrade = existingNode.getGrade() + currentNode.getGrade();
                    existingNode.setGrade(newGrade);
                }
            }
        }
    }
}
