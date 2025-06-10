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
                System.out.println("[AttenteChoixPokemonPourEnergie] Recherche de cible parmi les Pokémon du joueur " + joueur.getNom() + ":"); // getJoueur() -> joueur
                List<Pokemon> pokemonsDuJoueur = joueur.getListePokemonEnJeu(); // getJoueur() -> joueur
                System.out.println("[AttenteChoixPokemonPourEnergie] Nombre de Pokémon en jeu pour le joueur: " + pokemonsDuJoueur.size());

                for (Pokemon p : pokemonsDuJoueur) {
                    // Utiliser getCartePokemon().getNom() pour l'affichage si p.getNom() n'existe pas ou n'est pas souhaité
                    System.out.println("[AttenteChoixPokemonPourEnergie] Checking Pkmn: " + p.getCartePokemon().getNom() + " (CartePokemon ID: " + p.getCartePokemon().getId() + ")");
                    // newSelection est déjà vérifié non-null à ce stade
                    // p.getCartePokemon() doit aussi être non-null pour que le Pokémon soit valide en jeu
                    if (p.getCartePokemon() != null && newSelection.getId() != null && p.getCartePokemon().getId().equals(newSelection.getId())) {
                        ciblePotentielle = p;
                        System.out.println("[AttenteChoixPokemonPourEnergie] Cible potentielle trouvée (par ID): " + ciblePotentielle.getCartePokemon().getNom()); // .getNom() -> .getCartePokemon().getNom()
                        break;
                    }
                }

                if (ciblePotentielle != null) {
                    System.out.println("[AttenteChoixPokemonPourEnergie] Cible VALIDE trouvée: " + ciblePotentielle.getCartePokemon().getNom() + ". Attachement de " + this.energieAAttacher.getNom()); // .getNom() -> .getCartePokemon().getNom()
                    joueur.retirerCarteMain(this.energieAAttacher); // getJoueur() -> joueur
                    ciblePotentielle.ajouterCarte(this.energieAAttacher);
                    joueur.setAJoueEnergie(); // getJoueur() -> joueur

                    cleanupListener();
                    getJeu().instructionProperty().setValue(this.energieAAttacher.getNom() + " attachée à " + ciblePotentielle.getCartePokemon().getNom() + ". Choisissez une action."); // .getNom() -> .getCartePokemon().getNom()
                    joueur.setEtatCourant(new TourNormal(joueur)); // getJoueur() -> joueur (twice)
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
        getJoueur().setEtatCourant(new TourNormal(getJoueur())); // Corrected: getJoueur()
        getJeu().carteSelectionneeProperty().set(null);
    }

    @Override
    public void passer() {
        annulerAttachement();
        getJoueur().getEtatCourant().passer();
    }

    @Override
    public void carteChoisie(String idAutreCarteEnMain) {
        annulerAttachement();
        getJoueur().getEtatCourant().carteChoisie(idAutreCarteEnMain);
    }

    @Override
    public void attaquer(String nomAttaque) {
        annulerAttachement();
        getJoueur().getEtatCourant().attaquer(nomAttaque);
    }

    @Override
    public void retraiteChoisie() {
        annulerAttachement();
        getJoueur().getEtatCourant().retraiteChoisie();
    }

    @Override
    public void carteSurTerrainCliquee(String idCarte) {
        // La logique principale est dans le listener.
        // Si un clic arrive ici, c'est que la sélection n'a pas changé ou que le listener
        // n'a pas abouti à une action (ex: clic sur une carte non-Pokémon du joueur).
        // Si newSelection dans le listener est la même que oldSelection, le listener ne se redéclenche pas.
        // Dans ce cas, un nouveau clic sur la même carte (qui est déjà sélectionnée) pourrait être interprété différemment,
        // mais actuellement, la sélection est effacée par le listener si la cible n'est pas valide.
        // Donc, un deuxième clic sur une carte invalide ne devrait pas arriver ici si la sélection a été annulée.
        // Cette méthode peut rester vide ou loguer si un comportement inattendu est observé.
         System.out.println("[AttenteChoixPokemonPourEnergie] carteSurTerrainCliquee appelée directement. ID: " + idCarte + ". La logique principale est dans le listener.");
    }
}
