package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte; // Use concrete Carte
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class VueEnergieSelection extends VBox {
    @FXML private Label instructionLabel;
    @FXML private FlowPane energyCardsPane;

    private static final double ENERGY_IMAGE_SIZE = 40;

    public VueEnergieSelection() {
        // FXML will instantiate this
    }

    public void setInstructionText(String text) {
        if (instructionLabel != null) {
            instructionLabel.setText(text);
        }
    }

    // Takes List<Carte> because we need getTypeEnergie()
    public void populateEnergies(List<Carte> energyCards, IJeu jeu) {
        energyCardsPane.getChildren().clear();
        if (energyCards == null) return;

        for (Carte energyCard : energyCards) {
            // getTypeEnergie() is available on concrete Carte
            if (energyCard == null || energyCard.getTypeEnergie() == null) continue;

            ImageView energyImageView = VueUtils.creerImageViewPourIconeEnergie(energyCard.getTypeEnergie(), ENERGY_IMAGE_SIZE);
            energyImageView.setUserData(energyCard.getId());
            energyImageView.getStyleClass().add("clickable-energy"); // For CSS styling if needed

            energyImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                String cardId = (String) ((ImageView) event.getSource()).getUserData();
                if (jeu != null) {
                    jeu.uneCarteEnergieAEteChoisie(cardId);
                    // VueJoueurActif will hide this pane based on subsequent instruction change
                }
            });
            energyCardsPane.getChildren().add(energyImageView);
        }
    }

    public void setPaneVisible(boolean visible) {
        this.setVisible(visible);
        this.setManaged(visible);
    }
}
