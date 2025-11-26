package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileComparator {

    // Regex to extract ID and Score from the format: ID: 101, Final Score: 95.00
    private static final Pattern RESULT_PATTERN = Pattern.compile("\\s*\\((\\d+),\\s*([0-9.]+)\\)\\s*");

    /**
     * Reads a result file and extracts (ID, Score) pairs into a Map.
     *
     * @param fileName The name of the file to read.
     * @return Map<Integer, Double> where the key is the Student ID, and the value is the Final Score.
     * @throws IOException If there is an error reading the file.
     */
    private Map<Integer, Double> readResultsToMap(String fileName) throws IOException {
        Map<Integer, Double> results = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matcher = RESULT_PATTERN.matcher(line.trim());

                if (matcher.find()) {
                    try {
                        int id = Integer.parseInt(matcher.group(1));
                        double score = Double.parseDouble(matcher.group(2));
                        results.put(id, score);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed line in " + fileName + ": " + line);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Compares the results from two files, ignoring the order.
     * This method is used to verify the integrity of parallel processing against sequential results.
     *
     * @param sequentialFile The path to the file with sequential results.
     * @param parallelFile The path to the file with parallel results.
     * @return true if both sets of results (ID-Score pairs) are identical, false otherwise.
     */
    public boolean compareFiles(String sequentialFile, String parallelFile) {
        Map<Integer, Double> sequentialResults;
        Map<Integer, Double> parallelResults;

        try {
            sequentialResults = readResultsToMap(sequentialFile);
            parallelResults = readResultsToMap(parallelFile);
        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
            return false;
        }

        //System.out.println("--- Comparison Report ---");
        //System.out.println("Sequential records found: " + sequentialResults.size());
        //System.out.println("Parallel records found: " + parallelResults.size());

        // 1. Compare total record count
        if (sequentialResults.size() != parallelResults.size()) {
            System.err.println("FAILURE: Mismatch in total number of records.");
            return false;
        }

        // 2. Compare each ID-Score pair
        int errors = 0;
        final double EPSILON = 0.0001; // Tolerance for floating-point comparison

        for (Map.Entry<Integer, Double> seqEntry : sequentialResults.entrySet()) {
            int id = seqEntry.getKey();
            double seqScore = seqEntry.getValue();

            if (!parallelResults.containsKey(id)) {
                //System.err.println("FAILURE: Student ID " + id + " found in sequential but missing in parallel.");
                errors++;
                continue;
            }

            double parScore = parallelResults.get(id);

            // Compare scores with tolerance
            if (Math.abs(seqScore - parScore) > EPSILON) {
                //System.err.printf("FAILURE: Score mismatch for ID %d. Sequential: %.2f, Parallel: %.2f\n", id, seqScore, parScore);
                errors++;
            }
        }

        // 3. Final Result
        if (errors == 0) {
            System.out.println("SUCCESS: All " + sequentialResults.size() + " records match perfectly (order ignored).");
            return true;
        } else {
            System.err.println("TOTAL FAILURES: " + errors + " mismatches found.");
            return false;
        }
    }
}