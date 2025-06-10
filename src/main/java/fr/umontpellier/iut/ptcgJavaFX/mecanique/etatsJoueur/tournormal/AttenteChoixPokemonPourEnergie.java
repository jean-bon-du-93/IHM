package fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Pokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.EtatJoueur;
import javafx.beans.value.ChangeListener;

public class AttenteChoixPokemonPourEnergie extends EtatJoueur {

    private Carte energieAAttacher;
    private ChangeListener<Carte> listenerSelectionCarteTerrain;

    public AttenteChoixPokemonPourEnergie(Joueur joueur, Carte energieAAttacher) {
        super(joueur);
        this.energieAAttacher = energieAAttacher;
        getJeu().instructionProperty().setValue("Énergie " + energieAAttacher.getNom() + " : Choisissez un de vos Pokémon pour l'attacher, ou une autre action pour annuler.");

        listenerSelectionCarteTerrain = (obs, oldSelection, newSelection) -> {
            if (newSelection != null) { // Une carte sur le terrain a été sélectionnée
                // Vérifier si newSelection est un Pokémon appartenant au joueur actif
                Pokemon ciblePotentielle = null;
                // Iterate over Pokemon instances, not CartePokemon instances directly from a list of Carte
                for (Pokemon p : getJoueur().getListePokemonEnJeu()) { // Assuming Joueur has getListePokemonEnJeu()
                    if (p.getCartePokemon() == newSelection) { // Compare base CartePokemon objects
                        ciblePotentielle = p;
                        break;
                    }
                }

                if (ciblePotentielle != null) {
                    // Cible valide trouvée
                    getJoueur().retirerCarteMain(this.energieAAttacher); // Retirer de la main
                    ciblePotentielle.ajouterCarte(this.energieAAttacher); // Attacher au Pokémon
                    getJoueur().setAJoueEnergie(); // Marquer que l'énergie a été jouée ce tour

                    // Nettoyage et retour à l'état normal
                    cleanupListener();
                    getJeu().instructionProperty().setValue(this.energieAAttacher.getNom() + " attachée à " + ciblePotentielle.getNom() + ". Choisissez une action.");
                    getJoueur().setEtatCourant(new TourNormal(getJoueur()));
                    getJeu().carteSelectionneeProperty().set(null); // Déselectionner la carte sur le terrain après l'action
                } else {
                    // Clic sur une carte non valide (pas un Pokémon du joueur, ou Pokémon adverse, ou carte non-pokemon)
                    getJeu().instructionProperty().setValue("Cible invalide. Choisissez un de VOS Pokémon pour attacher " + this.energieAAttacher.getNom() + ".");
                    // Important: Déselectionner la carte pour permettre une nouvelle tentative ou une autre action.
                    // Si la carte sélectionnée n'est pas valide, le listener sera rappelé avec newSelection = null
                    // lors de la déselection, ce qui ne posera pas de problème.
                    getJeu().carteSelectionneeProperty().set(null);
                }
            }
            // Si newSelection est null (déselection explicite ou après clic invalide), on ne fait rien ici,
            // le joueur peut alors choisir une autre action ou un autre Pokémon.
        };
        // S'abonner au changement de la carte sélectionnée sur le terrain
        getJeu().carteSelectionneeProperty().addListener(listenerSelectionCarteTerrain);
    }

    private void cleanupListener() {
        if (listenerSelectionCarteTerrain != null) {
            getJeu().carteSelectionneeProperty().removeListener(listenerSelectionCarteTerrain);
            listenerSelectionCarteTerrain = null;
        }
    }

    private void annulerAttachement() {
        cleanupListener();
        // L'énergie reste en main car elle n'a pas été retirée si l'attachement n'a pas eu lieu.
        getJeu().instructionProperty().setValue("Attachement d'énergie annulé. Choisissez une action.");
        getJoueur().setEtatCourant(new TourNormal(getJoueur()));
        getJeu().carteSelectionneeProperty().set(null); // Déselectionner toute carte terrain
    }

    @Override
    public void passer() {
        // Si le joueur passe son tour alors qu'il était en train d'attacher une énergie
        annulerAttachement();
        getJoueur().getEtatCourant().passer(); // Delelguer à l'état TourNormal
    }

    @Override
    public void carteChoisie(String idAutreCarteEnMain) {
        // Si le joueur choisit une autre carte de sa main
        annulerAttachement();
        getJoueur().getEtatCourant().carteChoisie(idAutreCarteEnMain); // Deleguer à l'état TourNormal
    }

    // Surcharger d'autres actions si nécessaire pour annuler (ex: retraiteChoisie, etc.)
    // Par exemple, si on clique sur une attaque, il faut aussi annuler.
    // Pour l'instant, on se limite à passer et choisir une autre carte.
    // Il faudrait idéalement une méthode cancelAction() dans EtatJoueur
    // que toutes les autres actions appelleraient par défaut dans cet état.

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
        // Le listener sur carteSelectionneeProperty s'occupe de la logique principale.
        // Si un clic sur une carte terrain arrive ici et que ce n'est pas géré par le listener
        // (par exemple si la sélection est la même et que le listener ne se redéclenche pas),
        // ou si on veut une logique d'annulation explicite par re-clic sur la même carte énergie en main (non géré ici).
        // Pour l'instant, on ne fait rien de plus ici, le listener est le principal acteur.
        // Si on clique sur une carte qui n'est PAS un Pokémon du joueur, le listener mettra la sélection à null.
        // Si on clique sur un Pokémon VALIDE du joueur, le listener attachera l'énergie.
        // Si on clique sur la même carte Energie en main (pas géré par carteSurTerrainCliquee)
        // -> voir carteChoisie(String idAutreCarteEnMain)
    }
}
