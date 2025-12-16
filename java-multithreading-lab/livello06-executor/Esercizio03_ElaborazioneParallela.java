/**
 * LIVELLO 6 - Esercizio 3: Elaborazione Parallela di Dati
 *
 * Obiettivo: Applicare l'Executor Framework a un caso reale
 *            di elaborazione parallela di grandi quantità di dati.
 *
 * Scenario: Simuliamo l'elaborazione di un batch di ordini e-commerce.
 *           Ogni ordine richiede:
 *           - Validazione
 *           - Calcolo totale
 *           - Verifica disponibilità magazzino
 */
import java.util.concurrent.*;
import java.util.*;

public class Esercizio03_ElaborazioneParallela {

    // Rappresenta un ordine
    static class Ordine {
        private int id;
        private String cliente;
        private List<String> prodotti;
        private double totale;

        public Ordine(int id, String cliente, List<String> prodotti) {
            this.id = id;
            this.cliente = cliente;
            this.prodotti = prodotti;
            this.totale = 0;
        }

        public int getId() { return id; }
        public String getCliente() { return cliente; }
        public List<String> getProdotti() { return prodotti; }
        public double getTotale() { return totale; }
        public void setTotale(double totale) { this.totale = totale; }
    }

    // Risultato dell'elaborazione
    static class RisultatoElaborazione {
        private int ordineId;
        private boolean successo;
        private String messaggio;
        private double totale;
        private long tempoMs;

        public RisultatoElaborazione(int ordineId, boolean successo,
                                      String messaggio, double totale, long tempoMs) {
            this.ordineId = ordineId;
            this.successo = successo;
            this.messaggio = messaggio;
            this.totale = totale;
            this.tempoMs = tempoMs;
        }

        @Override
        public String toString() {
            return String.format("Ordine #%d: %s - %s (%.2f€) [%dms]",
                    ordineId, successo ? "OK" : "ERRORE", messaggio, totale, tempoMs);
        }
    }

    // Task che elabora un singolo ordine
    static class ElaboraOrdine implements Callable<RisultatoElaborazione> {
        private Ordine ordine;

        public ElaboraOrdine(Ordine ordine) {
            this.ordine = ordine;
        }

        @Override
        public RisultatoElaborazione call() throws Exception {
            long inizio = System.currentTimeMillis();
            String thread = Thread.currentThread().getName();

            System.out.printf("[%s] Elaboro ordine #%d per %s...%n",
                    thread, ordine.getId(), ordine.getCliente());

            try {
                // Step 1: Validazione (50-150ms)
                Thread.sleep((long) (Math.random() * 100 + 50));

                // Simula errore di validazione per ordini senza prodotti
                if (ordine.getProdotti().isEmpty()) {
                    throw new Exception("Ordine vuoto!");
                }

                // Step 2: Calcolo totale (30-100ms)
                Thread.sleep((long) (Math.random() * 70 + 30));
                double totale = ordine.getProdotti().size() * (Math.random() * 50 + 10);
                ordine.setTotale(totale);

                // Step 3: Verifica magazzino (100-300ms)
                Thread.sleep((long) (Math.random() * 200 + 100));

                // Simula prodotto esaurito (10% di probabilità)
                if (Math.random() < 0.10) {
                    throw new Exception("Prodotto esaurito");
                }

                long tempo = System.currentTimeMillis() - inizio;
                return new RisultatoElaborazione(ordine.getId(), true,
                        "Elaborato con successo", totale, tempo);

            } catch (Exception e) {
                long tempo = System.currentTimeMillis() - inizio;
                return new RisultatoElaborazione(ordine.getId(), false,
                        e.getMessage(), 0, tempo);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Esercizio 3: Elaborazione Parallela Ordini ===\n");

        // Genera ordini di test
        List<Ordine> ordini = generaOrdini(20);

        System.out.println("Ordini da elaborare: " + ordini.size() + "\n");

        // Determina il numero ottimale di thread
        int numCores = Runtime.getRuntime().availableProcessors();
        int numThread = numCores * 2; // I/O bound, quindi più thread dei core

        System.out.println("Core disponibili: " + numCores);
        System.out.println("Thread nel pool: " + numThread + "\n");

        // TODO 1: Crea l'ExecutorService
        ExecutorService executor = Executors.newFixedThreadPool(numThread);

        // Lista per i task
        List<Callable<RisultatoElaborazione>> tasks = new ArrayList<>();

        // TODO 2: Crea un task per ogni ordine
        for (Ordine ordine : ordini) {
            tasks.add(new ElaboraOrdine(ordine));
        }

        long inizioTotale = System.currentTimeMillis();

        try {
            // TODO 3: Esegui tutti i task con invokeAll()
            List<Future<RisultatoElaborazione>> futures = executor.invokeAll(tasks);

            // TODO 4: Raccogli e analizza i risultati
            int successi = 0;
            int errori = 0;
            double totaleFatturato = 0;

            System.out.println("\n--- Risultati ---\n");

            for (Future<RisultatoElaborazione> future : futures) {
                RisultatoElaborazione risultato = future.get();
                System.out.println(risultato);

                if (risultato.successo) {
                    successi++;
                    totaleFatturato += risultato.totale;
                } else {
                    errori++;
                }
            }

            long tempoTotale = System.currentTimeMillis() - inizioTotale;

            // Riepilogo
            System.out.println("\n=== RIEPILOGO ===");
            System.out.println("Ordini elaborati: " + ordini.size());
            System.out.println("Successi: " + successi);
            System.out.println("Errori: " + errori);
            System.out.printf("Fatturato totale: %.2f€%n", totaleFatturato);
            System.out.println("Tempo totale: " + tempoTotale + " ms");

            // Calcola il tempo teorico sequenziale
            // (Ogni ordine impiega ~250-550ms, quindi per 20 ordini...)
            long tempoSeqStimato = ordini.size() * 400; // media 400ms
            System.out.println("\nTempo stimato sequenziale: ~" + tempoSeqStimato + " ms");
            System.out.printf("Speedup: ~%.2fx%n", (double) tempoSeqStimato / tempoTotale);

        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Errore durante l'elaborazione: " + e.getMessage());
        } finally {
            // TODO 5: Shutdown corretto
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }

        // ESERCIZI AGGIUNTIVI:

        // TODO 6: Modifica per usare CompletableFuture (Java 8+)
        // che offre una API più fluente per operazioni asincrone

        // TODO 7: Aggiungi un meccanismo di retry per gli ordini falliti
        // (riprova max 3 volte prima di dichiarare l'errore)

        // TODO 8: Implementa un callback per ogni ordine completato
        // (usa CompletableFuture.thenAccept o simili)
    }

    // Genera ordini di test
    private static List<Ordine> generaOrdini(int n) {
        List<Ordine> ordini = new ArrayList<>();
        String[] clienti = {"Mario", "Giulia", "Luca", "Anna", "Marco", "Sara"};
        String[] prodotti = {"Laptop", "Mouse", "Tastiera", "Monitor", "Cuffie", "Webcam"};

        for (int i = 1; i <= n; i++) {
            String cliente = clienti[(int) (Math.random() * clienti.length)];

            // Genera lista prodotti (1-5 prodotti per ordine)
            List<String> listaProdotti = new ArrayList<>();
            int numProdotti = (int) (Math.random() * 5) + 1;

            // Occasionalmente crea un ordine vuoto (per testare la gestione errori)
            if (Math.random() < 0.05) {
                numProdotti = 0;
            }

            for (int j = 0; j < numProdotti; j++) {
                listaProdotti.add(prodotti[(int) (Math.random() * prodotti.length)]);
            }

            ordini.add(new Ordine(i, cliente, listaProdotti));
        }

        return ordini;
    }
}

/*
 * PATTERN COMUNI CON EXECUTORSERVICE:
 *
 * 1. FIRE AND FORGET
 *    executor.execute(task);  // Non ci interessa il risultato
 *
 * 2. SUBMIT AND COLLECT
 *    List<Future<R>> futures = ...
 *    for (task : tasks) futures.add(executor.submit(task));
 *    for (future : futures) results.add(future.get());
 *
 * 3. INVOKE ALL (più semplice)
 *    List<Future<R>> futures = executor.invokeAll(tasks);
 *    // Blocca finché TUTTI completano
 *
 * 4. INVOKE ANY
 *    R result = executor.invokeAny(tasks);
 *    // Ritorna il risultato del PRIMO task che completa
 *
 * 5. COMPLETABLE FUTURE (moderno)
 *    CompletableFuture.supplyAsync(() -> compute(), executor)
 *        .thenApply(result -> transform(result))
 *        .thenAccept(final -> print(final));
 */
