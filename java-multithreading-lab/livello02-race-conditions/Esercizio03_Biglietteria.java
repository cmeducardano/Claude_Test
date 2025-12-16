/**
 * LIVELLO 2 - Esercizio 3: La Biglietteria
 *
 * Obiettivo: Simulare la vendita concorrente di biglietti per un concerto
 *            e osservare i problemi di overselling (vendita eccessiva).
 *
 * Scenario: Un concerto con 100 posti disponibili.
 *           Più sportelli vendono biglietti contemporaneamente.
 */
public class Esercizio03_Biglietteria {

    static class Concerto {
        private String nome;
        private int bigliettiDisponibili;
        private int bigliettiVenduti;

        public Concerto(String nome, int capacita) {
            this.nome = nome;
            this.bigliettiDisponibili = capacita;
            this.bigliettiVenduti = 0;
        }

        // ATTENZIONE: Metodo NON thread-safe!
        public boolean vendiBiglietto(String sportello) {
            if (bigliettiDisponibili > 0) {
                // Simula il tempo di emissione del biglietto
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return false;
                }

                bigliettiDisponibili--;
                bigliettiVenduti++;

                System.out.printf("[%s] Biglietto venduto! Disponibili: %d%n",
                        sportello, bigliettiDisponibili);
                return true;
            }

            System.out.printf("[%s] ESAURITI!%n", sportello);
            return false;
        }

        public int getBigliettiDisponibili() {
            return bigliettiDisponibili;
        }

        public int getBigliettiVenduti() {
            return bigliettiVenduti;
        }

        public String getNome() {
            return nome;
        }
    }

    // Sportello che vende biglietti
    static class Sportello implements Runnable {
        private String id;
        private Concerto concerto;
        private int venditeEffettuate;
        private int tentativiTotali;

        public Sportello(String id, Concerto concerto, int tentativiTotali) {
            this.id = id;
            this.concerto = concerto;
            this.venditeEffettuate = 0;
            this.tentativiTotali = tentativiTotali;
        }

        @Override
        public void run() {
            for (int i = 0; i < tentativiTotali; i++) {
                if (concerto.vendiBiglietto(id)) {
                    venditeEffettuate++;
                }

                // Pausa tra un cliente e l'altro
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        public int getVenditeEffettuate() {
            return venditeEffettuate;
        }

        public String getId() {
            return id;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Esercizio 3: La Biglietteria ===\n");

        // Concerto con 100 posti
        final int CAPACITA = 100;
        Concerto concerto = new Concerto("Rock Festival 2024", CAPACITA);

        System.out.println("Concerto: " + concerto.getNome());
        System.out.println("Posti disponibili: " + CAPACITA);
        System.out.println("\n--- Apertura vendite ---\n");

        // 5 sportelli, ognuno cerca di vendere 30 biglietti
        // Totale tentativi: 150 (ma abbiamo solo 100 posti!)
        final int NUM_SPORTELLI = 5;
        final int TENTATIVI_PER_SPORTELLO = 30;

        Sportello[] sportelli = new Sportello[NUM_SPORTELLI];
        Thread[] threads = new Thread[NUM_SPORTELLI];

        for (int i = 0; i < NUM_SPORTELLI; i++) {
            sportelli[i] = new Sportello("Sportello-" + (i + 1), concerto, TENTATIVI_PER_SPORTELLO);
            threads[i] = new Thread(sportelli[i]);
        }

        long inizio = System.currentTimeMillis();

        // Avvia tutti gli sportelli contemporaneamente
        for (Thread t : threads) {
            t.start();
        }

        // Aspetta che tutti finiscano
        for (Thread t : threads) {
            t.join();
        }

        long fine = System.currentTimeMillis();

        // Riepilogo vendite
        System.out.println("\n========== RIEPILOGO ==========");
        System.out.println("Durata vendite: " + (fine - inizio) + " ms\n");

        int totaleVendite = 0;
        for (Sportello s : sportelli) {
            System.out.printf("%s: %d biglietti venduti%n", s.getId(), s.getVenditeEffettuate());
            totaleVendite += s.getVenditeEffettuate();
        }

        System.out.println("\n--- Verifica ---");
        System.out.println("Biglietti venduti (somma sportelli): " + totaleVendite);
        System.out.println("Biglietti venduti (contatore):       " + concerto.getBigliettiVenduti());
        System.out.println("Biglietti disponibili:               " + concerto.getBigliettiDisponibili());
        System.out.println("================================\n");

        // Verifica coerenza
        int bigliettiContati = concerto.getBigliettiVenduti() + concerto.getBigliettiDisponibili();
        if (bigliettiContati != CAPACITA) {
            System.out.println("*** ERRORE: I conti non tornano! ***");
            System.out.println("Venduti + Disponibili = " + bigliettiContati);
            System.out.println("Capacità originale = " + CAPACITA);
        }

        if (concerto.getBigliettiVenduti() > CAPACITA) {
            System.out.println("*** OVERSELLING RILEVATO! ***");
            System.out.println("Sono stati venduti " + (concerto.getBigliettiVenduti() - CAPACITA) +
                    " biglietti in più della capacità!");
        }

        if (concerto.getBigliettiDisponibili() < 0) {
            System.out.println("*** ERRORE: Biglietti disponibili negativi! ***");
        }

        // TODO 1: Esegui il programma più volte.
        // Quante volte ottieni valori incoerenti?


        // TODO 2: Aumenta il numero di sportelli a 10 e i tentativi a 50.
        // Il problema diventa più evidente?


        // TODO 3: Rimuovi i Thread.sleep() nel metodo vendiBiglietto().
        // Il bug si manifesta ancora? Perché?


        // TODO 4: Prova a identificare esattamente dove avviene la race condition.
        // Quali linee di codice dovrebbero essere eseguite atomicamente?
    }
}

/*
 * ANALISI DEL PROBLEMA:
 *
 * Nel metodo vendiBiglietto(), ci sono TRE operazioni che dovrebbero
 * essere atomiche:
 *
 * 1. LEGGI bigliettiDisponibili
 * 2. VERIFICA se > 0
 * 3. DECREMENTA bigliettiDisponibili e incrementa bigliettiVenduti
 *
 * Il problema del "Time of Check vs Time of Use" (TOCTOU):
 *
 * Sportello 1                     Sportello 2
 * ───────────                     ───────────
 * legge disponibili = 1
 *                                 legge disponibili = 1
 * verifica: 1 > 0? SÌ
 *                                 verifica: 1 > 0? SÌ
 * decrementa: disponibili = 0
 *                                 decrementa: disponibili = -1  ← ERRORE!
 * venduti = 1
 *                                 venduti = 2                   ← OVERSELLING!
 */
