package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener; // Added import
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap; // For energy display
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos; // For alignment
import javafx.scene.Node; // For creerPokemonBancNode return type
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte; // Added import
import javafx.event.ActionEvent; // Added import
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region; // For placeholders or card backs
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List; // For energieProperty value type

public class VueAdversaire extends VBox {
    private static final int MAX_BENCH_SLOTS = 5; // Matching VueJoueurActif

    private IJeu jeu;
    private IJoueur adversaire;

    @FXML Label nomAdversaireLabel;
    @FXML Button opponentPokemonActifButton; // Changed from Label to Button and renamed
    @FXML HBox energiePokemonActifAdversaireHBox; // Added
    @FXML HBox bancAdversaireHBox;
    @FXML HBox panneauMainAdversaireHBox; // Added
    // mainAdversaireLabel is still used for count, but now part of a different logical group in FXML
    @FXML Label mainAdversaireLabel;
    @FXML Label deckAdversaireLabel;
    @FXML Label defausseAdversaireLabel;
    @FXML Label prixAdversaireLabel;

    // Listeners to update UI when properties of 'adversaire' change
    private ChangeListener<IPokemon> pokemonActifListener;
    private ListChangeListener<IPokemon> bancListener;
    // private ListChangeListener<ICarte> mainListener; // Replaced by cardCountChangeListener
    // private ListChangeListener<ICarte> deckListener; // Replaced by cardCountChangeListener
    // private ListChangeListener<ICarte> defausseListener; // Replaced by cardCountChangeListener
    // private ListChangeListener<ICarte> prixListener; // Replaced by cardCountChangeListener
    private ListChangeListener<ICarte> cardCountChangeListener; // For main, deck, discard, prizes
    private MapChangeListener<String, List<String>> energiePokemonActifAdversaireListener; // Added field
    private IPokemon opponentActivePokemonForEnergyListener = null; // To keep track of the Pokemon whose energy is being listened to


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
            if (opponentPokemonActifButton != null) opponentPokemonActifButton.setText("N/A"); // Changed field name
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

        // Removed old setOnMouseClicked handler for pokemonActifAdversaireDisplay from here.
        // The new Button will use onAction specified in FXML.

        placerPokemonActifAdversaire();
        placerBancAdversaire();
        placerMainAdversaire(); // Initial placement of hand representation
        mettreAJourComptesCartesAdversaire(); // Initial update of counts

        // Setup listeners
        setupListeners();

        if (this.jeu != null && this.jeu instanceof fr.umontpellier.iut.ptcgJavaFX.mecanique.Jeu) {
            fr.umontpellier.iut.ptcgJavaFX.mecanique.Jeu jeuConcret = (fr.umontpellier.iut.ptcgJavaFX.mecanique.Jeu) this.jeu;
            // Pour éviter d'ajouter plusieurs listeners si initialiserAffichage est appelé plusieurs fois avec le même jeu,
            // il serait mieux de passer le listener à clearBindingsAndListeners pour le retirer.
            // Pour l'instant, on va simplement l'ajouter.
            // A more robust solution would be to manage this listener in setJeu or ensure it's cleared if VueAdversaire can be re-assigned a new Jeu instance.
            // Or, check if a listener is already attached before adding.
            // For simplicity as per task, just adding:
            jeuConcret.carteSelectionneeProperty().addListener((obs, oldSelection, newSelection) -> {
                mettreAJourStyleSelectionPokemonAdversaire(newSelection);
            });
            mettreAJourStyleSelectionPokemonAdversaire(jeuConcret.carteSelectionneeProperty().get());
        }
    }

    private void setupListeners() {
        if (this.adversaire == null) return; // NPE Hardening

        // Define the listener for energy changes on the active Pokemon
        this.energiePokemonActifAdversaireListener = change -> {
            rafraichirEnergiePokemonActifAdversaire();
        };

        // Listener for active Pokémon changes
        ObjectProperty<? extends IPokemon> pokemonActifProp = adversaire.pokemonActifProperty();
        if (pokemonActifProp != null) {
            pokemonActifListener = (obs, oldVal, newVal) -> {
                // Detach listener from old active Pokemon's energy
                if (oldVal != null && oldVal.energieProperty() != null) {
                    oldVal.energieProperty().removeListener(this.energiePokemonActifAdversaireListener);
                }
                opponentActivePokemonForEnergyListener = newVal; // Update tracked Pokemon

                placerPokemonActifAdversaire(); // This will also call rafraichirEnergiePokemonActifAdversaire

                // Attach listener to new active Pokemon's energy
                if (newVal != null && newVal.energieProperty() != null) {
                    newVal.energieProperty().addListener(this.energiePokemonActifAdversaireListener);
                }
            };
            pokemonActifProp.addListener(pokemonActifListener);
            // Initial attachment to current active Pokemon's energy (if any)
            IPokemon currentActive = pokemonActifProp.get();
            if (currentActive != null && currentActive.energieProperty() != null) {
                currentActive.energieProperty().addListener(this.energiePokemonActifAdversaireListener);
                opponentActivePokemonForEnergyListener = currentActive;
            }
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
        // Detach energy listener from the last known active Pokemon
        if (opponentActivePokemonForEnergyListener != null && opponentActivePokemonForEnergyListener.energieProperty() != null && energiePokemonActifAdversaireListener != null) {
            opponentActivePokemonForEnergyListener.energieProperty().removeListener(energiePokemonActifAdversaireListener);
        }
        opponentActivePokemonForEnergyListener = null;


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

        if (opponentPokemonActifButton != null) { // Changed field name
            if (pkmnActif != null && pkmnActif.getCartePokemon() != null) {
                opponentPokemonActifButton.setText(pkmnActif.getCartePokemon().getNom());
            } else {
                opponentPokemonActifButton.setText("Aucun");
            }
        }
        // The energy display is now handled by rafraichirEnergiePokemonActifAdversaire()
        // which is called by this method's caller (pokemonActifListener) or directly by the energy listener.
        // Call it here for initial setup or if this method is called outside the listener chain.
        rafraichirEnergiePokemonActifAdversaire();
    }

    private void rafraichirEnergiePokemonActifAdversaire() {
        if (energiePokemonActifAdversaireHBox == null) return;
        energiePokemonActifAdversaireHBox.getChildren().clear();
        IPokemon pkmnActif = (this.adversaire != null && this.adversaire.pokemonActifProperty() != null) ? this.adversaire.pokemonActifProperty().get() : null;
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

    private Node creerOpponentPokemonBancNode(IPokemon pokemon) {
        VBox pokemonCardContainer = new VBox(2);
        pokemonCardContainer.getStyleClass().add("pokemon-node-display");
        pokemonCardContainer.setAlignment(Pos.CENTER);
        if (pokemon != null && pokemon.getCartePokemon() != null && pokemon.getCartePokemon().getId() != null) {
            pokemonCardContainer.setUserData(pokemon.getCartePokemon().getId()); // Store card ID
        }

        Button pokemonButton = new Button(pokemon.getCartePokemon().getNom());
        pokemonButton.getStyleClass().setAll("card-button", "text-18px"); // Apply button styling
        pokemonButton.setOnAction(actionEvent -> {
            if (this.jeu != null && pokemon != null && pokemon.getCartePokemon() != null && pokemon.getCartePokemon().getId() != null) {
                this.jeu.carteSurTerrainCliquee(pokemon.getCartePokemon().getId());
            } else {
                // Optionnel: Gérer le cas
                System.err.println("Clic sur Pokémon de banc adverse, mais pas de Pokémon/carte/ID trouvé.");
            }
        });

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
        pokemonCardContainer.getChildren().addAll(pokemonButton, energieHBox);

        // Removed old pokemonCardContainer.setOnMouseClicked handler.
        // Click actions are now on the pokemonButton via setOnAction.

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

    @FXML
    void handleOpponentActivePokemonClick(ActionEvent event) {
        if (this.jeu != null && this.adversaire != null) {
            IPokemon activePokemon = this.adversaire.pokemonActifProperty().get();
            if (activePokemon != null && activePokemon.getCartePokemon() != null && activePokemon.getCartePokemon().getId() != null) {
                this.jeu.carteSurTerrainCliquee(activePokemon.getCartePokemon().getId());
            } else {
                // Optionnel: Gérer le cas où il n'y a pas de Pokémon actif cliquable
                System.err.println("Clic sur Pokémon actif adverse, mais pas de Pokémon/carte/ID trouvé.");
            }
        }
    }

    private void mettreAJourStyleSelectionPokemonAdversaire(Carte carteActuellementSelectionnee) {
        String idCarteSelectionnee = (carteActuellementSelectionnee == null) ? null : carteActuellementSelectionnee.getId();

        // Pokémon Actif de l'adversaire
        if (opponentPokemonActifButton != null && this.adversaire != null) {
            IPokemon pkmnActif = this.adversaire.pokemonActifProperty().get();
            if (pkmnActif != null && pkmnActif.getCartePokemon() != null && pkmnActif.getCartePokemon().getId() != null) {
                if (pkmnActif.getCartePokemon().getId().equals(idCarteSelectionnee)) {
                    opponentPokemonActifButton.getStyleClass().add("pokemon-selectionne");
                } else {
                    opponentPokemonActifButton.getStyleClass().removeAll("pokemon-selectionne");
                }
            } else {
                opponentPokemonActifButton.getStyleClass().removeAll("pokemon-selectionne");
            }
        } else if (opponentPokemonActifButton != null) { // Ensure style is removed if no adversary or button
             opponentPokemonActifButton.getStyleClass().removeAll("pokemon-selectionne");
        }


        // Pokémon du Banc de l'adversaire
        if (bancAdversaireHBox != null) {
            for (Node nodePokemonBanc : bancAdversaireHBox.getChildren()) {
                if (nodePokemonBanc.getUserData() instanceof String) {
                    String idCarteNode = (String) nodePokemonBanc.getUserData();
                    if (idCarteNode.equals(idCarteSelectionnee)) {
                        nodePokemonBanc.getStyleClass().add("pokemon-selectionne");
                    } else {
                        nodePokemonBanc.getStyleClass().removeAll("pokemon-selectionne");
                    }
                } else {
                    // Cas des nœuds non-pokemon (devraient pas avoir userData String ID, ou autres types de noeuds)
                    nodePokemonBanc.getStyleClass().removeAll("pokemon-selectionne");
                }
            }
        }
    }
}
