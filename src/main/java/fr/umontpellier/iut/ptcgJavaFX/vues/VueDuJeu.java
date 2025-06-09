package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class VueDuJeu extends VBox {

    private IJeu jeu;
    private Label instruction;

    private VueJoueurActif panneauDuJoueurActif;

    public VueDuJeu(IJeu jeu) {
        this.jeu = jeu;
        this.instruction = new Label();
        this.panneauDuJoueurActif = new VueJoueurActif(this.jeu);

        getChildren().addAll(instruction, panneauDuJoueurActif);
        this.setSpacing(15);

        creerBindings();
    }

    public void creerBindings() {
        if (jeu != null && jeu.instructionProperty() != null) {
            instruction.textProperty().bind(jeu.instructionProperty());
        }

        if (panneauDuJoueurActif != null) {
            panneauDuJoueurActif.lierAuJoueurActifDuJeu();
        }
    }
}