package org.example;

public class Node {
    private int id;
    private double grade;
    private Node next;

    public Node(int id, double nota) {
        this.id = id;
        this.grade = nota;
        this.next = null;
    }

    public int getId() {
        return id;
    }

    public double getGrade() {
        return grade;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "(" + id + ", " + grade + ")";
    }
}