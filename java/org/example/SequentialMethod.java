package org.example;

import java.io.*;
import java.util.List;

public class SequentialMethod {
    private LinkedList accumulatedResult;
    private LinkedList sortedResult;
    private List<String> fileNames;
    private String outputFileName;

    public SequentialMethod(List<String> fileNames, String outputFileName) {
        this.accumulatedResult = new LinkedListForSeqImpl();
        this.sortedResult = new LinkedListForSeqImpl();
        this.fileNames = fileNames;
        this.outputFileName = outputFileName;
    }

    private void addGrade(int id, double grade) {
        accumulatedResult.addOrUpdateByID(id, grade);
    }

    private void processFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            line = br.readLine();
            while (line != null) {
                String[] parts = line.replace("(", "").replace(")", "").split(", ");
                int id = Integer.parseInt(parts[0]);
                double grade = Double.parseDouble(parts[1]);

                addGrade(id, grade);
                line = br.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sortResults () {
        Node current = accumulatedResult.get();
        while (current != null) {
            Node nextNode = current.getNext();
            current.setNext(null);
            sortedResult.addSortedByGradeDescending(current);
            current = nextNode;
        }
    }

    private void writeToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName))) {
            Node current = sortedResult.get();
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

        for (String fileName : fileNames) {
            processFile(fileName);
        }

        sortResults();

        writeToFile();
    }
}
