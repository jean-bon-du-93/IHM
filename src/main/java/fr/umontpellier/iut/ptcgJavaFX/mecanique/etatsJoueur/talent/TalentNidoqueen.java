package fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.talent;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.deplacement.PiocheVersMain;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.EtatJoueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.TourNormal;

import java.util.List;

public class TalentNidoqueen extends EtatJoueur {

    public TalentNidoqueen(Joueur joueurActif) {
        super(joueurActif);
        getJeu().instructionProperty().setValue("Choisissez un pokémon de votre deck.");
    }

    @Override
    public void carteChoisie(String numCarte) {
        // Défausser une carte énergie du pokémon
        List<String> choixPossibles = joueur.getChoixComplementaires().stream()
                        .map(Carte::getId)
                        .toList();
        if (!choixPossibles.isEmpty() && choixPossibles.contains(numCarte)) {
            Carte carteRevelee = Carte.get(numCarte); // Get the card object before moving
            joueur.deplacerCarteComplementaire(numCarte, new PiocheVersMain());
            if (carteRevelee != null && joueur.getJeu() != null) { // Add null checks for safety
                joueur.getJeu().logRevealCard(joueur, carteRevelee, "Searched from deck with Nidoqueen's Talent");
            }
            joueur.melangerPioche();
            joueur.viderListChoixComplementaires();
            joueur.setEtatCourant(new TourNormal(joueur));
        }
    }

}
