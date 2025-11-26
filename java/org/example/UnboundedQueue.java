package org.example;

public interface UnboundedQueue {
    /**
     * Enqueue a new node with the given id and grade.
     * @param node the node to be added to the queue.
     */
    void enqueue(Node node);

    /**
     * Dequeue a node from the front of the queue.
     * @return the dequeued node.
     */
    Node dequeue();

    /**
     * Check if the queue is empty.
     * @return true if the queue is empty, false otherwise.
     */
    boolean isEmpty();
}
