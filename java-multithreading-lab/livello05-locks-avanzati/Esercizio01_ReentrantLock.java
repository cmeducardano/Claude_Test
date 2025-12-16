/**
 * LIVELLO 5 - Esercizio 1: ReentrantLock e TryLock
 *
 * Obiettivo: Utilizzare ReentrantLock per gestire risorse condivise
 *            e tryLock per evitare deadlock.
 *
 * Scenario: Un sistema di prenotazione risorse dove i thread provano
 *           a acquisire risorse senza rimanere bloccati indefinitamente.
 */
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class Esercizio01_ReentrantLock {

    // Risorsa condivisa protetta da ReentrantLock
    static class Risorsa {
        private String nome;
        private ReentrantLock lock = new ReentrantLock();
        private String utilizzatore = null;

        public Risorsa(String nome) {
            this.nome = nome;
        }

        // TODO 1: Implementa acquisizione con lock() standard
        public void acquisisci(String utente) {
            lock.lock();
            try {
                utilizzatore = utente;
                System.out.println("[" + nome + "] Acquisita da " + utente);
            } finally {
                // IMPORTANTE: unlock() sempre nel finally!
                // Anche se c'è un'eccezione, il lock deve essere rilasciato
            }
        }

        // Rilascia la risorsa
        public void rilascia(String utente) {
            lock.lock();
            try {
                if (utente.equals(utilizzatore)) {
                    System.out.println("[" + nome + "] Rilasciata da " + utente);
                    utilizzatore = null;
                }
            } finally {
                lock.unlock();
            }
        }

        // TODO 2: Implementa acquisizione con tryLock() (non bloccante)
        // Ritorna true se riesce ad acquisire, false altrimenti
        public boolean tentaAcquisizione(String utente) {
            // Usa lock.tryLock() che ritorna immediatamente
            boolean acquisito = lock.tryLock();

            if (acquisito) {
                try {
                    utilizzatore = utente;
                    System.out.println("[" + nome + "] Acquisita da " + utente + " (tryLock)");
                    return true;
                } catch (Exception e) {
                    lock.unlock();
                    return false;
                }
            } else {
                System.out.println("[" + nome + "] NON disponibile per " + utente);
                return false;
            }

            // NOTA: Non chiamare unlock() se non hai acquisito il lock!
        }

        // TODO 3: Implementa acquisizione con timeout
        // Prova per un massimo di 'timeout' millisecondi
        public boolean tentaAcquisizioneConTimeout(String utente, long timeout) {
            boolean acquisito = false;

            try {
                // Usa lock.tryLock(time, unit)
                acquisito = lock.tryLock(timeout, TimeUnit.MILLISECONDS);

                if (acquisito) {
                    utilizzatore = utente;
                    System.out.println("[" + nome + "] Acquisita da " + utente +
                            " (dopo attesa max " + timeout + "ms)");
                    return true;
                } else {
                    System.out.println("[" + nome + "] Timeout scaduto per " + utente);
                    return false;
                }
            } catch (InterruptedException e) {
                System.out.println("[" + nome + "] Interruzione per " + utente);
                return false;
            }
        }

        // Usa la risorsa per un certo tempo
        public void utilizza(String utente, long durata) {
            if (!lock.isHeldByCurrentThread()) {
                System.out.println("ERRORE: " + utente + " non ha il lock su " + nome);
                return;
            }

            System.out.println("[" + nome + "] In uso da " + utente + "...");
            try {
                Thread.sleep(durata);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void unlock() {
            if (lock.isHeldByCurrentThread()) {
                utilizzatore = null;
                lock.unlock();
            }
        }

        public String getNome() {
            return nome;
        }
    }

    // Worker che prova ad acquisire risorse
    static class Worker implements Runnable {
        private String nome;
        private Risorsa[] risorse;

        public Worker(String nome, Risorsa... risorse) {
            this.nome = nome;
            this.risorse = risorse;
        }

        @Override
        public void run() {
            System.out.println(nome + " inizia il lavoro...");

            for (Risorsa r : risorse) {
                // Prova ad acquisire con timeout
                if (r.tentaAcquisizioneConTimeout(nome, 2000)) {
                    try {
                        r.utilizza(nome, 1000);
                    } finally {
                        r.unlock();
                        System.out.println("[" + r.getNome() + "] Rilasciata da " + nome);
                    }
                }
            }

            System.out.println(nome + " ha finito.");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Esercizio 1: ReentrantLock ===\n");

        // Creiamo alcune risorse condivise
        Risorsa stampante = new Risorsa("Stampante");
        Risorsa scanner = new Risorsa("Scanner");
        Risorsa fax = new Risorsa("Fax");

        // Creiamo worker che competono per le risorse
        Thread t1 = new Thread(new Worker("Alice", stampante, scanner));
        Thread t2 = new Thread(new Worker("Bob", scanner, stampante));
        Thread t3 = new Thread(new Worker("Carol", fax, stampante));

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("\n--- Tutti i worker hanno finito ---");

        // TODO 4: Osserva l'output. Grazie a tryLock con timeout,
        // nessun thread rimane bloccato indefinitamente!

        // TODO 5: Prova a rimuovere il timeout e usare lock() normale.
        // Cosa potrebbe succedere? (Pensa al deadlock!)

        // TODO 6: Cosa stampa lock.isHeldByCurrentThread() quando
        // un thread ha il lock vs quando non ce l'ha?
    }
}

/*
 * ATTENZIONE AL DEADLOCK!
 *
 * Senza tryLock(), questo scenario può causare deadlock:
 *
 * Alice: lock(Stampante)    Bob: lock(Scanner)
 * Alice: lock(Scanner)      Bob: lock(Stampante)
 *        [aspetta Bob]            [aspetta Alice]
 *             ← DEADLOCK! →
 *
 * Con tryLock() o tryLock(timeout):
 *
 * Alice: lock(Stampante)    Bob: lock(Scanner)
 * Alice: tryLock(Scanner)   Bob: tryLock(Stampante)
 *        [false, continua]        [false, continua]
 *             ← Nessun deadlock! →
 *
 * Il thread può decidere di riprovare, rilasciare tutto e aspettare, ecc.
 */
