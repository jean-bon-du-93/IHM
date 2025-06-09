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
    // private ListChangeListener<ICarte> mainListener; // Replaced by cardCountChangeListener
    // private ListChangeListener<ICarte> deckListener; // Replaced by cardCountChangeListener
    // private ListChangeListener<ICarte> defausseListener; // Replaced by cardCountChangeListener
    // private ListChangeListener<ICarte> prixListener; // Replaced by cardCountChangeListener
    private ListChangeListener<ICarte> cardCountChangeListener; // For main, deck, discard, prizes


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

        // Initialize the shared listener for card counts
        this.cardCountChangeListener = c -> mettreAJourComptesCartesAdversaire();

        // Initial UI setup
        nomAdversaireLabel.setText(adversaire.getNom()); // Assuming getNom() is available and sufficient for now
        placerPokemonActifAdversaire();
        placerBancAdversaire();
        mettreAJourComptesCartesAdversaire();

        // Setup listeners
        setupListeners();
    }

    private void setupListeners() {
        if (this.adversaire == null) return; // NPE Hardening

        // Listener for active Pokémon
        ObjectProperty<? extends IPokemon> pokemonActifProp = adversaire.pokemonActifProperty();
        if (pokemonActifProp != null) {
            pokemonActifListener = (obs, oldVal, newVal) -> placerPokemonActifAdversaire();
            pokemonActifProp.addListener(pokemonActifListener);
        }

        // Listener for bench
        ObservableList<? extends IPokemon> bancList = adversaire.getBanc(); // Assuming getBanc() returns ObservableList
        if (bancList != null) {
            bancListener = change -> placerBancAdversaire();
            bancList.addListener(bancListener);
        }

        // Listeners for card counts using the shared listener
        // Assuming IJoueur has xxxProperty() methods returning ObservableList or ListProperty
        // If IJoueur provides e.g. mainProperty() which is a ReadOnlyListProperty<ICarte>,
        // then .addListener(this.cardCountChangeListener) is correct.
        // If it's just getMain() returning ObservableList, that's also fine.
        // The task implies property methods exist for these.
        // Correcting main to use getMain() as per new instructions
        ObservableList<? extends ICarte> mainList = adversaire.getMain();
        if (mainList != null) {
            mainList.addListener(this.cardCountChangeListener);
        }
        if (adversaire.piocheProperty() != null) { // Changed from getPioche()
            adversaire.piocheProperty().addListener(this.cardCountChangeListener);
        }
        if (adversaire.defausseProperty() != null) { // Changed from getDefausse()
            adversaire.defausseProperty().addListener(this.cardCountChangeListener);
        }
        if (adversaire.recompensesProperty() != null) { // Changed from getCartesRecompense()
            adversaire.recompensesProperty().addListener(this.cardCountChangeListener);
        }
    }

    private void clearBindingsAndListeners() {
        if (adversaire == null) return;

        ObjectProperty<? extends IPokemon> pokemonActifProp = adversaire.pokemonActifProperty();
        if (pokemonActifProp != null && pokemonActifListener != null) {
            pokemonActifProp.removeListener(pokemonActifListener);
        }

        ObservableList<? extends IPokemon> bancList = adversaire.getBanc(); // Assuming getBanc()
        if (bancList != null && bancListener != null) {
            bancList.removeListener(bancListener);
        }

        // Remove the shared card count listener
        if (this.cardCountChangeListener != null) {
            // Correcting main to use getMain()
            ObservableList<? extends ICarte> mainList = adversaire.getMain();
            if (mainList != null) {
                mainList.removeListener(this.cardCountChangeListener);
            }
            if (adversaire.piocheProperty() != null) { // Changed from getPioche()
                adversaire.piocheProperty().removeListener(this.cardCountChangeListener);
            }
            if (adversaire.defausseProperty() != null) { // Changed from getDefausse()
                adversaire.defausseProperty().removeListener(this.cardCountChangeListener);
            }
            if (adversaire.recompensesProperty() != null) { // Changed from getCartesRecompense()
                adversaire.recompensesProperty().removeListener(this.cardCountChangeListener);
            }
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

        // Assuming xxxProperty() returns an ObservableList or a ListProperty that has a size() method or getSize().
        // For ReadOnlyListProperty, it would be .getSize(). For ObservableList, it's .size().
        // The task used .size(), implying the property itself is an ObservableList or SimpleListProperty.
        // Correcting main to use getMain().size()
        mainAdversaireLabel.setText("Main Adv.: " + (adversaire.getMain() != null ? adversaire.getMain().size() : "N/A"));
        deckAdversaireLabel.setText("Deck Adv.: " + (adversaire.piocheProperty() != null ? adversaire.piocheProperty().size() : "N/A"));
        defausseAdversaireLabel.setText("Défausse Adv.: " + (adversaire.defausseProperty() != null ? adversaire.defausseProperty().size() : "N/A"));
        prixAdversaireLabel.setText("Prix Adv.: " + (adversaire.recompensesProperty() != null ? adversaire.recompensesProperty().size() : "N/A"));
    }
}
