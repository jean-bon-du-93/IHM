package fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.talent;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Pokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.EtatJoueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.TourNormal; // Or an appropriate next state

import java.util.List;

public class EtatChoixUtiliserEnergyGrounding extends EtatJoueur {
    private Pokemon koPokemon;
    private Pokemon lanturnPokemon;
    private List<Carte> availableEnergies;

    public EtatChoixUtiliserEnergyGrounding(Joueur joueurActif, Pokemon koPokemon, Pokemon lanturnPokemon, List<Carte> availableEnergies) {
        super(joueurActif);
        this.koPokemon = koPokemon;
        this.lanturnPokemon = lanturnPokemon;
        this.availableEnergies = availableEnergies;
        String instruction = String.format("Lanturn's Energy Grounding: Move a basic energy from %s (KO) to Lanturn? (Oui/Non)", koPokemon.getCartePokemon().getNom());
        getJeu().instructionProperty().setValue(instruction);
        // The UI will need to offer Yes/No. Assume Yes calls talentAEteAccepte, No calls talentAEteRefuse.
    }

    @Override
    public void talentAEteAccepte() {
        joueur.setEtatCourant(new EtatChoixEnergiePourEnergyGrounding(joueur, koPokemon, lanturnPokemon, availableEnergies));
    }

    @Override
    public void talentAEteRefuse() {
        // Transition to a state that continues KO processing or ends interaction
        // For now, let's assume it goes back to a generic post-action state or TourNormal if simple.
        // This might need to be more sophisticated, e.g., returning to a specific KO processing state.
        joueur.setEtatCourant(new TourNormal(joueur));
    }

    @Override
    public void passer() { // If player tries to pass, treat as "No"
        talentAEteRefuse();
    }
}
