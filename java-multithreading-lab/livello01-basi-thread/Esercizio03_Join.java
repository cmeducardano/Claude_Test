/**
 * LIVELLO 1 - Esercizio 3: Coordinazione con Join
 *
 * Obiettivo: Usare join() per attendere che i thread terminino
 *            prima di procedere con altre operazioni.
 *
 * Scenario: Un ristorante dove più chef preparano piatti in parallelo.
 *           Il cameriere deve aspettare che TUTTI i piatti siano pronti
 *           prima di servire al tavolo.
 */
public class Esercizio03_Join {

    static class Chef implements Runnable {
        private String nome;
        private String piatto;
        private int tempoPreparazione; // in millisecondi

        public Chef(String nome, String piatto, int tempoPreparazione) {
            this.nome = nome;
            this.piatto = piatto;
            this.tempoPreparazione = tempoPreparazione;
        }

        @Override
        public void run() {
            System.out.println("Chef " + nome + " inizia a preparare: " + piatto);

            try {
                // Simula il tempo di preparazione
                Thread.sleep(tempoPreparazione);
            } catch (InterruptedException e) {
                System.out.println("Chef " + nome + " è stato interrotto!");
                return;
            }

            System.out.println(">>> Chef " + nome + " ha completato: " + piatto + " <<<");
        }

        public String getPiatto() {
            return piatto;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Esercizio 3: La Cucina del Ristorante ===\n");
        System.out.println("Il cameriere prende l'ordine per il tavolo 5...\n");

        // Creiamo gli chef con i loro piatti
        Chef chefMarco = new Chef("Marco", "Risotto ai funghi", 3000);
        Chef chefGiulia = new Chef("Giulia", "Tagliata di manzo", 4000);
        Chef chefLuca = new Chef("Luca", "Tiramisù", 2000);

        // Creiamo i thread
        Thread tMarco = new Thread(chefMarco);
        Thread tGiulia = new Thread(chefGiulia);
        Thread tLuca = new Thread(chefLuca);

        long inizioTempo = System.currentTimeMillis();

        // Avviamo tutti gli chef
        System.out.println("--- Gli chef iniziano a cucinare in parallelo ---\n");
        tMarco.start();
        tGiulia.start();
        tLuca.start();

        // TODO 1: Usa join() per aspettare che TUTTI gli chef finiscano
        // Senza join(), il cameriere servirebbe prima che i piatti siano pronti!

        // DECOMMENTA e completa:
        /*
        try {
            // Aspetta che Marco finisca
            tMarco.???();

            // Aspetta che Giulia finisca
            // ...

            // Aspetta che Luca finisca
            // ...

        } catch (InterruptedException e) {
            System.out.println("Attesa interrotta!");
        }
        */

        long fineTempo = System.currentTimeMillis();
        long tempoTotale = fineTempo - inizioTempo;

        System.out.println("\n--- Tutti i piatti sono pronti! ---");
        System.out.println("Il cameriere serve il tavolo 5.");
        System.out.println("\nTempo totale di preparazione: " + tempoTotale + " ms");

        // TODO 2: Rispondi a questa domanda nei commenti:
        // Se gli chef lavorano in sequenza (uno dopo l'altro),
        // quanto tempo ci vorrebbe? (3000 + 4000 + 2000 = ?)
        // Invece, lavorando in parallelo, quanto ci è voluto?
        // Questo dimostra il vantaggio del _____________.


        // =====================================================
        // PARTE BONUS: Join con timeout
        // =====================================================

        System.out.println("\n=== BONUS: Ordine urgente! ===\n");

        Chef chefPaolo = new Chef("Paolo", "Pasta al tartufo", 5000);
        Thread tPaolo = new Thread(chefPaolo);

        System.out.println("Il cliente ha fretta! Aspettiamo massimo 2 secondi...\n");
        tPaolo.start();

        // TODO 3: Usa join con timeout per aspettare MASSIMO 2 secondi
        // Sintassi: thread.join(millisecondi)

        /*
        try {
            tPaolo.join(???);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

        // TODO 4: Verifica se il piatto è pronto usando isAlive()
        /*
        if (tPaolo.isAlive()) {
            System.out.println("Il piatto non è ancora pronto... il cliente se ne va!");
        } else {
            System.out.println("Piatto servito in tempo!");
        }
        */
    }
}

/*
 * DOMANDE DI RIFLESSIONE:
 *
 * 1. Senza le chiamate a join(), cosa stamperebbe il programma?
 *    Prova a commentare i join() e osserva.
 *
 * 2. L'ordine in cui chiami join() sui thread è importante per il tempo totale?
 *    (Es: se chiami prima join su Giulia invece che su Marco)
 *
 * 3. Cosa succede se un thread viene interrotto mentre è in join()?
 *
 * 4. Nel bonus, cosa succede al thread di Paolo quando il main termina?
 */
