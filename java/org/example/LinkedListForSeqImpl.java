package org.example;

public class LinkedListForSeqImpl implements LinkedList {
    private Node head;

    public LinkedListForSeqImpl() {
        this.head = null;
    }

    public Node get() {
        return head;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    public void addFirst(int id, double grade) {
        Node newNode = new Node(id, grade);
        newNode.setNext(head);
        head = newNode;
    }

    @Override
    public void addOrUpdateByID(int id, double grade) {
        Node node = findByID(id);
        if (node == null) {
            addFirst(id, grade);
        } else {
            double currentGrade = node.getGrade();
            node.setGrade(currentGrade + grade);
        }
    }

    @Override
    public void addSortedByGradeDescending(Node newNode) {
        if (head == null || newNode.getGrade() > head.getGrade()) {
            newNode.setNext(head);
            head = newNode;
            return;
        }

        Node current = head;
        while (current.getNext() != null && newNode.getGrade() <= current.getNext().getGrade()) {
            current = current.getNext();
        }

        newNode.setNext(current.getNext());
        current.setNext(newNode);
    }

    @Override
    public Node extractFirstNode() {
        if (head == null) return null;
        Node extracted = head;
        head = head.getNext();
        extracted.setNext(null);
        return extracted;
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