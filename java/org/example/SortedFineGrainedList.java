package org.example;

public class SortedFineGrainedList {
    private final Node head;
    private final Node tail;

    public SortedFineGrainedList() {
        head = new Node(); // sentinela
        tail = new Node();
        head.setNext(tail);
    }

    // Insert descrescător după grade (fine-grain locking)
    public void insertDescending(Node toInsert) {
        Node prev = head;
        prev.lock();
        try {
            Node curr = prev.getNext();
            curr.lock();
            try {
                while (curr != tail && curr.getGrade() > toInsert.getGrade()) {
                    prev.unlock();
                    prev = curr;
                    curr = curr.getNext();
                    curr.lock();
                }
                // se copiază datele într-un Node nou, pt. a nu reutiliza lock-ul vechi
                Node newNode = new Node(toInsert.getId(), toInsert.getGrade());
                newNode.setNext(curr);
                prev.setNext(newNode);
            } finally {
                curr.unlock();
            }
        } finally {
            prev.unlock();
        }
    }

    // Ca să scrii în fișier:
    public Node getFirst() {
        Node n = head.getNext();
        return n == tail ? null : n;
    }
}
