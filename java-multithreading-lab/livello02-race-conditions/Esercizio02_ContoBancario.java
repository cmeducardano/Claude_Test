/**
 * LIVELLO 2 - Esercizio 2: Il Conto Bancario
 *
 * Obiettivo: Simulare operazioni bancarie concorrenti e osservare
 *            come le race condition possono causare problemi finanziari!
 *
 * Scenario: Un conto bancario condiviso tra marito e moglie.
 *           Entrambi effettuano operazioni simultaneamente.
 */
public class Esercizio02_ContoBancario {

    static class ContoBancario {
        private String intestatario;
        private double saldo;

        public ContoBancario(String intestatario, double saldoIniziale) {
            this.intestatario = intestatario;
            this.saldo = saldoIniziale;
        }

        // ATTENZIONE: Questo metodo NON è thread-safe!
        public boolean preleva(double importo, String operatore) {
            System.out.printf("[%s] Richiesta prelievo di %.2f€ (Saldo attuale: %.2f€)%n",
                    operatore, importo, saldo);

            // Simula il tempo di verifica del saldo
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return false;
            }

            // Verifica se c'è saldo sufficiente
            if (saldo >= importo) {
                // Simula il tempo di elaborazione della transazione
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return false;
                }

                saldo -= importo;
                System.out.printf("[%s] Prelievo di %.2f€ EFFETTUATO. Nuovo saldo: %.2f€%n",
                        operatore, importo, saldo);
                return true;
            } else {
                System.out.printf("[%s] Prelievo di %.2f€ RIFIUTATO. Saldo insufficiente!%n",
                        operatore, importo);
                return false;
            }
        }

        public void deposita(double importo, String operatore) {
            System.out.printf("[%s] Deposito di %.2f€%n", operatore, importo);

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return;
            }

            saldo += importo;
            System.out.printf("[%s] Deposito completato. Nuovo saldo: %.2f€%n",
                    operatore, saldo);
        }

        public double getSaldo() {
            return saldo;
        }
    }

    // Cliente che effettua operazioni sul conto
    static class Cliente implements Runnable {
        private String nome;
        private ContoBancario conto;
        private double[] operazioni; // positivo = deposito, negativo = prelievo

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

                // Piccola pausa tra le operazioni
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Esercizio 2: Il Conto Bancario ===\n");

        // Conto con saldo iniziale di 1000€
        ContoBancario conto = new ContoBancario("Famiglia Rossi", 1000.0);

        System.out.println("Saldo iniziale: " + conto.getSaldo() + "€\n");

        // Operazioni del marito: prelievi
        double[] operazioniMarito = {-300, -400, -200};  // Totale: -900€

        // Operazioni della moglie: prelievi
        double[] operazioniMoglie = {-350, -250, -300};  // Totale: -900€

        // Se eseguite in sequenza: saldo finale = 1000 - 900 - 900 = -800€ (impossibile!)
        // Quindi alcuni prelievi dovrebbero essere rifiutati...

        Thread marito = new Thread(new Cliente("Mario", conto, operazioniMarito));
        Thread moglie = new Thread(new Cliente("Laura", conto, operazioniMoglie));

        System.out.println("--- Inizio operazioni ---\n");

        marito.start();
        moglie.start();

        marito.join();
        moglie.join();

        System.out.println("\n--- Fine operazioni ---");
        System.out.println("\nSaldo finale: " + conto.getSaldo() + "€");

        // Verifica
        if (conto.getSaldo() < 0) {
            System.out.println("\n*** ERRORE GRAVE: IL SALDO È NEGATIVO! ***");
            System.out.println("La banca ha perso soldi a causa della race condition!");
        }

        // TODO 1: Esegui più volte il programma
        // Riesci a ottenere un saldo negativo? Cosa è successo?


        // TODO 2: Quali sono le sezioni critiche in questo codice?
        // Indica i numeri di riga che andrebbero protette.


        // TODO 3: Descrivi uno scenario specifico (interleaving) che
        // porta a un saldo negativo. Usa questo schema:
        //
        // Mario                          Laura
        // ──────                          ─────
        // legge saldo = 1000
        //                                 legge saldo = 1000
        // ...
    }
}

/*
 * PROBLEMA: CHECK-THEN-ACT
 *
 * Il pattern "controlla poi agisci" (check-then-act) è intrinsecamente
 * non thread-safe se non sincronizzato:
 *
 *   if (condizione) {     // CHECK
 *       faiQualcosa();    // ACT
 *   }
 *
 * Tra il CHECK e l'ACT, un altro thread può modificare lo stato,
 * rendendo la condizione non più vera!
 *
 * Soluzione: rendere atomica l'intera operazione check-then-act.
 * (Lo vedremo nel Livello 3)
 */
