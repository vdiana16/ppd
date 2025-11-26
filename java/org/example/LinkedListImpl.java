package org.example;

public class LinkedListImpl implements LinkedList {
    private final Node head;
    private final Node tail;

    public LinkedListImpl() {
        this.head = null;
        this.tail = null;
        head.setNext(tail);
    }

    public Node get() {
        return head.getNext() != tail ? head.getNext() : null;
    }

    @Override
    public boolean isEmpty() {
        return head.getNext() == tail;
    }

    @Override
    public void addOrUpdateByID(int id, double grade) {
        Node predecessor = head;
        predecessor.getLock().lock();
        try {
            Node current = predecessor.getNext();
            current.getLock().lock();
            try {
                while (current != tail) {
                    if (current.getId() == id) {
                        double oldGrade = current.getGrade();
                        current.setGrade(oldGrade + grade);
                        return;
                    }
                    predecessor.getLock().unlock();
                    predecessor = current;
                    current = current.getNext();
                    current.getLock().lock();
                }

                Node newNode = new Node(id, grade);
                newNode.setNext(head.getNext());
                head.setNext(newNode);
            } finally {
                if (current != null) {
                    current.getLock().unlock();
                }
            }
        } finally {
            if (predecessor != null) {
                predecessor.getLock().unlock();
            }
        }
    }

    @Override
    public void addSortedByGradeDescending(Node newNode) {
        Node predecessor = head;
        predecessor.getLock().lock();
        try {
            Node current = predecessor.getNext();
            current.getLock().lock();
            try {
                while (current != tail && current.getGrade() >= newNode.getGrade()) {
                    predecessor.getLock().unlock();
                    predecessor = current;
                    current = current.getNext();
                    current.getLock().lock();
                }

                newNode.setNext(current);
                predecessor.setNext(newNode);
            } finally {
                if (current != null) {
                    current.getLock().unlock();
                }
            }
        } finally {
            if (predecessor != null) {
                predecessor.getLock().unlock();
            }
        }
    }

    @Override
    public Node extractFirstNode() {
        head.getLock().lock();
        try {
            Node firstNode = head.getNext();
            if (firstNode == tail) {
                return null;
            }

            firstNode.getLock().lock();
            try {
                head.setNext(firstNode.getNext());
                firstNode.setNext(null);
                return firstNode;
            } finally {
                firstNode.getLock().unlock();
            }
        } finally {
            head.getLock().unlock();
        }
    }

    @Override
    public Node findByID(int id) {
        Node predecessor = head;
        predecessor.getLock().lock();
        try {
            Node current = predecessor.getNext();
            current.getLock().lock();
            try {
                while (current != tail) {
                    if (current.getId() == id) {
                        current.getLock().unlock();
                        return current;
                    }
                    predecessor.getLock().unlock();
                    predecessor = current;
                    current = current.getNext();
                    current.getLock().lock();
                }
                return null;
            } finally {
                if (current != null) {
                    current.getLock().unlock();
                }
            }
        } finally {
            if (predecessor != null) {
                predecessor.getLock().unlock();
            }
        }
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
