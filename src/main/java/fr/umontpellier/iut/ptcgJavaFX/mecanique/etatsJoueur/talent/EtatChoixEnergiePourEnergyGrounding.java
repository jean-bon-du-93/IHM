package fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.talent;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Pokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.EtatJoueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.TourNormal; // Or an appropriate next state

import java.util.List;
import java.util.stream.Collectors;

public class EtatChoixEnergiePourEnergyGrounding extends EtatJoueur {
    private Pokemon koPokemon;
    private Pokemon lanturnPokemon;
    private List<Carte> availableEnergies;

    public EtatChoixEnergiePourEnergyGrounding(Joueur joueurActif, Pokemon koPokemon, Pokemon lanturnPokemon, List<Carte> availableEnergies) {
        super(joueurActif);
        this.koPokemon = koPokemon;
        this.lanturnPokemon = lanturnPokemon;
        // Filter again to be sure, though previous state should pass correct list
        this.availableEnergies = availableEnergies.stream().filter(Carte::isBasicEnergy).collect(Collectors.toList());

        String instruction = String.format("Select a basic energy from %s (KO) to move to Lanturn.", koPokemon.getCartePokemon().getNom());
        getJeu().instructionProperty().setValue(instruction);
        // The UI needs to display these availableEnergies as clickable choices.
        // Clicking one should call an appropriate method on IJeu (e.g. uneCarteEnergieAEteChoisie or uneCarteComplementaireAEteChoisie)
        // which then calls this state's corresponding handler method.
        joueur.setListChoixComplementaires(this.availableEnergies); // To inform UI
    }

    // This method will be called by Jeu when an energy is selected by the player via the UI.
    // It could be carteChoisie, defausseEnergie, or a new specific method.
    // Based on the prompt, we'll use carteChoisie as it's more generic for selecting a card from a list.
    // The UI should trigger jeu.uneCarteComplementaireAEteChoisie(cardId)
    @Override
    public void carteChoisie(String cardId) {
        Carte chosenEnergy = null;
        for (Carte c : availableEnergies) {
            if (c.getId().equals(cardId)) {
                chosenEnergy = c;
                break;
            }
        }

        if (chosenEnergy != null) {
            // IMPORTANT: Modify koPokemon's list of cards *before* it's fully processed for discard.
            boolean removed = koPokemon.retirerCarte(chosenEnergy);
            if(removed) {
                lanturnPokemon.ajouterCarte(chosenEnergy); // Assumes ajouterCarte handles attaching energy
                // TODO: Add logic for "once per turn" flag for Energy Grounding if implementing
                // joueur.setAUtiliseEnergyGroundingCeTour(true);
            } // If not removed, something is wrong, but proceed for now.
        }
        joueur.viderListChoixComplementaires();
        // Transition to a state that continues KO processing or ends interaction.
        joueur.setEtatCourant(new TourNormal(joueur));
    }

    @Override
    public void passer() { // Cannot pass this choice
        getJeu().instructionProperty().setValue("You must select an energy. If you wish to cancel, that should be handled before this state.");
        // Or, alternatively, treat pass as cancelling the talent effect and proceeding with normal KO
        // joueur.setEtatCourant(new TourNormal(joueur));
    }
}
