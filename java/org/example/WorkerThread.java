package org.example;

public class WorkerThread extends Thread {
    private final Queue queue;
    private final FineGrainedList list;
    private volatile boolean keepRunning = true;

    public WorkerThread(Queue queue, FineGrainedList list) {
        this.queue = queue;
        this.list = list;
    }

    @Override
    public void run() {
        while (keepRunning) {
            Node node = queue.dequeue();
            if (node == null) break;
            if (node == QueueImpl.PHASE1_PILL) { // Închide după ce toți reader-ii
                queue.enqueue(QueueImpl.PHASE1_PILL); // pentru ceilalți workeri
                keepRunning = false;
                break;
            }
            list.addOrUpdate(node.getId(), node.getGrade());
        }
    }
}