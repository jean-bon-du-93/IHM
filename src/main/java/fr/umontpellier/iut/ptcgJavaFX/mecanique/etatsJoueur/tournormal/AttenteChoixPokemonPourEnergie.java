package fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Pokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.EtatJoueur;
import javafx.beans.value.ChangeListener;
import java.util.List; // Required for List<Pokemon>

public class AttenteChoixPokemonPourEnergie extends EtatJoueur {

    private Carte energieAAttacher;
    private ChangeListener<Carte> listenerSelectionCarteTerrain;

    public AttenteChoixPokemonPourEnergie(Joueur joueur, Carte energieAAttacher) {
        super(joueur);
        this.energieAAttacher = energieAAttacher;
        System.out.println("[AttenteChoixPokemonPourEnergie] Etat créé. Energie: " + energieAAttacher.getNom() + " (" + energieAAttacher.getId() + ")");
        getJeu().instructionProperty().setValue("Énergie " + energieAAttacher.getNom() + " : Choisissez un de vos Pokémon pour l'attacher, ou une autre action pour annuler.");

        listenerSelectionCarteTerrain = (obs, oldSelection, newSelection) -> {
            System.out.println("[AttenteChoixPokemonPourEnergie] Listener carteSelectionneeProperty. Old: " +
                (oldSelection == null ? "null" : oldSelection.getNom() + " ID " + oldSelection.getId()) +
                ", New: " + (newSelection == null ? "null" : newSelection.getNom() + " ID " + newSelection.getId()));

            if (newSelection != null) { // Une carte sur le terrain a été sélectionnée
                Pokemon ciblePotentielle = null;
                System.out.println("[AttenteChoixPokemonPourEnergie] Recherche de cible parmi les Pokémon du joueur " + this.joueur.getNom() + ":");
                List<Pokemon> pokemonsDuJoueur = this.joueur.getListePokemonEnJeu();
                System.out.println("[AttenteChoixPokemonPourEnergie] Nombre de Pokémon en jeu pour le joueur: " + pokemonsDuJoueur.size());

                for (Pokemon p : pokemonsDuJoueur) {
                    System.out.println("[AttenteChoixPokemonPourEnergie] Checking Pkmn: " + p.getCartePokemon().getNom() + " (CartePokemon ID: " + p.getCartePokemon().getId() + ")");
                    if (p.getCartePokemon() != null && newSelection.getId() != null && p.getCartePokemon().getId().equals(newSelection.getId())) {
                        ciblePotentielle = p;
                        System.out.println("[AttenteChoixPokemonPourEnergie] Cible potentielle trouvée (par ID): " + ciblePotentielle.getCartePokemon().getNom());
                        break;
                    }
                }

                if (ciblePotentielle != null) {
                    System.out.println("[AttenteChoixPokemonPourEnergie] Cible VALIDE trouvée: " + ciblePotentielle.getCartePokemon().getNom() + ". Attachement de " + this.energieAAttacher.getNom());
                    this.joueur.retirerCarteMain(this.energieAAttacher);
                    ciblePotentielle.ajouterCarte(this.energieAAttacher);
                    this.joueur.setAJoueEnergie();

                    cleanupListener();
                    getJeu().instructionProperty().setValue(this.energieAAttacher.getNom() + " attachée à " + ciblePotentielle.getCartePokemon().getNom() + ". Choisissez une action.");
                    this.joueur.setEtatCourant(new TourNormal(this.joueur));
                    getJeu().carteSelectionneeProperty().set(null);
                } else {
                    System.out.println("[AttenteChoixPokemonPourEnergie] Cible INVALIDE. newSelection: " + (newSelection != null ? newSelection.getNom() + " ID " + newSelection.getId() : "null"));
                    getJeu().instructionProperty().setValue("Cible invalide. Choisissez un de VOS Pokémon pour attacher " + this.energieAAttacher.getNom() + ".");
                    getJeu().carteSelectionneeProperty().set(null);
                }
            } else {
                System.out.println("[AttenteChoixPokemonPourEnergie] newSelection est null, le listener ne fait rien.");
            }
        };
        getJeu().carteSelectionneeProperty().addListener(listenerSelectionCarteTerrain);
        System.out.println("[AttenteChoixPokemonPourEnergie] Listener ajouté à carteSelectionneeProperty.");
    }

    private void cleanupListener() {
        if (listenerSelectionCarteTerrain != null) {
            getJeu().carteSelectionneeProperty().removeListener(listenerSelectionCarteTerrain);
            listenerSelectionCarteTerrain = null;
            System.out.println("[AttenteChoixPokemonPourEnergie] Listener retiré de carteSelectionneeProperty.");
        }
    }

    private void annulerAttachement() {
        System.out.println("[AttenteChoixPokemonPourEnergie] Annulation de l'attachement pour " + energieAAttacher.getNom());
        cleanupListener();
        getJeu().instructionProperty().setValue("Attachement d'énergie annulé. Choisissez une action.");
        joueur.setEtatCourant(new TourNormal(joueur)); // Changed getJoueur() to joueur
        getJeu().carteSelectionneeProperty().set(null);
    }

    @Override
    public void passer() {
        annulerAttachement();
        joueur.getEtatCourant().passer();
    }

    @Override
    public void carteChoisie(String idAutreCarteEnMain) {
        annulerAttachement();
        joueur.getEtatCourant().carteChoisie(idAutreCarteEnMain);
    }

    // attaquer(String) a été retirée. EtatJoueur a attaquer() sans paramètre.

    @Override
    public void retraiteChoisie() {
        annulerAttachement();
        joueur.getEtatCourant().retraiteChoisie();
    }

    // @Override // carteSurTerrainCliquee n'est pas dans EtatJoueur, donc @Override est incorrect
    public void carteSurTerrainCliquee(String idCarte) { // Removed @Override
         System.out.println("[AttenteChoixPokemonPourEnergie] carteSurTerrainCliquee appelée directement. ID: " + idCarte + ". La logique principale est dans le listener.");
    }
}
