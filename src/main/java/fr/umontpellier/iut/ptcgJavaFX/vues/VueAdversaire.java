package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap; // For energy display
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos; // For alignment
import javafx.scene.Node; // For creerPokemonBancNode return type
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region; // For placeholders or card backs
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List; // For energieProperty value type

public class VueAdversaire extends VBox {
    private static final int MAX_BENCH_SLOTS = 5; // Matching VueJoueurActif

    private IJeu jeu;
    private IJoueur adversaire;

    @FXML private Label nomAdversaireLabel;
    @FXML private Label pokemonActifAdversaireDisplay; // Renamed
    @FXML private HBox energiePokemonActifAdversaireHBox; // Added
    @FXML private HBox bancAdversaireHBox;
    @FXML private HBox panneauMainAdversaireHBox; // Added
    // mainAdversaireLabel is still used for count, but now part of a different logical group in FXML
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
            if (pokemonActifAdversaireDisplay != null) pokemonActifAdversaireDisplay.setText("N/A");
            if (energiePokemonActifAdversaireHBox != null) energiePokemonActifAdversaireHBox.getChildren().clear();
            if (panneauMainAdversaireHBox != null) panneauMainAdversaireHBox.getChildren().clear();
            if (bancAdversaireHBox != null) bancAdversaireHBox.getChildren().clear();
            if (mainAdversaireLabel != null) mainAdversaireLabel.setText("Main Adv.: N/A");
            if (deckAdversaireLabel != null) deckAdversaireLabel.setText("Deck Adv.: N/A");
            if (defausseAdversaireLabel != null) defausseAdversaireLabel.setText("Défausse Adv.: N/A");
            if (prixAdversaireLabel != null) prixAdversaireLabel.setText("Prix Adv.: N/A");
            return;
        }

        // Initialize the shared listener for card counts
        this.cardCountChangeListener = c -> {
            mettreAJourComptesCartesAdversaire(); // Updates count labels
            placerMainAdversaire(); // Updates visual hand display
        };

        // Initial UI setup
        if (nomAdversaireLabel != null) nomAdversaireLabel.setText(adversaire.getNom());
        placerPokemonActifAdversaire();
        placerBancAdversaire();
        placerMainAdversaire(); // Initial placement of hand representation
        mettreAJourComptesCartesAdversaire(); // Initial update of counts

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
        IPokemon pkmnActif = null;
        if (adversaire != null && adversaire.pokemonActifProperty() != null) {
            pkmnActif = adversaire.pokemonActifProperty().get();
        }

        if (pokemonActifAdversaireDisplay != null) {
            if (pkmnActif != null && pkmnActif.getCartePokemon() != null) {
                pokemonActifAdversaireDisplay.setText(pkmnActif.getCartePokemon().getNom());
            } else {
                pokemonActifAdversaireDisplay.setText("Aucun");
            }
        }

        if (energiePokemonActifAdversaireHBox != null) {
            energiePokemonActifAdversaireHBox.getChildren().clear();
            if (pkmnActif != null) {
                ObservableMap<String, List<String>> energieMap = pkmnActif.energieProperty();
                if (energieMap != null) {
                    for (java.util.Map.Entry<String, List<String>> entry : energieMap.entrySet()) {
                        Label energyLabel = new Label(entry.getKey() + " x" + entry.getValue().size());
                        energyLabel.getStyleClass().add("energy-tag");
                        energiePokemonActifAdversaireHBox.getChildren().add(energyLabel);
                    }
                }
            }
        }
    }

    private Node creerOpponentPokemonBancNode(IPokemon pokemon) {
        VBox pokemonCardContainer = new VBox(2);
        pokemonCardContainer.getStyleClass().add("pokemon-node-display"); // Using similar style as player's bench node
        pokemonCardContainer.setAlignment(Pos.CENTER);

        Label pkmnLabel = new Label(pokemon.getCartePokemon().getNom());
        pkmnLabel.getStyleClass().setAll("opponent-card-display", "text-18px"); // Name part

        HBox energieHBox = new HBox(2);
        energieHBox.setAlignment(Pos.CENTER);
        ObservableMap<String, List<String>> energieMap = pokemon.energieProperty();
        if (energieMap != null) {
            for (java.util.Map.Entry<String, List<String>> entry : energieMap.entrySet()) {
                Label energyLabel = new Label(entry.getKey() + " x" + entry.getValue().size());
                energyLabel.getStyleClass().add("energy-tag");
                energieHBox.getChildren().add(energyLabel);
            }
        }
        pokemonCardContainer.getChildren().addAll(pkmnLabel, energieHBox);
        return pokemonCardContainer;
    }

    private void placerBancAdversaire() {
        if (bancAdversaireHBox == null) return;
        bancAdversaireHBox.getChildren().clear();
        if (adversaire != null && adversaire.getBanc() != null) {
            for (IPokemon pokemon : adversaire.getBanc()) {
                if (pokemon != null && pokemon.getCartePokemon() != null) {
                    bancAdversaireHBox.getChildren().add(creerOpponentPokemonBancNode(pokemon));
                }
            }
            // For fixed slots (displaying empty placeholders), loop MAX_BENCH_SLOTS
            // and add placeholders if pokemon is null. Simpler for now: only show actual Pokemon.
        }
    }

    private void placerMainAdversaire() {
        if (panneauMainAdversaireHBox == null) return;
        panneauMainAdversaireHBox.getChildren().clear();
        if (adversaire != null && adversaire.getMain() != null) {
            int handSize = adversaire.getMain().size();
            for (int i = 0; i < handSize; i++) {
                Label cardBack = new Label("Carte"); // Placeholder text
                cardBack.getStyleClass().add("opponent-card-back"); // CSS class for styling
                // Dimensions for card backs should be handled by CSS class .opponent-card-back
                panneauMainAdversaireHBox.getChildren().add(cardBack);
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
