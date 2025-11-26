package org.example;

public interface LinkedList {
    /**
     * Get the head node of the list.
     * @return head node.
     */
    Node get();

    /**
     * Add a new node to the list, sorted by grade in descending order.
     * @param newNode the new node to be added.
     */
    void addSortedByGradeDescending(Node newNode);

    /**
     * Add a new node or update an existing node by its ID.
     * @param id node's ID.
     * @param grade new grade to be set if the node exists.
     */
    void addOrUpdateByID(int id, double grade);

    /**
     * Extract the first head node from the list.
     * @return the extracted head node.
     */
    Node extractFirstNode();

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