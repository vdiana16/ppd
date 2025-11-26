package org.example;

// Requirement 1 & 2: Sorted list (Descending Grade) with Fine-Grain Locking (Hand-over-Hand)
public class SortedLinkedListImpl implements LinkedList {
    private final Node head;
    private final Node tail;

    public SortedLinkedListImpl() {
        this.head = new Node();
        this.tail = new Node();
        head.setNext(this.tail);
    }

    // Core method for Requirement 1: Insert node in descending order of grade.
    public void insertSortedDesc(Node newNode) {
        Node pred = this.head;
        pred.lock();
        Node current = pred.getNext();
        current.lock();

        try {
            // Traverse until finding the position where newNode's grade is GREATER OR EQUAL
            // to the current node's grade (for DESCENDING order).
            while (current != this.tail && newNode.getGrade() < current.getGrade()) {
                pred.unlock();
                pred = current;
                current = current.getNext();
                current.lock();
            }

            // Insert newNode between pred and current
            newNode.setNext(current);
            pred.setNext(newNode);

        } finally {
            current.unlock();
            pred.unlock();
        }
    }

    // --- Other methods are boilerplate or unused for Phase 2 ---
    @Override
    public Node get() {
        Node current = null;
        head.lock();
        try {
            current = head.getNext();
            if (current == tail) return null;
        } finally {
            head.unlock();
        }
        return current;
    }

    @Override
    public boolean isEmpty() { return false; }

    @Override
    public void addFirst(int id, double grade) { }

    @Override
    public Node findByID(int id) { return null; }

    @Override
    public void printLinkedList() {
        if (isEmpty()) {
            System.out.println("Linked list is empty.");
            return;
        }

        System.out.print("Linked list (Head -> ... -> Final): ");
        Node current = head;
        while (current != null) {
            System.out.print(current + (current.getNext() != null ? " -> " : ""));
            current = current.getNext();
        }
        System.out.println();
    }
}