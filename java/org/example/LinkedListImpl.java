package org.example;


// Requirement 2: Fine-Grain Locking (Hand-over-Hand)
public class LinkedListImpl implements LinkedList {
    private final Node head;
    private final Node tail;

    // Used for Phase 1 output (aggregation)
    public LinkedListImpl() {
        this.head = new Node(); // Dummy head sentinel (ID: -1, Grade: -1.0)
        this.tail = new Node(); // Dummy tail sentinel (ID: -1, Grade: -1.0)
        head.setNext(this.tail);
    }

    // --- Auxiliary Methods for ParallelMethod (Phase 2 Producer) ---
    public Node getHeadSentinel() { return head; }
    public Node getTailSentinel() { return tail; }
    // ------------------------------------------

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
    public boolean isEmpty() {
        head.lock();
        try {
            return head.getNext() == tail;
        } finally {
            head.unlock();
        }
    }

    @Override
    public void addFirst(int id, double grade) {
        Node newNode = new Node(id, grade);
        head.lock();
        try {
            newNode.setNext(head.getNext());
            head.setNext(newNode);
        } finally {
            head.unlock();
        }
    }

    // Hand-over-hand locking for findByID
    @Override
    public Node findByID(int id) {
        Node pred = this.head;
        pred.lock();
        Node result = null;
        try {
            Node curr = pred.getNext();
            curr.lock();
            try {
                while (curr != this.tail) {
                    if (curr.getId() == id) {
                        result = curr;
                        return result;
                    }
                    pred.unlock();
                    pred = curr;
                    curr = curr.getNext();
                    curr.lock();
                }
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
        return null;
    }

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
