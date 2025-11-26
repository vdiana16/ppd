package org.example;

public interface LinkedList {
    /**
     * Get the head node of the list.
     * @return head node.
     */
    Node get();

    /**
     * Add a new node at the beginning of the list.
     * @param id node's ID.
     * @param grade node's grade.
     */
    void addFirst(int id, double grade);

    /**
     * Delete a node by its ID.
     * @param id node's ID to be deleted.
     * @return true if the node was found and deleted, false otherwise.
     */
    boolean deleteByID(int id);

    /**
     * Check if the list is empty.
     * @return true if the list is empty, false otherwise.
     */
    boolean isEmpty();

    /**
     * Search for a node by its ID.
     * @param id node's ID to search for.
     * @return the node if found, null otherwise.
     */
    Node findByID(int id);

    /**
     * Print the linked list.
     */
    void printLinkedList();
}