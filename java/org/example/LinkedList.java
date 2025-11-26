package org.example;

public interface LinkedList {
    /**
     * Obține nodul de început (următorul după santinela de head) al listei.
     * @return nodul de început.
     */
    Node get();

    /**
     * Adaugă un nod nou la începutul listei.
     * Această metodă este folosită în special de ThreadSafeLinkedListImpl pentru
     * a adăuga rapid noduri la început, fără a menține ordinea.
     * @param id ID-ul nodului.
     * @param grade Nota nodului.
     */
    void addFirst(int id, double grade);

    /**
     * Verifică dacă lista este goală (conține doar santinele).
     * @return true dacă lista este goală, false altfel.
     */
    boolean isEmpty();

    /**
     * Caută un nod după ID-ul său.
     * Această metodă este crucială pentru faza de agregare (adunarea notelor).
     * @param id ID-ul nodului de căutat.
     * @return nodul găsit, sau null dacă nu a fost găsit.
     */
    Node findByID(int id);

    /**
     * Afișează lista.
     */
    void printLinkedList();
}