package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static List<String> fileNames = List.of(
            "src/main/resources/project1.txt",
            "src/main/resources/project2.txt",
            "src/main/resources/project3.txt",
            "src/main/resources/project4.txt",
            "src/main/resources/project5.txt",
            "src/main/resources/project6.txt",
            "src/main/resources/project7.txt",
            "src/main/resources/project8.txt",
            "src/main/resources/project9.txt",
            "src/main/resources/project10.txt"
    );
    public static String outputFileNameSeq = "src/main/resources/sequential_results.txt";
    public static String outputFileNamePar = "src/main/resources/parallel_results.txt";


    public static void main(String[] args) {
        //DataGenerator.generateFiles();


        long start_time = System.nanoTime();

        //SequentialMethod sequentialMethod = new SequentialMethod(fileNames, outputFileNameSeq);
        //sequentialMethod.sequentialProcessing();

        ParallelMethod parallelMethod = new ParallelMethod(16, 2, fileNames, outputFileNamePar);
        parallelMethod.processingParallel();

        long end_time = System.nanoTime();

        FileComparator comparator = new FileComparator();
        //comparator.compareFiles(outputFileNameSeq, outputFileNamePar);

        System.out.println((double) (end_time - start_time) / 1E6); //ms

    }

}