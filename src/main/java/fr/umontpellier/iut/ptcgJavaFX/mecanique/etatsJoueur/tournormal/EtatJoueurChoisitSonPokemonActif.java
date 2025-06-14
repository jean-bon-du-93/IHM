package fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
// Assuming VerificationPokemonAttaquant is in the same package 'tournormal'
// If not, this import will need adjustment, e.g.:
// import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.attaque.VerificationPokemonAttaquant;

public class EtatJoueurChoisitSonPokemonActif extends AvancePokemon {
    private Joueur nextPlayerToMakeActive;

    public EtatJoueurChoisitSonPokemonActif(Joueur playerMakingChoice, Joueur nextPlayerToMakeActiveAfterChoice) {
        super(playerMakingChoice); // playerMakingChoice is the one choosing their new active
        this.nextPlayerToMakeActive = nextPlayerToMakeActiveAfterChoice;
        // Instruction is set by AvancePokemon's constructor: "Choisissez un nouveau pokémon actif."
        // AvancePokemon also calls joueur.choisirPokemonActif(); which might set the UI prompt.
    }

    @Override
    public void passerALEtatSuivant() {
        // This is called after 'this.joueur' (playerMakingChoice, the defender)
        // has successfully chosen a new active Pokemon (via logic in AvancePokemon).

        // Now, switch control back to nextPlayerToMakeActive (the original attacker).
        getJeu().joueurActifProperty().set(this.nextPlayerToMakeActive);

        // Set the attacker's state to continue KO checks for their own Pokemon.
        // This ensures if the attacker also had a Pokemon KO'd (e.g. by confusion damage after attacking),
        // they get to choose their new active Pokemon.
        this.nextPlayerToMakeActive.setEtatCourant(new VerificationPokemonAttaquant(this.nextPlayerToMakeActive));

        // The verifierPokemonKO() call will check if the attacker's active Pokemon is KO.
        // If it is, VerificationPokemonAttaquant should transition to AvancePokemon (for the attacker).
        // If not, it should proceed to end the turn or the attack sequence (e.g. TourNormal).
        this.nextPlayerToMakeActive.getEtatCourant().verifierPokemonKO();
    }

    // Override other methods from AvancePokemon or EtatJoueur if specific behavior is needed
    // while the defender is choosing their active Pokemon. For example, what happens if they
    // try to use an item or attack? AvancePokemon likely restricts this.
}
