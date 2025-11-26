package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

public class DataGenerator {

    private static final int TOTAL_STUDENTS = 200;
    private static final int TOTAL_PROJECTS = 10;
    private static final int MAX_PROJECT_SCORE = 10;

    /**
     * Generates 10 text files (project1.txt to project10.txt)
     * with random scores for students.
     */
    public static void generateFiles() {
        Random random = new Random();
        String resourcePath = "src/main/resources/";

        System.out.println("Starting file generation...");

        for (int i = 1; i <= TOTAL_PROJECTS; i++) {
            String fileName = resourcePath + "project" + i + ".txt";

            ArrayList<Integer> submittedIds = new ArrayList<>();

            int currentNumberOfScores = 80 + random.nextInt(TOTAL_STUDENTS - 80 + 1);

            for (int j = 1; j <= TOTAL_STUDENTS; j++) {
                submittedIds.add(j);
            }
            Collections.shuffle(submittedIds);

            try (FileWriter writer = new FileWriter(fileName)) {

                for (int k = 0; k < currentNumberOfScores; k++) {
                    int studentId = submittedIds.get(k);
                    int score = random.nextInt(MAX_PROJECT_SCORE + 1);

                    // Data format: (ID, ProjectScore)
                    writer.write("(" + studentId + ", " + score + ")\n");
                }

                System.out.println("-> File generated: " + fileName + " (" + currentNumberOfScores + " records)");

            } catch (IOException e) {
                System.err.println("Error writing file " + fileName + ": " + e.getMessage());
            }
        }
        System.out.println("File generation finished.");
    }
}