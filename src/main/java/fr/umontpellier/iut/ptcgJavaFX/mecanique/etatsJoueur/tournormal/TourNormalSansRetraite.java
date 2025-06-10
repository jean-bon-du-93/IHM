package fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Pokemon; // Added import
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.EtatJoueur;
// Added import for the new state
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.AttenteChoixPokemonPourEnergie;

import java.util.List;

public class TourNormalSansRetraite extends EtatJoueur {

    public TourNormalSansRetraite(Joueur joueur) {
        super(joueur);
        getJeu().instructionProperty().setValue("Choisissez une action ou passez");
    }

    @Override
    public void passer() {
        getJeu().controlePokemon();
        joueur.onFinTour();
        if (!getJeu().estTermine())
            passerAuJoueurSuivant();
        else
            joueur.setEtatCourant(new FinPartie(joueur));
    }

    @Override
    public void carteChoisie(String numCarte) {
        Carte carte = Carte.get(numCarte);
        if (carte == null) {
            System.err.println("Carte avec ID " + numCarte + " non trouvée.");
            return;
        }

        // Vérifier si c'est une carte Énergie jouable
        if (carte.getTypeEnergie() != null && getJoueur().peutJouerEnergie()) {
            getJoueur().setEtatCourant(new AttenteChoixPokemonPourEnergie(getJoueur(), carte));
            // L'instruction est mise à jour par le constructeur de AttenteChoixPokemonPourEnergie
        } else if (carte.getTypeEnergie() != null && !getJoueur().peutJouerEnergie()) {
            getJeu().instructionProperty().setValue("Vous avez déjà joué une carte Énergie ce tour.");
            // Reste dans l'état TourNormal (ou TourNormalSansRetraite)
        }
        else { // Logique pour les autres types de cartes (Pokémon, Dresseur, Talent)
            List<String> choixPossibles = getJoueur().getCartesEnMainJouables(); // S'assurer que cette liste est à jour
            if (!choixPossibles.isEmpty() && choixPossibles.contains(numCarte)) {
                List<String> pokemonsEnJeuAvecTalent = getJoueur().getListePokemonEnJeu().stream()
                        .filter(Pokemon::peutUtiliserTalent)
                        .map(Pokemon::getCartePokemon)
                        .map(Carte::getId)
                        .toList();
                if (pokemonsEnJeuAvecTalent.contains(numCarte)) {
                    // Assurez-vous que getPokemon(Carte) existe et fonctionne comme prévu
                    Pokemon pokemonAvecTalent = getJoueur().getPokemon(carte);
                    if (pokemonAvecTalent != null) {
                        pokemonAvecTalent.utiliserTalent(getJoueur());
                    } else {
                        // Fallback or error, should not happen if pokemonsEnJeuAvecTalent is correct
                        getJoueur().jouerCarteEnMain(numCarte);
                    }
                } else {
                    getJoueur().jouerCarteEnMain(numCarte);
                }
            } else {
                // Si la carte n'est pas jouable (par ex. énergie alors qu'on ne peut plus, ou autre carte non listée)
                getJeu().instructionProperty().setValue("Cette carte ne peut pas être jouée actuellement.");
            }
        }
    }

    public void passerAuJoueurSuivant() {
        getJeu().passeAuJoueurSuivant();
        joueur = getJeu().getJoueurActif();
        passerALEtatSuivant();
        joueur.jouerTour();
    }

    public void passerALEtatSuivant() {
        joueur.setEtatCourant(new TourNormalSansRetraite(joueur));
    }

}
