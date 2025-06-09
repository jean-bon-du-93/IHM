package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur; // Added for type casting/usage
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
// import javafx.scene.control.Button; // No longer used
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class VueDuJeu extends VBox {

    private IJeu jeu; // Keep this to pass to VueJoueurActif
    @FXML
    private Label instructionLabel;
    @FXML
    private VueJoueurActif panneauDuJoueurActif;
    @FXML
    private VueAdversaire vueAdversaire;

    public VueDuJeu(IJeu jeu) {
        this.jeu = jeu;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vueDuJeu.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        // creerBindings(); // Moved to initialize()
    }

    @FXML
    private void initialize() {
        if (panneauDuJoueurActif != null) {
            panneauDuJoueurActif.setJeu(this.jeu); // Pass game reference
            panneauDuJoueurActif.postInit();      // Initialize VueJoueurActif's bindings & listeners
        }

        if (vueAdversaire != null) {
            vueAdversaire.setJeu(this.jeu); // Pass game reference
            IJoueur joueurCourant = this.jeu.joueurActifProperty().get();
            IJoueur adversaireJ = null;
            if (this.jeu.getJoueurs() != null) { // Check if getJoueurs() is null
                for (IJoueur j : this.jeu.getJoueurs()) {
                    if (j != joueurCourant) {
                        adversaireJ = j;
                        break;
                    }
                }
            }
            vueAdversaire.setAdversaire(adversaireJ);
        }

        creerBindings(); // Now call bindings for VueDuJeu itself

        // Listener for active player changes to update opponent view
        if (this.jeu != null && this.jeu.joueurActifProperty() != null) {
            this.jeu.joueurActifProperty().addListener((obs, oldJoueur, newJoueur) -> {
                if (vueAdversaire != null) {
                    IJoueur adversaireUpdated = null;
                    if (this.jeu.getJoueurs() != null) { // Check if getJoueurs() is null
                        for (IJoueur j : this.jeu.getJoueurs()) {
                            if (j != newJoueur) {
                                adversaireUpdated = j;
                                break;
                            }
                        }
                    }
                    vueAdversaire.setAdversaire(adversaireUpdated);
                }
            });
        }
    }

    public void creerBindings() {
        if (jeu != null && jeu.instructionProperty() != null && instructionLabel != null) {
            instructionLabel.textProperty().bind(jeu.instructionProperty());
        }
        // The binding for panneauDuJoueurActif related to joueurActifProperty is handled by its own postInit now.
    }
}