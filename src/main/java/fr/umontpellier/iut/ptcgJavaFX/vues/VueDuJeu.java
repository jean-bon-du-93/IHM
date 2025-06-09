package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
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
    private Button boutonPasserVueDuJeu;

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
        creerBindings(); // Now call bindings for VueDuJeu itself
    }

    @FXML
    private void actionPasserVueDuJeu(ActionEvent event) {
        if (this.jeu != null) {
            this.jeu.passerAEteChoisi(); // Or a different action if this button is for something else
            System.out.println("Bouton Passer de VueDuJeu cliqué"); // For differentiation
        }
    }

    public void creerBindings() {
        if (jeu != null && jeu.instructionProperty() != null && instructionLabel != null) {
            instructionLabel.textProperty().bind(jeu.instructionProperty());
        }
        // The binding for panneauDuJoueurActif related to joueurActifProperty is handled by its own postInit now.
    }
}