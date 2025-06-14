package fr.umontpellier.iut.ptcgJavaFX.mecanique;

import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJeu.EtatJeu;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJeu.InitialisationJoueurs;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.VerificationPokemonAdversaire;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte; // Added import
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty; // Added import (already present, but good to note)


public class Jeu implements IJeu {
    /**
     * Les joueurs du jeu
     */
    private final Joueur[] joueurs;
    /**
     * Qui doit réaliser un choix
     */
    private final ObjectProperty<Joueur> joueurActif;
    /**
     * Instruction à afficher au joueur actif
     */
    private final ObjectProperty<String> instruction;

    private Carte carteSelectionnee; // Added field
    private final ObjectProperty<Carte> carteSelectionneeProperty = new SimpleObjectProperty<>(null); // Added field

    /**
     * Permet de savoir si le jeu est en initialisation ou si la partie a commencé
     */
    private boolean finInitialisation;
    /**
     * Compteur de tours de jeu, incrémenté de 1 avant chaque tour d'un joueur.
     * <p>
     * Le premier tour du premier joueur est le tour 1, le premier tour du second
     * joueur est le tour 2, etc.
     */
    private int compteurTour = 0;
    /**
     * Permet de savoir si la partie est terminée
     */
    private final BooleanProperty finDePartie;

    public Jeu(Joueur joueur1, Joueur joueur2) {
        instruction = new SimpleObjectProperty<>("");
        this.joueurs = new Joueur[] { joueur1, joueur2 };
        joueur1.setJeu(this);
        joueur2.setJeu(this);
        joueurActif = new SimpleObjectProperty<>();
        finDePartie = new SimpleBooleanProperty(false);
        finInitialisation = false;
    }

    /**
     * Renvoie le numéro du tour de jeu actuel
     */
    public int getCompteurTour() {
        return compteurTour;
    }

    /**
     * Renvoie l'adversaire du joueur passé en argument
     *
     * @param joueur un des deux joueurs du jeu
     */
    public Joueur getAdversaire(Joueur joueur) {
        for (Joueur j : joueurs) {
            if (j != joueur) {
                return j;
            }
        }
        return null;
    }

    /**
     * Exécute un tirage à pile ou face
     *
     * @return {@code true} si le tirage est face, {@code false} sinon
     */
    public boolean lancerPiece() {
        return Math.random() < 0.5;
    }

    /**
     * Démarre le jeu pour jouer une partie
     */
    public void run() {
        joueurActif.setValue(joueurs[0]);
        etatCourantDuJeu = new InitialisationJoueurs(this);
    }

    /**
     * Exécute la phase de contrôle pokémon, pendant laquelle les états spéciaux
     * sont résolus, ainsi que les effets qui se déroulent entre deux tours de jeu.
     */
    public void controlePokemon() {
        for (Joueur joueur : joueurs) {
            joueur.controlePokemon();
        }
    }

    /**
     * Vérifie les points de vie de chaque pokémon de chaque joueur pour les
     * défausser s'ils sont KO et remplacer les pokémon actifs KO.
     * <p>
     * Devrait être appelée après chaque action qui pourrait avoir rendu un pokémon
     * KO : attaque, résolution des effets (poison, brulé), activation de talent qui
     * peut faire des dégâts, etc.
     * <p>
     * Remarque : d'après les règles, si des pokémon sont KO chez chacun des
     * joueurs, c'est le prochain joueur qui jouera qui doit faire la vérification
     * en premier, et choisir son nouveau pokémon actif en premier.
     */
    public void verifierPokemonKO() {
        joueurActif.getValue().setEtatCourant(new VerificationPokemonAdversaire(joueurActif.getValue()));
        joueurActif.getValue().getEtatCourant().verifierPokemonKO();
    }

    /**
     * Teste si la partie est terminée.
     * <p>
     * Les conditions de victoire possibles sont :
     * — l'adversaire n'a plus de pokémon en jeu
     * — toutes les cartes récompense ont été prises
     * — l'adversaire n'a plus de carte dans son deck au début de son tour
     *
     * @return {@code true} si la partie est terminée, {@code false} sinon
     */
    public boolean estTermine() {
        for (Joueur joueur : joueurs) {
            if (joueur.aGagne() || joueur.aPerdu()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Positionne la propriété pour signaler
     * que c'est la fin de la partie
     */
    public void setFinDePartie() {
        finDePartie.setValue(true);
    }

    /**
     * @return le gagnant de la partie, ou {@code null} si la partie n'est pas
     *         terminée ou si les joueurs ne peuvent pas être départagés (partie
     *         nulle).
     */
    public Joueur getGagnant() {
        int score1 = (joueurs[0].aGagne() ? 1 : 0) + (joueurs[0].aPerdu() ? -1 : 0);
        int score2 = (joueurs[1].aGagne() ? 1 : 0) + (joueurs[1].aPerdu() ? -1 : 0);
        if (score1 == 1 && score2 == 1) {
            return null;
        }
        return score1 > score2 ? joueurs[0] : joueurs[1];
    }

    // La suite pour l'IHM avec JavaFX

    /**
     * Gestion des phases (états) du jeu
     */
    private EtatJeu etatCourantDuJeu;

    public void setEtatCourant(EtatJeu etatCourantDuJeu) {
        this.etatCourantDuJeu = etatCourantDuJeu;
    }

    public EtatJeu getEtatCourant() {
        return etatCourantDuJeu;
    }

    public Joueur getJoueurActif() {
        return joueurActif.getValue();
    }

    public void passeAuJoueurSuivant() {
        compteurTour += 1;
        joueurActif.setValue(joueurs[compteurTour % 2]);
    }

    public void initialiserJoueurSuivant() {
        passeAuJoueurSuivant();
        joueurActif.getValue().initialiserPokemons();
        if (compteurTour == 1) finInitialisation = true;
    }

    public boolean initialisationTerminee() {
        return finInitialisation;
    }

    @Override
    public IJoueur[] getJoueurs() {
        return joueurs;
    }

    @Override
    public String getNomDuGagnant() {
        Joueur gagnant = getGagnant();
        if (gagnant == null)
            return "Pas de gagnant";
        else return gagnant.getNom();
    }

    /**
     * Propriétés exportées à utiliser dans l'IHM
     */
    @Override
    public ObjectProperty<String> instructionProperty() {
        return instruction;
    }

    @Override
    public ObjectProperty<Joueur> joueurActifProperty() {
        return joueurActif;
    }

    @Override
    public BooleanProperty finDePartieProperty() {
        return finDePartie;
    }

    /**
     * Gestionnaires des demandes du joueur
     */
    @Override
    public void passerAEteChoisi() {
        Joueur leJoueur = joueurActif.getValue();
        leJoueur.getEtatCourant().passer();
    }

    @Override
    public void uneCarteDeLaMainAEteChoisie(String numPokemon) {
        Joueur leJoueur = joueurActif.getValue();
        leJoueur.getEtatCourant().carteChoisie(numPokemon);
    }

    @Override
    public void unEmplacementVideDuBancAEteChoisi(String indiceBanc) {
        Joueur leJoueur = joueurActif.getValue();
        leJoueur.getEtatCourant().bancChoisi(indiceBanc);
    }

    @Override
    public void uneAttaqueAEteChoisie(String attaque) {
        Joueur leJoueur = joueurActif.getValue();
        leJoueur.attaquer(attaque);
    }

    @Override
    public void retraiteAEteChoisie() {
        Joueur leJoueur = joueurActif.getValue();
        leJoueur.getEtatCourant().retraiteChoisie();
    }

    @Override
    public void melangerAEteChoisi() {
        Joueur leJoueur = joueurActif.getValue();
        leJoueur.getEtatCourant().melangerAEteChoisi();
    }

    @Override
    public void ajouterAEteChoisi() {
        Joueur leJoueur = joueurActif.getValue();
        leJoueur.getEtatCourant().ajouterAEteChoisi();
    }

    @Override
    public void defausserEnergieAEteChoisi() {
        Joueur leJoueur = joueurActif.getValue();
        leJoueur.getEtatCourant().defausserEnergieAEteChoisi();
    }

    @Override
    public void defausserEnergieNAPasEteChoisi() {
        Joueur leJoueur = joueurActif.getValue();
        leJoueur.getEtatCourant().defausserEnergieNAPasEteChoisi();
    }

    @Override
    public void uneCarteEnergieAEteChoisie(String carteEnergie) {
        Joueur leJoueur = joueurActif.getValue();
        leJoueur.getEtatCourant().defausseEnergie(carteEnergie);
    }

    @Override
    public void uneCarteComplementaireAEteChoisie(String cartecomplementaire) {
        Joueur leJoueur = joueurActif.getValue();
        leJoueur.getEtatCourant().carteChoisie(cartecomplementaire);
    }

    public ObjectProperty<Carte> carteSelectionneeProperty() { // Added getter
        return carteSelectionneeProperty;
    }

    public Carte getCarteSelectionnee() { // Added getter
        return carteSelectionnee;
    }

    @Override
    public void carteSurTerrainCliquee(String idCarte) { // Added method implementation
        Carte carteTrouvee = null;
        Pokemon pokemonTrouve = null; // Utile si on veut stocker le Pokemon

        for (Joueur joueur : joueurs) {
            // Pokémon actif
            Pokemon actif = joueur.getPokemonActif();
            if (actif != null && actif.getCartePokemon() != null && actif.getCartePokemon().getId().equals(idCarte)) {
                carteTrouvee = actif.getCartePokemon();
                pokemonTrouve = actif;
                break;
            }
            // Pokémon sur le banc
            for (Pokemon pokemonBanc : joueur.getListePokemonDeBanc()) {
                if (pokemonBanc.getCartePokemon() != null && pokemonBanc.getCartePokemon().getId().equals(idCarte)) {
                    carteTrouvee = pokemonBanc.getCartePokemon();
                    pokemonTrouve = pokemonBanc;
                    break;
                }
            }
            if (carteTrouvee != null) {
                break;
            }
        }

        if (carteTrouvee != null) {
            // Pour l'instant, stockons la CartePokemon.
            // Si on voulait stocker l'instance IPokemon, il faudrait changer le type de carteSelectionneeProperty.
            if (this.carteSelectionnee == carteTrouvee) {
                this.carteSelectionnee = null;
                this.carteSelectionneeProperty.set(null);
                System.out.println("Carte déselectionnée sur le terrain: " + idCarte);
            } else {
                this.carteSelectionnee = carteTrouvee;
                this.carteSelectionneeProperty.set(carteTrouvee);
                System.out.println("Carte sélectionnée sur le terrain: " + idCarte + " (" + carteTrouvee.getNom() + ")");
            }
        } else {
            // Si aucune carte n'est trouvée (par exemple clic "à côté"), on pourrait déselectionner la carte actuelle.
            // Pour l'instant, on ne fait rien si la carte n'est pas trouvée pour éviter une déselection non désirée.
            System.err.println("Carte cliquée avec ID " + idCarte + " non trouvée sur le terrain.");
        }
    }

    @Override
    public void talentAEteAccepte() {
        getJoueurActif().getEtatCourant().talentAEteAccepte();
    }

    @Override
    public void talentAEteRefuse() {
        getJoueurActif().getEtatCourant().talentAEteRefuse();
    }
}
