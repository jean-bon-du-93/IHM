package fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
// import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.attaque.AvancePokemonAdversaire; // REMOVED

public class VerificationPokemonAdversaire extends VerificationPokemon {
    public VerificationPokemonAdversaire(Joueur joueurActif) {
        super(joueurActif.getAdversaire());
    }

    @Override
    public void continuerVerification() {
        // ici joueur représente celui sur lequel on fait la vérifi soit l'adversaire de l'actif dans le jeu
        // on continue sur la vérification du joueut Attaquant, cad l'adversaire du joueur en cours de vérification
        joueur.getAdversaire().setEtatCourant(new VerificationPokemonAttaquant(joueur.getAdversaire()));
        joueur.getAdversaire().getEtatCourant().verifierPokemonKO();
    }

    @Override
    public void avancerPokemon() {
        // 'this.joueur' is the defender (owner of the KO'd Pokemon),
        // because super(joueurActif.getAdversaire()) was called.
        Joueur attacker = this.joueur.getAdversaire();
        Joueur defender = this.joueur;

        defender.setEtatCourant(new EtatJoueurChoisitSonPokemonActif(defender, attacker));
        getJeu().joueurActifProperty().set(defender); // Temporarily make defender active for UI
        // The state EtatJoueurChoisitSonPokemonActif will prompt the defender.
        // Its passerALEtatSuivant() will handle returning control to the attacker.
    }

}