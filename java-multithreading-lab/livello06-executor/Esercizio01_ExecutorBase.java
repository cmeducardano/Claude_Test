/**
 * LIVELLO 6 - Esercizio 1: Primo ExecutorService
 *
 * Obiettivo: Familiarizzare con ExecutorService e i pool di thread.
 *
 * Scenario: Simuliamo un server web che gestisce richieste HTTP.
 *           Invece di creare un thread per ogni richiesta, usiamo un pool.
 */
import java.util.concurrent.*;

public class Esercizio01_ExecutorBase {

    // Simula una richiesta HTTP
    static class RichiestaHTTP implements Runnable {
        private int id;
        private String url;

        public RichiestaHTTP(int id, String url) {
            this.id = id;
            this.url = url;
        }

        @Override
        public void run() {
            String thread = Thread.currentThread().getName();
            System.out.printf("[%s] Elaboro richiesta #%d: %s%n", thread, id, url);

            try {
                // Simula tempo di elaborazione (100-500ms)
                Thread.sleep((long) (Math.random() * 400 + 100));
            } catch (InterruptedException e) {
                System.out.printf("[%s] Richiesta #%d interrotta!%n", thread, id);
                return;
            }

            System.out.printf("[%s] Richiesta #%d completata.%n", thread, id);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Esercizio 1: ExecutorService Base ===\n");

        // TODO 1: Crea un ExecutorService con un pool di 4 thread
        // Usa: Executors.newFixedThreadPool(4);
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Array di URL da "processare"
        String[] urls = {
                "/home", "/products", "/cart", "/checkout",
                "/api/users", "/api/orders", "/login", "/logout",
                "/search", "/categories"
        };

        System.out.println("Sottometto " + urls.length + " richieste al pool di thread...\n");

        // TODO 2: Sottometti ogni richiesta all'executor
        for (int i = 0; i < urls.length; i++) {
            RichiestaHTTP richiesta = new RichiestaHTTP(i + 1, urls[i]);

            // Usa executor.submit(richiesta) o executor.execute(richiesta)
            executor.submit(richiesta);
        }

        // TODO 3: Chiudi l'executor correttamente
        // Non accettare più nuovi task
        executor.shutdown();

        // TODO 4: Aspetta che tutti i task finiscano (max 30 secondi)
        try {
            boolean completato = executor.awaitTermination(30, TimeUnit.SECONDS);

            if (completato) {
                System.out.println("\n--- Tutte le richieste completate! ---");
            } else {
                System.out.println("\n--- Timeout! Alcune richieste non completate. ---");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        // OSSERVAZIONI:
        // 1. Nota come i thread vengono riutilizzati (stesso nome per più richieste)
        // 2. Con 4 thread e 10 richieste, alcune richieste devono attendere


        // TODO 5: Prova a cambiare il numero di thread nel pool (2, 8, 10)
        // Come cambia il comportamento?


        // TODO 6: Cosa succede se provi a sottomettere una richiesta
        // DOPO aver chiamato shutdown()?
        // Prova: executor.submit(new RichiestaHTTP(99, "/test"));


        // TODO 7: Confronta il tempo totale con pool di dimensione diversa
        // Quale è il numero ottimale di thread per questo scenario?
    }
}

/*
 * VANTAGGI DI EXECUTORSERVICE:
 *
 * 1. RIUSO DEI THREAD
 *    - Invece di creare 10 thread, ne creiamo 4 che vengono riutilizzati
 *    - Meno overhead di creazione/distruzione
 *
 * 2. CONTROLLO DELLE RISORSE
 *    - Limitiamo il numero massimo di thread attivi
 *    - Preveniamo l'esaurimento delle risorse
 *
 * 3. GESTIONE CENTRALIZZATA
 *    - Un unico punto per shutdown e monitoraggio
 *    - Facile da configurare e modificare
 *
 * REGOLA PRATICA PER LA DIMENSIONE DEL POOL:
 *
 * - Task CPU-bound (calcoli): N thread = N core
 * - Task I/O-bound (rete, disco): N thread = N core * 2 (o più)
 *
 * Puoi ottenere il numero di core con:
 * int cores = Runtime.getRuntime().availableProcessors();
 */
