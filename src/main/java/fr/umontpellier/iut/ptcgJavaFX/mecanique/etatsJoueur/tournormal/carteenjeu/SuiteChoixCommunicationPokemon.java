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
            joueur.deplacerCarteComplementaire(numCarte, new PiocheVersMain());
            // It's important to clear choixComplementaires *before* shuffling if it contained references to pioche.
            // However, deplacerCarteComplementaire already removes the card from choixComplementaires.
            // If viderListChoixComplementaires() is meant to clear the list that was populated from the deck,
            // it should be called before shuffling.
            // For safety, let's assume viderListChoixComplementaires clears the temporary list of choices.
            joueur.viderListChoixComplementaires(); // Clear the list of choices from deck
            joueur.melangerPioche(); // Added this line
            onFinAction(); // Proceeds to TourNormal
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