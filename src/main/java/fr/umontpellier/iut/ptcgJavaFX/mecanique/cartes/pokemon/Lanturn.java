package fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon;

import java.util.List;
import java.util.stream.Collectors;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Pokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte; // Ensure this is imported
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.attaque.FrapEclair;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.talent.EtatChoixUtiliserEnergyGrounding;

public class Lanturn extends CartePokemonEvolution {

    private boolean talentEnergyGroundingUtiliseCeTour = false;

    public Lanturn() {
        super(
                "Lanturn",
                "CES050",
                110,
                Type.ELECTRIQUE,
                Type.COMBAT,
                Type.METAL,
                2,
                "Loupio",
                1);

        ajouterAttaque(new Attaque("Frap'Éclair", this, Type.ELECTRIQUE, 2, Type.INCOLORE, 1) {
            @Override
            public void attaquer(Joueur joueur) {
                joueur.setEtatCourant(new FrapEclair(joueur));
            }
        });
    }

    // Removed old onPokemonKO method

    @Override
    public boolean peutUtiliserTalent() {
        // Simplified check for UI hint. Full check is in utiliserTalent.
        return !talentEnergyGroundingUtiliseCeTour;
    }

    @Override
    public void utiliserTalent(Joueur joueur) {
        if (talentEnergyGroundingUtiliseCeTour) {
            if (joueur.getJeu() != null) {
                joueur.getJeu().instructionProperty().setValue("Energy Grounding a déjà été utilisé ce tour par ce Lanturn.");
            }
            return;
        }

        Pokemon koPokemonAllie = joueur.getPokemonAllieKOAuTourPrecedent();
        if (koPokemonAllie == null) {
            if (joueur.getJeu() != null) {
                joueur.getJeu().instructionProperty().setValue("Aucun Pokémon allié n'a été KO par une attaque au tour précédent.");
            }
            return;
        }

        // Check if KO happened on opponent's last turn.
        // joueur.getTourPokemonAllieKO() is the turn number the KO occurred.
        // joueur.getJeu().getCompteurTour() is the current turn number.
        // Talent is used on player's current turn. So current turn must be KO_turn + 1.
        if (joueur.getJeu() == null || joueur.getJeu().getCompteurTour() != joueur.getTourPokemonAllieKO() + 1) {
            if (joueur.getJeu() != null) {
                joueur.getJeu().instructionProperty().setValue("Energy Grounding ne peut être utilisé que le tour suivant le KO.");
            }
            return;
        }

        List<Carte> basicEnergiesOnKOPokemon = koPokemonAllie.getCartes().stream()
            .filter(Carte::isBasicEnergy) // Relies on Carte.isBasicEnergy()
            .collect(Collectors.toList());

        if (basicEnergiesOnKOPokemon.isEmpty()) {
            if (joueur.getJeu() != null && koPokemonAllie.getCartePokemon() != null) {
                joueur.getJeu().instructionProperty().setValue(koPokemonAllie.getCartePokemon().getNom() + " (KO) n'avait pas d'énergie de base attachée.");
            } else if (joueur.getJeu() != null) {
                 joueur.getJeu().instructionProperty().setValue("Le Pokémon KO n'avait pas d'énergie de base attachée.");
            }
            return;
        }

        Pokemon thisLanturnInPlay = joueur.getPokemon(this); // 'this' refers to this CartePokemon instance
        if (thisLanturnInPlay == null || thisLanturnInPlay.estKO()) {
             if (joueur.getJeu() != null) {
                joueur.getJeu().instructionProperty().setValue("Lanturn doit être en jeu et non KO pour utiliser son talent.");
            }
            return;
        }

        // All conditions met, proceed with talent
        this.talentEnergyGroundingUtiliseCeTour = true;
        joueur.setEtatCourant(
            new EtatChoixUtiliserEnergyGrounding(joueur, koPokemonAllie, thisLanturnInPlay, basicEnergiesOnKOPokemon)
        );
    }

    @Override
    public void onFinTour(Joueur joueur) {
        super.onFinTour(joueur); // Handles peutEvoluer = true; and other base class onFinTour logic
        this.talentEnergyGroundingUtiliseCeTour = false;
    }
}
