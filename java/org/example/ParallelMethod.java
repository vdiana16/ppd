package org.example;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;

public class ParallelMethod {

    private final int numReaders;
    private final int numWorkers;
    private final List<String> fileNames;
    private final String outputFileName;

    // Structuri sincronizate
    private final QueueImpl queue = new QueueImpl();               // bounded queue, capacitate MAX
    private final FineGrainedList unsortedList = new FineGrainedList(); // lista note finale, cu fine-grain lock

    public static void readFileAndEnqueue(String filename, QueueImpl queue) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Extrage id și nota, presupunem format (ID, Nota)
                line = line.replace("(", "").replace(")", "");
                String[] parts = line.split(",");
                if (parts.length != 2) continue;

                int id = Integer.parseInt(parts[0].trim());
                double grade = Double.parseDouble(parts[1].trim());

                queue.enqueue(new Node(id, grade));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
        }
    }

    public ParallelMethod(int numThreads, int numReaders, List<String> fileNames, String outputFileName) {
        this.numReaders = numReaders;
        this.numWorkers = numThreads - numReaders;
        this.fileNames = fileNames;
        this.outputFileName = outputFileName;
    }

    public void processingParallel() {
        // ===== 1. Taskuri de citire cu Thread Pool (ExecutorService) =====
        ExecutorService readerPool = Executors.newFixedThreadPool(numReaders);

        // Folosim un CountDownLatch ca să știm când toți cititorii au terminat
        CountDownLatch readersDone = new CountDownLatch(fileNames.size());

        for (String file : fileNames) {
            readerPool.submit(() -> {
                readFileAndEnqueue(file, queue);
                readersDone.countDown();
            });
        }
        readerPool.shutdown();

        // ===== 2. Worker threads care preiau din coadă și inserează cu fine-grain sync =====
        Thread[] workers = new Thread[numWorkers];
        for (int i = 0; i < numWorkers; i++) {
            workers[i] = new WorkerThread(queue, unsortedList);
            workers[i].start();
        }

        // ===== 3. Când toate reader-ele au terminat - injectăm "poison pills" =====
        try {
            readersDone.await(); // Așteaptă toți cititorii
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // Injectează atâtea POISON_PILL câți workeri ai, ca ei să se oprească
        for (int i = 0; i < numWorkers; i++) {
            queue.enqueue(QueueImpl.PHASE1_PILL);
        }

        // ===== 4. Așteaptă terminarea workerilor =====
        for (Thread worker : workers) {
            try { worker.join(); } catch (InterruptedException e) {Thread.currentThread().interrupt();}
        }

        // ===== 5. Sortarea paralelă (fiecare thread copiază noduri) =====
        // O implementezi paralel sau secvențial (depinde cât de multă paralelizare vrei)
        SortedFineGrainedList sorted = new SortedFineGrainedList();
        Node curr = unsortedList.getFirst();
        while (curr != null) {
            sorted.insertDescending(curr);
            curr = curr.getNext();
        }

        // ===== 6. Scrierea rezultatului sortat în fișier =====
        writeToFile(sorted, outputFileName);
    }


    // --------- Helper pentru scrierea rezultatului in fisier ----------
    private void writeToFile(SortedFineGrainedList sortedList, String filename) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            Node current = sortedList.getFirst();
            while (current != null) {
                // nu scrie sentinele (id == -1 sau == -2)
                if (current.getId() > 0)
                    bw.write(current.toString() + "\n");
                current = current.getNext();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}