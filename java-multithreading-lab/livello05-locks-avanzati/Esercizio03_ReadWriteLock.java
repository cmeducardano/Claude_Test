/**
 * LIVELLO 5 - Esercizio 3: ReadWriteLock per Cache
 *
 * Obiettivo: Usare ReadWriteLock per ottimizzare l'accesso a una cache
 *            dove le letture sono molto più frequenti delle scritture.
 *
 * Scenario: Una cache di configurazione che viene letta spesso
 *           ma aggiornata raramente.
 */
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.HashMap;
import java.util.Map;

public class Esercizio03_ReadWriteLock {

    // Cache protetta con synchronized (baseline per confronto)
    static class CacheSynchronized {
        private Map<String, String> dati = new HashMap<>();

        public synchronized String get(String chiave) {
            simulaLatenza(10);  // Simula accesso lento
            return dati.get(chiave);
        }

        public synchronized void put(String chiave, String valore) {
            simulaLatenza(50);  // Scrittura più lenta
            dati.put(chiave, valore);
        }

        private void simulaLatenza(long ms) {
            try { Thread.sleep(ms); } catch (InterruptedException e) {}
        }
    }

    // Cache protetta con ReadWriteLock (ottimizzata)
    static class CacheReadWriteLock {
        private Map<String, String> dati = new HashMap<>();
        private ReadWriteLock rwLock = new ReentrantReadWriteLock();

        public String get(String chiave) {
            // TODO 1: Acquisisci il lock di LETTURA
            rwLock.readLock().lock();
            try {
                simulaLatenza(10);
                return dati.get(chiave);
            } finally {
                // TODO 2: Rilascia il lock di lettura
                rwLock.readLock().unlock();
            }
        }

        public void put(String chiave, String valore) {
            // TODO 3: Acquisisci il lock di SCRITTURA
            rwLock.writeLock().lock();
            try {
                simulaLatenza(50);
                dati.put(chiave, valore);
            } finally {
                // TODO 4: Rilascia il lock di scrittura
                rwLock.writeLock().unlock();
            }
        }

        private void simulaLatenza(long ms) {
            try { Thread.sleep(ms); } catch (InterruptedException e) {}
        }
    }

    // Task di lettura
    static class Lettore implements Runnable {
        private String nome;
        private Object cache; // Può essere uno dei due tipi
        private String[] chiavi;
        private int letture;

        public Lettore(String nome, Object cache, String[] chiavi, int letture) {
            this.nome = nome;
            this.cache = cache;
            this.chiavi = chiavi;
            this.letture = letture;
        }

        @Override
        public void run() {
            for (int i = 0; i < letture; i++) {
                String chiave = chiavi[i % chiavi.length];
                String valore;

                if (cache instanceof CacheSynchronized) {
                    valore = ((CacheSynchronized) cache).get(chiave);
                } else {
                    valore = ((CacheReadWriteLock) cache).get(chiave);
                }
            }
        }
    }

    // Task di scrittura
    static class Scrittore implements Runnable {
        private String nome;
        private Object cache;
        private int scritture;

        public Scrittore(String nome, Object cache, int scritture) {
            this.nome = nome;
            this.cache = cache;
            this.scritture = scritture;
        }

        @Override
        public void run() {
            for (int i = 0; i < scritture; i++) {
                String chiave = "config_" + (i % 5);
                String valore = "value_" + System.nanoTime();

                if (cache instanceof CacheSynchronized) {
                    ((CacheSynchronized) cache).put(chiave, valore);
                } else {
                    ((CacheReadWriteLock) cache).put(chiave, valore);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Esercizio 3: ReadWriteLock per Cache ===\n");

        String[] chiavi = {"config_0", "config_1", "config_2", "config_3", "config_4"};

        // Prepopola le cache
        CacheSynchronized cacheSynch = new CacheSynchronized();
        CacheReadWriteLock cacheRW = new CacheReadWriteLock();

        for (String chiave : chiavi) {
            cacheSynch.put(chiave, "initial_" + chiave);
            cacheRW.put(chiave, "initial_" + chiave);
        }

        // Configurazione test: molti lettori, pochi scrittori
        int numLettori = 5;
        int numScrittori = 1;
        int letturePerThread = 20;
        int scritturePerThread = 5;

        // ======================
        // Test con synchronized
        // ======================
        System.out.println("--- Test con synchronized ---");

        Thread[] threadsSynch = new Thread[numLettori + numScrittori];
        for (int i = 0; i < numLettori; i++) {
            threadsSynch[i] = new Thread(new Lettore("L" + i, cacheSynch, chiavi, letturePerThread));
        }
        for (int i = 0; i < numScrittori; i++) {
            threadsSynch[numLettori + i] = new Thread(new Scrittore("S" + i, cacheSynch, scritturePerThread));
        }

        long startSynch = System.currentTimeMillis();

        for (Thread t : threadsSynch) t.start();
        for (Thread t : threadsSynch) t.join();

        long tempoSynch = System.currentTimeMillis() - startSynch;
        System.out.println("Tempo: " + tempoSynch + " ms\n");

        // ======================
        // Test con ReadWriteLock
        // ======================
        System.out.println("--- Test con ReadWriteLock ---");

        Thread[] threadsRW = new Thread[numLettori + numScrittori];
        for (int i = 0; i < numLettori; i++) {
            threadsRW[i] = new Thread(new Lettore("L" + i, cacheRW, chiavi, letturePerThread));
        }
        for (int i = 0; i < numScrittori; i++) {
            threadsRW[numLettori + i] = new Thread(new Scrittore("S" + i, cacheRW, scritturePerThread));
        }

        long startRW = System.currentTimeMillis();

        for (Thread t : threadsRW) t.start();
        for (Thread t : threadsRW) t.join();

        long tempoRW = System.currentTimeMillis() - startRW;
        System.out.println("Tempo: " + tempoRW + " ms\n");

        // ======================
        // Confronto
        // ======================
        System.out.println("=== Confronto ===");
        System.out.println("synchronized:   " + tempoSynch + " ms");
        System.out.println("ReadWriteLock:  " + tempoRW + " ms");

        if (tempoRW < tempoSynch) {
            double speedup = (double) tempoSynch / tempoRW;
            System.out.printf("ReadWriteLock è %.2fx più veloce!%n", speedup);
        } else {
            System.out.println("I tempi sono simili (prova ad aumentare i lettori)");
        }

        // TODO 5: Aumenta numLettori a 10 o 20 e osserva la differenza

        // TODO 6: Cosa succede se ci sono molti scrittori e pochi lettori?
        // ReadWriteLock è ancora vantaggioso?

        // TODO 7: Prova a rimuovere simulaLatenza() - c'è ancora differenza?
    }
}

/*
 * PERCHÉ ReadWriteLock È PIÙ VELOCE?
 *
 * Con synchronized:
 * - Tutti i thread (lettori E scrittori) competono per lo stesso lock
 * - I lettori si bloccano a vicenda anche se potrebbero leggere in parallelo
 *
 * Thread 1: [=== lettura ===][attende...][=== lettura ===]
 * Thread 2: [attende.........][=== lettura ===][attende..]
 * Thread 3: [attende...................][=== lettura ===]
 *
 * Con ReadWriteLock:
 * - Più lettori possono avere il read lock contemporaneamente
 * - Solo il write lock è esclusivo
 *
 * Thread 1: [=== lettura ===][=== lettura ===][attende (scrittura)]
 * Thread 2: [=== lettura ===][=== lettura ===][attende (scrittura)]
 * Thread 3: [=== lettura ===][=== lettura ===][=== scrittura ===]
 *
 * Quando le letture sono predominanti, il vantaggio è enorme!
 */
