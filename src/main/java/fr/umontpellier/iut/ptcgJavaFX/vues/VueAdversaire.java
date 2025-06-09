package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class VueAdversaire extends VBox {

    private IJeu jeu; // Not strictly needed if VueAdversaire only observes a given IJoueur
    private IJoueur adversaire;

    @FXML private Label nomAdversaireLabel;
    @FXML private Label pokemonActifAdversaireLabel;
    @FXML private HBox bancAdversaireHBox;
    @FXML private Label mainAdversaireLabel;
    @FXML private Label deckAdversaireLabel;
    @FXML private Label defausseAdversaireLabel;
    @FXML private Label prixAdversaireLabel;

    // Listeners to update UI when properties of 'adversaire' change
    private ChangeListener<IPokemon> pokemonActifListener;
    private ListChangeListener<IPokemon> bancListener;
    private ListChangeListener<ICarte> mainListener;
    private ListChangeListener<ICarte> deckListener;
    private ListChangeListener<ICarte> defausseListener;
    private ListChangeListener<ICarte> prixListener;


    public VueAdversaire() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vueAdversaire.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setJeu(IJeu jeu) {
        this.jeu = jeu;
        // If VueAdversaire needs to react to game-wide changes that affect the opponent,
        // bindings to 'jeu' would be set up here.
    }

    public void setAdversaire(IJoueur adversaire) {
        if (this.adversaire != null) {
            // Remove old listeners if switching opponent
            clearBindingsAndListeners();
        }
        this.adversaire = adversaire;
        initialiserAffichage();
    }

    private void initialiserAffichage() {
        if (adversaire == null) {
            nomAdversaireLabel.setText("Adversaire non défini");
            pokemonActifAdversaireLabel.setText("N/A");
            bancAdversaireHBox.getChildren().clear();
            mainAdversaireLabel.setText("Main: N/A");
            deckAdversaireLabel.setText("Deck: N/A");
            defausseAdversaireLabel.setText("Défausse: N/A");
            prixAdversaireLabel.setText("Prix: N/A");
            return;
        }

        // Initial UI setup
        nomAdversaireLabel.setText(adversaire.getNom()); // Assuming getNom() is available and sufficient for now
        placerPokemonActifAdversaire();
        placerBancAdversaire();
        mettreAJourComptesCartesAdversaire();

        // Setup listeners
        setupListeners();
    }

    private void setupListeners() {
        if (adversaire == null) return;

        // Listener for active Pokémon
        ObjectProperty<? extends IPokemon> pokemonActifProp = adversaire.pokemonActifProperty();
        if (pokemonActifProp != null) {
            pokemonActifListener = (obs, oldVal, newVal) -> placerPokemonActifAdversaire();
            pokemonActifProp.addListener(pokemonActifListener);
        }

        // Listener for bench
        ObservableList<? extends IPokemon> bancList = adversaire.getBanc();
        if (bancList != null) {
            bancListener = change -> placerBancAdversaire();
            bancList.addListener(bancListener);
        }

        // Listener for hand size
        ObservableList<? extends ICarte> mainList = adversaire.getMain();
        if (mainList != null) {
            mainListener = change -> mettreAJourComptesCartesAdversaire();
            mainList.addListener(mainListener);
        }

        // Listener for deck size (using getPioche as per IJoueur interface)
        ObservableList<? extends ICarte> deckList = adversaire.getPioche();
        if (deckList != null) {
            deckListener = change -> mettreAJourComptesCartesAdversaire();
            deckList.addListener(deckListener);
        }

        // Listener for discard pile size
        ObservableList<? extends ICarte> defausseList = adversaire.getDefausse();
        if (defausseList != null) {
            defausseListener = change -> mettreAJourComptesCartesAdversaire();
            defausseList.addListener(defausseListener);
        }

        // Listener for prize cards (using getCartesRecompense as per IJoueur interface)
        ObservableList<? extends ICarte> prixList = adversaire.getCartesRecompense();
        if (prixList != null) {
            prixListener = change -> mettreAJourComptesCartesAdversaire();
            prixList.addListener(prixListener);
        }
    }

    private void clearBindingsAndListeners() {
        if (adversaire == null) return;

        ObjectProperty<? extends IPokemon> pokemonActifProp = adversaire.pokemonActifProperty();
        if (pokemonActifProp != null && pokemonActifListener != null) {
            pokemonActifProp.removeListener(pokemonActifListener);
        }

        ObservableList<? extends IPokemon> bancList = adversaire.getBanc();
        if (bancList != null && bancListener != null) {
            bancList.removeListener(bancListener);
        }

        ObservableList<? extends ICarte> mainList = adversaire.getMain();
        if (mainList != null && mainListener != null) {
            mainList.removeListener(mainListener);
        }

        ObservableList<? extends ICarte> deckList = adversaire.getPioche();
        if (deckList != null && deckListener != null) {
            deckList.removeListener(deckListener);
        }

        ObservableList<? extends ICarte> defausseList = adversaire.getDefausse();
        if (defausseList != null && defausseListener != null) {
            defausseList.removeListener(defausseListener);
        }

        ObservableList<? extends ICarte> prixList = adversaire.getCartesRecompense();
        if (prixList != null && prixListener != null) {
            prixList.removeListener(prixListener);
        }
    }


    private void placerPokemonActifAdversaire() {
        if (adversaire != null && adversaire.pokemonActifProperty() != null) {
            IPokemon pkmnActif = adversaire.pokemonActifProperty().get();
            if (pkmnActif != null && pkmnActif.getCartePokemon() != null) {
                pokemonActifAdversaireLabel.setText(pkmnActif.getCartePokemon().getNom());
            } else {
                pokemonActifAdversaireLabel.setText("Aucun");
            }
        } else {
            pokemonActifAdversaireLabel.setText("N/A");
        }
    }

    private void placerBancAdversaire() {
        bancAdversaireHBox.getChildren().clear();
        if (adversaire != null && adversaire.getBanc() != null) {
            for (IPokemon pokemon : adversaire.getBanc()) {
                if (pokemon != null && pokemon.getCartePokemon() != null) {
                    // Simple representation for opponent's bench: Label with name or "Dos de Carte"
                    Label pkmnLabel = new Label(pokemon.getCartePokemon().getNom());
                    // Or Label pkmnLabel = new Label("Pokémon"); // To hide info
                    pkmnLabel.setStyle("-fx-border-color: black; -fx-padding: 5px;");
                    bancAdversaireHBox.getChildren().add(pkmnLabel);
                }
            }
        }
    }

    private void mettreAJourComptesCartesAdversaire() {
        if (adversaire == null) return;

        mainAdversaireLabel.setText("Main: " + (adversaire.getMain() != null ? adversaire.getMain().size() : "N/A"));
        deckAdversaireLabel.setText("Deck: " + (adversaire.getPioche() != null ? adversaire.getPioche().size() : "N/A"));
        defausseAdversaireLabel.setText("Défausse: " + (adversaire.getDefausse() != null ? adversaire.getDefausse().size() : "N/A"));
        prixAdversaireLabel.setText("Prix: " + (adversaire.getCartesRecompense() != null ? adversaire.getCartesRecompense().size() : "N/A"));
    }
}
