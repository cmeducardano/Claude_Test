/**
 * LIVELLO 3 - Esercizio 3: Blocchi Sincronizzati e Performance
 *
 * Obiettivo: Comprendere la differenza tra metodi synchronized e blocchi synchronized,
 *            e perché è importante minimizzare la sezione critica.
 *
 * Scenario: Un sistema di logging che deve scrivere messaggi in modo thread-safe,
 *           ma anche elaborare dati (operazione costosa che non richiede sincronizzazione).
 */
public class Esercizio03_BlocchiSync {

    // Versione 1: Metodo interamente sincronizzato (inefficiente)
    static class LoggerInefficient {
        private StringBuilder log = new StringBuilder();

        public synchronized void scriviLog(String messaggio) {
            // Questa operazione NON richiede sincronizzazione!
            String messaggioFormattato = formattaMessaggio(messaggio);

            // Solo QUESTA parte richiede sincronizzazione
            log.append(messaggioFormattato);
        }

        private String formattaMessaggio(String msg) {
            // Simula elaborazione costosa (es. formattazione, timestamp, ecc.)
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(System.currentTimeMillis()).append("] ");
            sb.append(Thread.currentThread().getName()).append(": ");
            sb.append(msg.toUpperCase()).append("\n");

            // Simula lavoro computazionale
            for (int i = 0; i < 1000; i++) {
                Math.sqrt(i);
            }

            return sb.toString();
        }

        public synchronized String getLog() {
            return log.toString();
        }
    }

    // Versione 2: Blocco sincronizzato solo dove necessario (efficiente)
    static class LoggerEfficient {
        private StringBuilder log = new StringBuilder();
        private final Object lock = new Object();

        public void scriviLog(String messaggio) {
            // Questa parte NON è sincronizzata - può eseguire in parallelo!
            String messaggioFormattato = formattaMessaggio(messaggio);

            // TODO 1: Sincronizza SOLO l'accesso a log.append()
            // usando l'oggetto "lock"
            log.append(messaggioFormattato);
        }

        private String formattaMessaggio(String msg) {
            // Stessa elaborazione costosa
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(System.currentTimeMillis()).append("] ");
            sb.append(Thread.currentThread().getName()).append(": ");
            sb.append(msg.toUpperCase()).append("\n");

            for (int i = 0; i < 1000; i++) {
                Math.sqrt(i);
            }

            return sb.toString();
        }

        public String getLog() {
            synchronized (lock) {
                return log.toString();
            }
        }
    }

    // Task che scrive molti messaggi
    static class WriterTask implements Runnable {
        private LoggerInefficient loggerIneff;
        private LoggerEfficient loggerEff;
        private int numMessaggi;
        private boolean usaEfficiente;

        public WriterTask(LoggerInefficient li, LoggerEfficient le, int n, boolean efficiente) {
            this.loggerIneff = li;
            this.loggerEff = le;
            this.numMessaggi = n;
            this.usaEfficiente = efficiente;
        }

        @Override
        public void run() {
            for (int i = 0; i < numMessaggi; i++) {
                if (usaEfficiente) {
                    loggerEff.scriviLog("Messaggio " + i);
                } else {
                    loggerIneff.scriviLog("Messaggio " + i);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Esercizio 3: Performance della Sincronizzazione ===\n");

        final int NUM_THREAD = 4;
        final int MESSAGGI_PER_THREAD = 100;

        // Test 1: Logger inefficiente (metodo synchronized)
        System.out.println("--- Test Logger Inefficiente ---");
        LoggerInefficient logIneff = new LoggerInefficient();
        Thread[] threadsIneff = new Thread[NUM_THREAD];

        long startIneff = System.currentTimeMillis();

        for (int i = 0; i < NUM_THREAD; i++) {
            threadsIneff[i] = new Thread(
                    new WriterTask(logIneff, null, MESSAGGI_PER_THREAD, false));
            threadsIneff[i].start();
        }

        for (Thread t : threadsIneff) t.join();

        long endIneff = System.currentTimeMillis();
        System.out.println("Tempo: " + (endIneff - startIneff) + " ms");

        // Test 2: Logger efficiente (blocco synchronized minimale)
        System.out.println("\n--- Test Logger Efficiente ---");
        LoggerEfficient logEff = new LoggerEfficient();
        Thread[] threadsEff = new Thread[NUM_THREAD];

        long startEff = System.currentTimeMillis();

        for (int i = 0; i < NUM_THREAD; i++) {
            threadsEff[i] = new Thread(
                    new WriterTask(null, logEff, MESSAGGI_PER_THREAD, true));
            threadsEff[i].start();
        }

        for (Thread t : threadsEff) t.join();

        long endEff = System.currentTimeMillis();
        System.out.println("Tempo: " + (endEff - startEff) + " ms");

        // Confronto
        System.out.println("\n=== Confronto ===");
        long tempoIneff = endIneff - startIneff;
        long tempoEff = endEff - startEff;

        System.out.println("Logger Inefficiente: " + tempoIneff + " ms");
        System.out.println("Logger Efficiente:   " + tempoEff + " ms");

        if (tempoIneff > tempoEff) {
            double speedup = (double) tempoIneff / tempoEff;
            System.out.printf("Speedup: %.2fx più veloce%n", speedup);
        } else {
            System.out.println("Hmm, i risultati sono simili. Prova ad aumentare MESSAGGI_PER_THREAD");
        }

        // TODO 2: Esegui il test e osserva la differenza di performance.
        // Perché il logger efficiente è più veloce?


        // TODO 3: Aumenta la complessità di formattaMessaggio() (es. più iterazioni)
        // La differenza di performance aumenta? Perché?


        // TODO 4: Cosa succederebbe se la parte sincronizzata (append) fosse
        // molto più lenta della parte non sincronizzata (formattazione)?
        // In quel caso, ci sarebbe differenza tra le due versioni?


        // TODO 5: Verifica che entrambi i logger abbiano registrato tutti i messaggi
        // Conta le righe del log (split by "\n") e verifica che siano
        // NUM_THREAD * MESSAGGI_PER_THREAD
    }
}

/*
 * LEZIONE CHIAVE:
 *
 * PRIMA (Metodo synchronized):
 * Thread 1: [=== formatta + scrivi ===][attende...........][=== formatta + scrivi ===]
 * Thread 2: [attende...................][=== formatta + scrivi ===][attende........]
 *
 * DOPO (Blocco synchronized minimale):
 * Thread 1: [=== formatta ===][#scrivi#][=== formatta ===][#scrivi#]
 * Thread 2: [=== formatta ===][attendi][#scrivi#][=== formatta ===][#scrivi#]
 *                                    ^
 *                              Solo qui c'è contesa!
 *
 * La formattazione può avvenire in PARALLELO, solo la scrittura è serializzata.
 *
 * Regola: Minimizza sempre la sezione critica!
 */
