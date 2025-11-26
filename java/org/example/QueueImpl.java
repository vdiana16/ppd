package org.example;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class QueueImpl implements Queue {
    private static final int MAX_CAPACITY = 100; // Requirement 3: Max capacity

    private Node head;
    private Node tail;
    private int size;
    // Define POISON_PILLs for two-phase termination
    public static final Node PHASE1_PILL = new Node(-1, -1.0);
    public static final Node PHASE2_PILL = new Node(-2, -2.0); // New sentinel for Phase 2 termination

    private final Lock lock;
    private final Condition notEmpty; // Requirement 4: Condition Variables
    private final Condition notFull;  // Requirement 4: Condition Variables

    public QueueImpl() {
        this.head = null;
        this.tail = null;
        this.size = 0;

        this.lock = new ReentrantLock();
        this.notEmpty = this.lock.newCondition();
        this.notFull = this.lock.newCondition();
    }

    @Override
    public void enqueue(Node newNode) {
        lock.lock();
        try {
            while (size == MAX_CAPACITY) {
                try {
                    notFull.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            if (isEmpty()) {
                head = newNode;
                tail = newNode;
            } else {
                tail.setNext(newNode);
                tail = newNode;
            }
            size++;

            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Node dequeue() {
        lock.lock();
        try {
            while (isEmpty()) {
                try {
                    notEmpty.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }

            Node dequeuedNode  = head;
            head = head.getNext();
            size--;
            dequeuedNode.setNext(null); // Detach node from queue

            if (isEmpty()) {
                tail = null;
            }

            notFull.signalAll();
            return dequeuedNode;
        }  finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }
}