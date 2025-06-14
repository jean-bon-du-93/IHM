package fr.umontpellier.iut.ptcgJavaFX.vues;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField; // Added import
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe correspond à une nouvelle fenêtre permettant de choisir les noms des joueurs de la partie.
 *
 * Lorsque l'utilisateur a fini de saisir les noms de joueurs, il demandera à démarrer la partie.
 */
public class VueChoixJoueurs extends Stage {

    @FXML
    private TextField joueur1NameField; // Added field
    @FXML
    private TextField joueur2NameField; // Added field

    private final ObservableList<String> nomsJoueurs;

    public VueChoixJoueurs() {
        nomsJoueurs = FXCollections.observableArrayList();
    }

    public ObservableList<String> nomsJoueursProperty() {
        return nomsJoueurs;
    }

    public List<String> getNomsJoueurs() {
        return nomsJoueurs;
    }

    /**
     * Définit l'action à exécuter lorsque la liste des participants est correctement initialisée
     */
    public void setNomsDesJoueursDefinisListener(ListChangeListener<String> quandLesNomsDesJoueursSontDefinis) {
        this.nomsJoueurs.addListener(quandLesNomsDesJoueursSontDefinis);
    }


    /**
     * Vérifie que tous les noms des participants sont renseignés
     * et affecte la liste définitive des participants
     */
    @FXML
    protected void setListeDesNomsDeJoueurs() {
        ArrayList<String> tempNamesList = new ArrayList<>();
        for (int i = 0; i < 2 ; i++) {
            String name = getJoueurParNumero(i);
            if (name == null || name.equals("")) {
                tempNamesList.clear();
                break;
            }
            else
                tempNamesList.add(name);
        }
        if (tempNamesList.size() == 2) {
            hide();
            nomsJoueurs.clear();
            nomsJoueurs.addAll(tempNamesList);
        }
    }

    /**
     * Retourne le nom que l'utilisateur a renseigné pour le ième participant à la partie
     * @param playerNumber : le numéro du participant
     */
    protected String getJoueurParNumero(int playerNumber) {
        if (playerNumber == 0) {
            return joueur1NameField != null ? joueur1NameField.getText() : null;
        } else if (playerNumber == 1) {
            return joueur2NameField != null ? joueur2NameField.getText() : null;
        }
        return null; // Should not happen for a 2-player game setup
    }

}

