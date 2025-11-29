package org.example;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileComparator {
    private static final Pattern RESULT_PATTERN = Pattern.compile("\\s*\\((\\d+),\\s*([0-9.]+)\\)\\s*");
    private static final double EPSILON = 0.0001;

    private static class Pair {
        int id;
        double score;

        Pair(int id, double score) {
            this.id = id;
            this.score = score;
        }

        // Pentru comparare: ID și scor identic (cu toleranță)
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair)) return false;
            Pair other = (Pair) o;
            return id == other.id && Math.abs(score - other.score) < EPSILON;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, (int) Math.round(score * 10000));
        }
    }

    private List<Pair> readResultsToList(String fileName) throws IOException {
        List<Pair> results = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matcher = RESULT_PATTERN.matcher(line.trim());
                if (matcher.find()) {
                    try {
                        int id = Integer.parseInt(matcher.group(1));
                        double score = Double.parseDouble(matcher.group(2));
                        results.add(new Pair(id, score));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed line in " + fileName + ": " + line);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Compară două fișiere: ordinea contează EXCEPTÂND blocurile de note egale
     */
    public boolean compareFiles(String sequentialFile, String parallelFile) {
        List<Pair> seqList, parList;
        try {
            seqList = readResultsToList(sequentialFile);
            parList = readResultsToList(parallelFile);
        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
            return false;
        }
        if (seqList.size() != parList.size()) {
            System.err.println("FAILURE: Different number of records");
            return false;
        }

        int n = seqList.size();
        int i = 0;
        int errors = 0;

        while (i < n) {
            double currentScore = seqList.get(i).score;
            // Găsește lungimea blocului cu același scor în seqList
            int j = i + 1;
            while (j < n && Math.abs(seqList.get(j).score - currentScore) < EPSILON)
                j++;

            // Găsește lungimea blocului cu același scor în parList (presupunem secvențele ar trebui să se potrivească, altfel e eroare)
            int k = i + 1;
            double parScore = parList.get(i).score;
            if (Math.abs(parScore - currentScore) > EPSILON) {
                System.err.printf("Blocat scor diferit pe poziția %d: %.2f vs %.2f\n", i+1, currentScore, parScore);
                errors++;
                break;
            }
            while (k < n && Math.abs(parList.get(k).score - parScore) < EPSILON)
                k++;

            // Dacă numărul de participanți cu același scor nu este egal, eroare
            if ((j - i) != (k - i)) {
                System.err.printf("Blocuri cu scor %.2f de dimensiuni diferite: %d vs %d\n", currentScore, (j-i), (k-i));
                errors++;
            } else {
                // Comparăm ca set/multime conținutul acestui bloc, nu ca secvență
                Set<Pair> set1 = new HashSet<>(seqList.subList(i, j));
                Set<Pair> set2 = new HashSet<>(parList.subList(i, k));
                if (!set1.equals(set2)) {
                    System.err.printf("Bloc cu scor %.2f diferit - seturi de participanți nu coincid!\n", currentScore);
                    errors++;
                }
            }
            // Avansăm la următorul bloc
            i = j;
        }

        if (errors == 0) {
            System.out.println("SUCCESS: All records match, order is correct (except among equal scores).");
            return true;
        } else {
            System.err.println("TOTAL FAILURES: " + errors);
            return false;
        }
    }
}