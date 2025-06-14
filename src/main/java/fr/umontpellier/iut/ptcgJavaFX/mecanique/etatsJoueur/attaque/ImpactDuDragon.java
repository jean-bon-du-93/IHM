package fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.attaque;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Pokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;

import java.util.List;
import java.util.stream.Collectors;

public class ImpactDuDragon extends EnAttaque {

    private int nbEnergiesADefausser; // Renamed for clarity
    private boolean discardCompleteOuImpossible;

    public ImpactDuDragon(Joueur joueurActif) {
        super(joueurActif);
        Pokemon pokemonActif = joueur.getPokemonActif();
        discardCompleteOuImpossible = false;

        if (pokemonActif == null) {
            nbEnergiesADefausser = 0;
            discardCompleteOuImpossible = true; // Nothing to discard
        } else {
            long energiesAttachees = pokemonActif.getCartes().stream()
                                        .filter(c -> c.getTypeEnergie() != null)
                                        .count();
            nbEnergiesADefausser = (int) Math.min(3, energiesAttachees);
        }

        if (nbEnergiesADefausser == 0) {
            getJeu().instructionProperty().setValue("Impact du Dragon ! Aucune énergie à défausser.");
            discardCompleteOuImpossible = true;
            // Directly calling finAttaque() as per simplified approach.
            // If this causes issues (e.g. state change during superclass constructor),
            // it would need to be deferred (e.g., Platform.runLater or a flag for next action).
            finAttaque();
        } else {
            getJeu().instructionProperty().setValue("Impact du Dragon ! Défaussez " + nbEnergiesADefausser + " énergie" + (nbEnergiesADefausser > 1 ? "s" : "") + " de " + pokemonActif.getCartePokemon().getNom() + ".");
        }
    }

    @Override
    public void defausseEnergie(String numCarte) {
        if (discardCompleteOuImpossible) {
            finAttaque();
            return;
        }

        Pokemon pokemon = joueur.getPokemonActif();
        if (pokemon == null) { // Safeguard
            finAttaque();
            return;
        }

        List<String> choixPossibles = pokemon.getCartes().stream()
                .filter(c -> c.getTypeEnergie() != null)
                .map(Carte::getId)
                .collect(Collectors.toList()); // Use Collectors.toList() for mutable list if needed by remove.

        if (choixPossibles.contains(numCarte)) {
            Carte carteEnergie = Carte.get(numCarte);
            pokemon.retirerCarte(carteEnergie);
            joueur.ajouterCarteDefausse(carteEnergie);
            nbEnergiesADefausser--; // Use the adjusted count

            if (nbEnergiesADefausser <= 0) { // Changed to <= for safety
                discardCompleteOuImpossible = true;
                getJeu().instructionProperty().setValue(pokemon.getCartePokemon().getNom() + " a défaussé les énergies requises.");
                finAttaque();
            } else {
                getJeu().instructionProperty().setValue("Impact du Dragon ! Défaussez encore " + nbEnergiesADefausser + " énergie" + (nbEnergiesADefausser > 1 ? "s" : "") + ".");
            }
        }
        // If invalid card choice, do nothing, wait for valid choice.
    }

    @Override
    public void passer() {
        // Player cannot pass this action.
    }
}

