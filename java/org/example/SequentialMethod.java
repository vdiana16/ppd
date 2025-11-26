// vdiana16/ppd/ppd-94ca92ca3564f0fe1dec91c44dee2bdaf0f8d658/java/org/example/SequentialMethod.java - VERSIUNE ACTUALIZATĂ
package org.example;

import java.io.*;
import java.util.List;

public class SequentialMethod {
    private SortedLinkedListSeqImpl accumulationList; // Lista ne-sortată pentru acumulare (Faza 1)
    private SortedLinkedListSeqImpl sortedList;       // Lista sortată pentru rezultatul final (Faza 2)
    private List<String> fileNames;
    private String outputFileName;

    public SequentialMethod(List<String> fileNames, String outputFileName) {
        this.accumulationList = new SortedLinkedListSeqImpl(); // Fără sincronizare
        this.sortedList = new SortedLinkedListSeqImpl();       // Fără sincronizare
        this.fileNames = fileNames;
        this.outputFileName = outputFileName;
    }

    // Metoda adaugă sau actualizează nota (logica acumulatorului)
    private void addGrade(int id, double grade) {
        // Folosim metoda nou implementată în LinkedListImpl
        accumulationList.addOrUpdateByID(id, grade);
    }

    private void processFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Extragerea ID și notă (similar cu ReaderThread)
                String[] parts = line.replace("(", "").replace(")", "").split(", ");
                int id = Integer.parseInt(parts[0]);
                double grade = Double.parseDouble(parts[1]);

                addGrade(id, grade);
            }
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Error processing file " + fileName + ": " + e.getMessage(), e);
        }
    }

    // Faza de sortare (Faza 2)
    private void sortResults() {
        Node current = accumulationList.get();
        while (current != null) {
            Node next = current.getNext(); // Salvează nodul următor

            // Detachează nodul curent de lista de acumulare
            // În lista simplă, e suficient să ne bazăm pe variabila 'current'
            // O versiune mai sigură ar fi să extragem nodul.
            // Vom folosi o variantă simplă de detașare locală, presupunând că
            // lista de acumulare nu mai e folosită (sau am folosi extractFirstNode)

            // Reutilizăm nodul 'current' și îl adăugăm în lista sortată
            current.setNext(null); // Deconectează nodul de restul listei vechi
            sortedList.addSortedByGradeDescending(current);

            current = next; // Treci la următorul nod salvat
        }
    }


    private void writeToFile() {
        // Scrie rezultatele din lista sortată în fișierul de ieșire.
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName))) {
            Node current = sortedList.get();
            while (current != null) {
                bw.write(current.toString());
                bw.newLine();
                current = current.getNext();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sequentialProcessing() {
        // Faza 1: Citirea și Acumularea (Nota totală pentru fiecare student)
        for (String fileName : fileNames) {
            processFile(fileName);
        }

        // Faza 2: Sortarea (extrage nodurile din lista de acumulare și le inserează sortate)
        sortResults();

        // Faza 3: Scrierea rezultatelor sortate
        writeToFile();
    }
}