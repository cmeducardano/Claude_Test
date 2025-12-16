/**
 * LIVELLO 3 - Esercizio 2: Il Conto Bancario Sicuro
 *
 * Obiettivo: Sincronizzare correttamente le operazioni bancarie
 *            per evitare prelievi non autorizzati e saldi negativi.
 *
 * Riprendi il problema del Livello 2 e risolvilo con synchronized.
 */
public class Esercizio02_ContoBancarioSicuro {

    static class ContoBancario {
        private String intestatario;
        private double saldo;
        private final Object lock = new Object();

        public ContoBancario(String intestatario, double saldoIniziale) {
            this.intestatario = intestatario;
            this.saldo = saldoIniziale;
        }

        // TODO 1: Sincronizza questo metodo per rendere atomica
        // l'operazione di verifica saldo + prelievo
        public boolean preleva(double importo, String operatore) {
            System.out.printf("[%s] Richiesta prelievo di %.2f€ (Saldo: %.2f€)%n",
                    operatore, importo, saldo);

            // Simula elaborazione
            pausa(100);

            if (saldo >= importo) {
                pausa(100);
                saldo -= importo;
                System.out.printf("[%s] Prelievo EFFETTUATO. Nuovo saldo: %.2f€%n",
                        operatore, importo, saldo);
                return true;
            } else {
                System.out.printf("[%s] Prelievo RIFIUTATO. Saldo insufficiente.%n",
                        operatore);
                return false;
            }
        }

        // TODO 2: Sincronizza anche questo metodo
        public void deposita(double importo, String operatore) {
            System.out.printf("[%s] Deposito di %.2f€%n", operatore, importo);
            pausa(50);
            saldo += importo;
            System.out.printf("[%s] Deposito completato. Saldo: %.2f€%n",
                    operatore, saldo);
        }

        // TODO 3: E questo metodo? Serve sincronizzarlo?
        public double getSaldo() {
            return saldo;
        }

        private void pausa(long ms) {
            try { Thread.sleep(ms); } catch (InterruptedException e) {}
        }
    }

    // Trasferimento tra conti (operazione più complessa)
    static class ContoBancarioAvanzato {
        private String intestatario;
        private double saldo;
        private final Object lock = new Object();

        public ContoBancarioAvanzato(String intestatario, double saldoIniziale) {
            this.intestatario = intestatario;
            this.saldo = saldoIniziale;
        }

        public synchronized boolean preleva(double importo) {
            if (saldo >= importo) {
                saldo -= importo;
                return true;
            }
            return false;
        }

        public synchronized void deposita(double importo) {
            saldo += importo;
        }

        public synchronized double getSaldo() {
            return saldo;
        }

        public String getIntestatario() {
            return intestatario;
        }

        // TODO 4: Implementa un metodo per trasferire denaro da questo conto
        // a un altro conto. ATTENZIONE: Questo è più complicato!
        // Devi acquisire il lock di ENTRAMBI i conti.
        // Cosa può andare storto? (Pensa al deadlock!)

        /*
        public boolean trasferisci(ContoBancarioAvanzato destinatario, double importo) {
            // Come acquisire entrambi i lock in modo sicuro?
            // HINT: Una soluzione è usare un ordine consistente per i lock.
            return false;
        }
        */
    }

    // Cliente che effettua operazioni
    static class Cliente implements Runnable {
        private String nome;
        private ContoBancario conto;
        private double[] operazioni;

        public Cliente(String nome, ContoBancario conto, double[] operazioni) {
            this.nome = nome;
            this.conto = conto;
            this.operazioni = operazioni;
        }

        @Override
        public void run() {
            for (double importo : operazioni) {
                if (importo > 0) {
                    conto.deposita(importo, nome);
                } else {
                    conto.preleva(-importo, nome);
                }
                try { Thread.sleep(50); } catch (InterruptedException e) { return; }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Esercizio 2: Il Conto Bancario Sicuro ===\n");

        final int NUM_TEST = 5;
        int testPassati = 0;

        for (int test = 1; test <= NUM_TEST; test++) {
            System.out.println("--- Test " + test + " ---\n");

            ContoBancario conto = new ContoBancario("Famiglia Rossi", 1000.0);

            double[] opMarito = {-300, -400, -200};  // -900€
            double[] opMoglie = {-350, -250, -300};  // -900€

            Thread marito = new Thread(new Cliente("Mario", conto, opMarito));
            Thread moglie = new Thread(new Cliente("Laura", conto, opMoglie));

            marito.start();
            moglie.start();

            marito.join();
            moglie.join();

            System.out.println("\nSaldo finale: " + conto.getSaldo() + "€");

            if (conto.getSaldo() >= 0) {
                System.out.println("OK - Saldo non negativo!");
                testPassati++;
            } else {
                System.out.println("ERRORE - Saldo negativo!");
            }
            System.out.println();
        }

        System.out.println("=================================");
        System.out.printf("Test passati: %d/%d%n", testPassati, NUM_TEST);

        if (testPassati == NUM_TEST) {
            System.out.println("Tutte le operazioni sono sicure!");
        } else {
            System.out.println("Alcune operazioni non sono sincronizzate correttamente.");
        }

        // TODO 5: Dopo aver sincronizzato i metodi, il saldo non dovrebbe
        // mai diventare negativo. Verifica eseguendo il programma.

        // TODO 6: Qual è il saldo finale tipico? È sempre lo stesso?
        // Perché il saldo finale può variare anche con la sincronizzazione?
        // (HINT: L'ordine delle operazioni è determinato, ma quale operazione
        //  viene rifiutata dipende da chi arriva prima...)
    }
}

/*
 * RIFLESSIONE SUL LOCK ORDERING:
 *
 * Nel TODO 4, il trasferimento tra conti richiede di acquisire
 * due lock contemporaneamente. Questo può causare DEADLOCK:
 *
 * Thread 1: trasferisce da A a B       Thread 2: trasferisce da B a A
 * ────────────────────────             ──────────────────────────
 * lock(A)                              lock(B)
 *     [attende B...]                       [attende A...]
 *         ← DEADLOCK! →
 *
 * Soluzione: Lock Ordering - acquisisci i lock sempre nello stesso ordine!
 * Es: ordina per ID dell'oggetto o usa System.identityHashCode()
 *
 * Lo approfondiremo nel Livello 5.
 */
