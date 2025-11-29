package org.example;

public class FineGrainedList {
    private final Node head; // sentinela head
    private final Node tail; // sentinela tail

    public FineGrainedList() {
        head = new Node(); // id = -1
        tail = new Node(); // id = -1, nu contează aici, e doar santinelă
        head.setNext(tail);
    }

    // Adaugă/actualizează cu fine-grain locking
    public void addOrUpdate(int id, double grade) {
        Node prev = head;
        prev.lock();
        try {
            Node curr = prev.getNext();
            curr.lock();
            try {
                while (curr != tail && curr.getId() < id) {
                    prev.unlock();
                    prev = curr;
                    curr = curr.getNext();
                    curr.lock();
                }
                if (curr != tail && curr.getId() == id) {
                    curr.setGrade(curr.getGrade() + grade); // update la nod existent
                } else {
                    Node newNode = new Node(id, grade);
                    newNode.setNext(curr);
                    prev.setNext(newNode);
                }
            } finally {
                curr.unlock();
            }
        } finally {
            prev.unlock();
        }
    }

    public Node getFirst() {
        Node n = head.getNext();
        return n == tail ? null : n;
    }

    public Node getHead() { return head; }
    public Node getTail() { return tail; }
}