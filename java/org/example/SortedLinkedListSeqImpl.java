// vdiana16/ppd/ppd-94ca92ca3564f0fe1dec91c44dee2bdaf0f8d658/java/org/example/LinkedListImpl.java - VERSIUNE ACTUALIZATĂ
package org.example;

public class SortedLinkedListSeqImpl implements LinkedList {
    private Node head;

    public SortedLinkedListSeqImpl() {
        // În versiunea secvențială, ne putem lipsi de nodurile sentinelă
        // sau le putem folosi, dar pentru simplitate și pe baza codului original,
        // îl menținem ca o listă simplă.
        this.head = null;
    }

    @Override
    public Node get() {
        return head;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    // Metodă ajutătoare re-utilizată (se menține)
    public void addFirst(int id, double grade) {
        Node newNode = new Node(id, grade);
        newNode.setNext(head);
        head = newNode;
    }

    // Metodă nouă pentru acumulare (înlocuiește vechiul addGrade din SequentialMethod)
    public void addOrUpdateByID(int id, double grade) {
        Node node = findByID(id);
        if (node == null) {
            // Dacă nu există, adaugă la început (simplu)
            addFirst(id, grade);
        } else {
            // Dacă există, actualizează nota
            double currentGrade = node.getGrade();
            node.setGrade(currentGrade + grade);
        }
    }

    public void addSortedByGradeDescending(Node newNode) {
        if (head == null || newNode.getGrade() > head.getGrade()) {
            // Inserează la început dacă lista e goală sau nota este mai mare decât a primului nod
            newNode.setNext(head);
            head = newNode;
            return;
        }

        Node current = head;
        // Găsește poziția: se oprește înainte de nodul cu nota mai mică sau egală
        // newNode.getGrade() > current.getNext().getGrade()
        while (current.getNext() != null && newNode.getGrade() <= current.getNext().getGrade()) {
            current = current.getNext();
        }

        newNode.setNext(current.getNext());
        current.setNext(newNode);
    }


    public Node extractFirstNode() {
        if (head == null) return null;
        Node extracted = head;
        head = head.getNext();
        extracted.setNext(null);
        return extracted;
    }

    // findByID rămâne neschimbat
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


    // printLinkedList rămâne neschimbat
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
        System.out.println();    }
}