package org.example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node {
    private int id;
    private double grade;
    private Node next;
    private final Lock lock = new ReentrantLock();

    public Node(int id, double nota) {
        this.id = id;
        this.grade = nota;
        this.next = null;
    }

    public Node() {
        this.id = -1;
        this.grade = -1.0;
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

    public void lock() { lock.lock(); }

    public void unlock() { lock.unlock(); }

    @Override
    public String toString() {
        if (id == -1) return "SENTINEL";
        return "(" + id + ", " + String.format("%.2f", grade) + ")";
    }
}