package org.example;

public class UnboundedQueueImpl implements UnboundedQueue {
    private Node head;
    private Node tail;
    private int size;
    public static final Node POISON_PILL = new Node(-1, -1.0);

    public UnboundedQueueImpl() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    @Override
    public synchronized void enqueue(Node newNode) {
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            tail = newNode;
        }
        size++;

        this.notifyAll();
    }

    @Override
    public synchronized Node dequeue() {
        while (isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        Node dequeuedNode = head;
        head = head.getNext();
        size--;
        if (isEmpty()) {
            tail = null;
        }
        return dequeuedNode;
    }

    @Override
    public synchronized boolean isEmpty() {
        return head == null;
    }
}