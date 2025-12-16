/**
 * LIVELLO 6 - Esercizio 2: Future e Callable
 *
 * Obiettivo: Usare Callable per task che ritornano valori
 *            e Future per gestire i risultati asincroni.
 *
 * Scenario: Calcoliamo i numeri primi in intervalli diversi
 *           e raccogliamo i risultati.
 */
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class Esercizio02_FutureCallable {

    // Callable che conta i numeri primi in un intervallo
    static class ContaPrimi implements Callable<Integer> {
        private int inizio;
        private int fine;

        public ContaPrimi(int inizio, int fine) {
            this.inizio = inizio;
            this.fine = fine;
        }

        @Override
        public Integer call() throws Exception {
            String thread = Thread.currentThread().getName();
            System.out.printf("[%s] Cerco primi in [%d, %d]...%n", thread, inizio, fine);

            int count = 0;
            for (int n = inizio; n <= fine; n++) {
                if (isPrimo(n)) {
                    count++;
                }
            }

            System.out.printf("[%s] Trovati %d primi in [%d, %d]%n", thread, count, inizio, fine);
            return count;
        }

        private boolean isPrimo(int n) {
            if (n < 2) return false;
            if (n == 2) return true;
            if (n % 2 == 0) return false;

            for (int i = 3; i * i <= n; i += 2) {
                if (n % i == 0) return false;
            }
            return true;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Esercizio 2: Future e Callable ===\n");

        // Vogliamo contare i primi da 1 a 100.000
        // Dividiamo il lavoro in 4 parti
        final int MAX = 100_000;
        final int NUM_TASK = 4;
        final int INTERVALLO = MAX / NUM_TASK;

        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Lista per memorizzare i Future
        List<Future<Integer>> futures = new ArrayList<>();

        System.out.println("Divido il lavoro in " + NUM_TASK + " task...\n");

        long inizio = System.currentTimeMillis();

        // TODO 1: Crea e sottometti i task Callable
        for (int i = 0; i < NUM_TASK; i++) {
            int start = i * INTERVALLO + 1;
            int end = (i + 1) * INTERVALLO;

            ContaPrimi task = new ContaPrimi(start, end);

            // submit() ritorna un Future
            Future<Integer> future = executor.submit(task);
            futures.add(future);
        }

        // TODO 2: Raccogli i risultati dai Future
        int totale = 0;

        for (int i = 0; i < futures.size(); i++) {
            Future<Integer> future = futures.get(i);

            try {
                // get() blocca finché il risultato non è disponibile
                Integer risultato = future.get();
                totale += risultato;
                System.out.println("Risultato task " + (i + 1) + ": " + risultato);
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Errore nel task " + (i + 1) + ": " + e.getMessage());
            }
        }

        long fine = System.currentTimeMillis();

        // Shutdown
        executor.shutdown();

        System.out.println("\n--- Risultato ---");
        System.out.println("Numeri primi trovati tra 1 e " + MAX + ": " + totale);
        System.out.println("Tempo impiegato: " + (fine - inizio) + " ms");


        // ===============================================
        // PARTE 2: Gestione timeout e cancellazione
        // ===============================================

        System.out.println("\n=== Parte 2: Timeout e Cancellazione ===\n");

        ExecutorService executor2 = Executors.newSingleThreadExecutor();

        // Task molto lento
        Callable<String> taskLento = () -> {
            System.out.println("Task lento iniziato...");
            Thread.sleep(5000);  // 5 secondi!
            return "Finito!";
        };

        Future<String> futureLento = executor2.submit(taskLento);

        // TODO 3: Prova a ottenere il risultato con timeout di 2 secondi
        try {
            System.out.println("Aspetto il risultato (max 2 secondi)...");
            String risultato = futureLento.get(2, TimeUnit.SECONDS);
            System.out.println("Risultato: " + risultato);
        } catch (TimeoutException e) {
            System.out.println("TIMEOUT! Il task è troppo lento.");

            // TODO 4: Cancella il task
            boolean cancellato = futureLento.cancel(true);
            System.out.println("Task cancellato: " + cancellato);
            System.out.println("Task isCancelled: " + futureLento.isCancelled());
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Errore: " + e.getMessage());
        }

        executor2.shutdownNow();


        // TODO 5: Sperimenta con invokeAll() per sottomettere più task insieme
        // Decomment e completa:
        /*
        ExecutorService executor3 = Executors.newFixedThreadPool(2);
        List<Callable<Integer>> tasks = List.of(
            () -> { Thread.sleep(100); return 1; },
            () -> { Thread.sleep(200); return 2; },
            () -> { Thread.sleep(150); return 3; }
        );

        try {
            List<Future<Integer>> results = executor3.invokeAll(tasks);
            // invokeAll blocca finché TUTTI i task sono completati

            for (Future<Integer> f : results) {
                System.out.println("Risultato: " + f.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        executor3.shutdown();
        */
    }
}

/*
 * DIFFERENZE TRA submit() E execute():
 *
 * execute(Runnable):
 * - Accetta solo Runnable
 * - Non ritorna nulla
 * - Le eccezioni non catturate terminano il thread
 *
 * submit(Runnable/Callable):
 * - Accetta Runnable o Callable
 * - Ritorna un Future
 * - Le eccezioni sono incapsulate nel Future
 *
 * BEST PRACTICE:
 * Preferisci submit() perché:
 * 1. Puoi catturare eccezioni via Future.get()
 * 2. Puoi cancellare il task via Future.cancel()
 * 3. Puoi verificare lo stato via Future.isDone()
 */
