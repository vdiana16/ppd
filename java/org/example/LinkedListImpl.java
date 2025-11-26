package org.example;

public class LinkedListImpl implements LinkedList {
    private Node head;

    public LinkedListImpl() {
        this.head = null;
    }

    public Node get() {
        return head;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public void addFirst(int id, double grade) {
        Node newNode = new Node(id, grade);
        newNode.setNext(head);
        head = newNode;
    }

    @Override
    public boolean deleteByID(int id) {
        if (isEmpty()) {
            System.out.println("The linked list is empty. Cannot delete ID: " + id);
            return false;
        }

        if (head.getId() == id) {
            head = head.getNext();
            return true;
        }

        Node current = head;
        Node previous = null;

        while (current != null && current.getId() != id) {
            previous = current;
            current = current.getNext();
        }

        if (current != null) {
            previous.setNext(current.getNext());
            return true;
        }

        return false;
    }

    @Override
    public Node findByID(int id) {
        Node current = head;
        while (current != null) {
            if (current.getId() == id) {
                return current;
            }
            current = current.getNext();
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
