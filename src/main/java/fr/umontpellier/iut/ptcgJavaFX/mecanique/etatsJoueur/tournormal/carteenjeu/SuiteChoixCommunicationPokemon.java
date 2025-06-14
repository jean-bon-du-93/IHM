package fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.carteenjeu;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.deplacement.PiocheVersMain;

import java.util.List;

public class SuiteChoixCommunicationPokemon extends CarteEnJeu {

    public SuiteChoixCommunicationPokemon(Joueur joueurActif) {
        super(joueurActif);
        getJeu().instructionProperty().setValue("Choisissez un pokémon de votre deck.");
    }

    @Override
    public void carteChoisie(String numCarte) {
        List<String> choixPossibles = joueur.getChoixComplementaires().stream().map(Carte::getId).toList();
        if (!choixPossibles.isEmpty() && choixPossibles.contains(numCarte)) {
            Carte carteReveleeDuDeck = Carte.get(numCarte); // Get the card object
            joueur.deplacerCarteComplementaire(numCarte, new PiocheVersMain());
            if (carteReveleeDuDeck != null && joueur.getJeu() != null) { // Add null checks
                joueur.getJeu().logRevealCard(joueur, carteReveleeDuDeck, "Searched from deck with Pokémon Communication");
            }
            joueur.viderListChoixComplementaires();
            joueur.melangerPioche();
            onFinAction();
        }
    }

    @Override
    public void passer() {
        // Player chose not to take a Pokemon after seeing the list from deck.
        // The deck was still "searched" (player saw its contents or a filtered list).
        // So, it should be shuffled.
        joueur.viderListChoixComplementaires(); // Clear the list of choices from deck
        joueur.melangerPioche(); // Added this line
        onFinAction(); // Proceeds to TourNormal
    }

}