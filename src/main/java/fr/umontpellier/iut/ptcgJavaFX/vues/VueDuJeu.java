package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur; // Ajouté pour le casting de type / utilisation
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
// import javafx.scene.control.Button; // Plus utilisé
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class VueDuJeu extends VBox {

    private IJeu jeuEnCours; // Garder pour passer à VueJoueurActif
    @FXML
    Label instructionLabel;
    @FXML
    VueJoueurActif panneauDuJoueurActif;
    @FXML
    VueAdversaire vueAdversaire;

    public VueDuJeu(IJeu jeu) {
        this.jeuEnCours = jeu;

        FXMLLoader chargeurFXML = new FXMLLoader(getClass().getResource("/fxml/vueDuJeu.fxml"));
        chargeurFXML.setRoot(this);
        chargeurFXML.setController(this);
        try {
            chargeurFXML.load();
        } catch (IOException e) { // Nom d'exception standard, ou exceptionIO
            throw new RuntimeException(e);
        }
        // creerBindings(); // Déplacé vers initialize()
    }

    @FXML
    private void initialize() {
        if (panneauDuJoueurActif != null) {
            panneauDuJoueurActif.setJeu(this.jeuEnCours); // Passer la référence du jeu
            panneauDuJoueurActif.postInit();      // Initialiser les liaisons et observateurs de VueJoueurActif
        }

        if (vueAdversaire != null) {
            vueAdversaire.setJeu(this.jeuEnCours); // Passer la référence du jeu
            IJoueur joueurActifActuel = this.jeuEnCours.joueurActifProperty().get();
            vueAdversaire.setAdversaire(trouverJoueurAdverse(joueurActifActuel));
        }

        creerBindings(); // Appeler maintenant les liaisons pour VueDuJeu elle-même

        // Observateur pour les changements de joueur actif afin de mettre à jour la vue de l'adversaire
        if (this.jeuEnCours != null && this.jeuEnCours.joueurActifProperty() != null) {
            this.jeuEnCours.joueurActifProperty().addListener((obs, ancienJoueur, nouveauJoueur) -> { // obs, oldJoueur, newJoueur sont des noms de paramètres standards pour les listeners
                if (vueAdversaire != null) {
                    vueAdversaire.setAdversaire(trouverJoueurAdverse(nouveauJoueur));
                }
            });
        }
    }

    private IJoueur trouverJoueurAdverse(IJoueur joueurActif) {
        if (this.jeuEnCours.getJoueurs() == null) {
            return null;
        }
        for (IJoueur joueur : this.jeuEnCours.getJoueurs()) {
            if (joueur != joueurActif) {
                return joueur;
            }
        }
        return null; // Au cas où aucun autre joueur n'est trouvé (ne devrait pas arriver dans un jeu à 2 joueurs)
    }

    public void creerBindings() {
        if (jeuEnCours != null && jeuEnCours.instructionProperty() != null && instructionLabel != null) {
            instructionLabel.textProperty().bind(jeuEnCours.instructionProperty());
        }
        // La liaison pour panneauDuJoueurActif relative à joueurActifProperty est maintenant gérée par son propre postInit.
    }
}