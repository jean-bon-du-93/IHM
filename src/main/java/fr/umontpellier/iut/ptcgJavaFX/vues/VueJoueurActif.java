package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener; // Added for energy listener
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap; // Added for energieProperty
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos; // Added for alignment in placerBanc
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List; // Added for type in MapChangeListener

public class VueJoueurActif extends VBox {

    private IJeu jeu;
    private ObjectProperty<IJoueur> joueurActifProperty;
    @FXML
    private Label nomDuJoueurLabel;
    @FXML
    private Button pokemonActifButton;
    @FXML
    private HBox energiePokemonActifHBox; // Added
    @FXML
    private HBox panneauMainHBox;
    @FXML
    private HBox panneauBancHBox;

    private ChangeListener<IJoueur> joueurActifGlobalChangeListener;
    private ChangeListener<IPokemon> pokemonDuJoueurActifChangeListener;
    private ListChangeListener<ICarte> mainDuJoueurActifChangeListener;
    private ListChangeListener<IPokemon> changementBancJoueur;
    private MapChangeListener<String, List<String>> energiePokemonActifListener; // Added


    @FXML
    private Button passerButton;

    // IJeu jeu field should already exist from previous refactoring, ensure it's not final if it was.
    // private IJeu jeu; // Ensure this field is present

    public VueJoueurActif() { // No-arg constructor
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VueJoueurActif.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        // DO NOT call initialiserProprietesEtListeners() or lierAuJoueurActifDuJeu() here
        // as 'jeu' is not yet set.
    }

    public void setJeu(IJeu jeu) {
        this.jeu = jeu;
    }

    public void postInit() {
        initialiserProprietesEtListeners();
        lierAuJoueurActifDuJeu();
    }

    @FXML
    private void actionPasserParDefaut(ActionEvent event) {
        System.out.println("Passer button clicked in VueJoueurActif, calling jeu.passerAEteChoisi()");
        if (this.jeu != null) {
            this.jeu.passerAEteChoisi();
        }
    }

    public void lierAuJoueurActifDuJeu() {
        if (this.jeu != null && this.jeu.joueurActifProperty() != null) {
            this.joueurActifProperty.bind(this.jeu.joueurActifProperty());
        }
    }

    private void initialiserProprietesEtListeners() {
        this.joueurActifProperty = new SimpleObjectProperty<>(null);

        StringBinding nomJoueurBinding = new StringBinding() {
            {
                super.bind(VueJoueurActif.this.joueurActifProperty);
            }

            @Override
            protected String computeValue() {
                IJoueur currentPlayer = VueJoueurActif.this.joueurActifProperty.get();
                return (currentPlayer == null) ? "Pas de joueur actif" : currentPlayer.getNom();
            }
        };
        // Ensure nomDuJoueurLabel is initialized by FXML before this line
        if (nomDuJoueurLabel != null) {
            nomDuJoueurLabel.textProperty().bind(nomJoueurBinding);
        }

        this.energiePokemonActifListener = change -> {
            // This listener is on the ObservableMap itself for the current active Pokemon.
            // Re-calling placerPokemonActif will update its energy display.
            placerPokemonActif();
        };

        this.pokemonDuJoueurActifChangeListener = (obs, oldPkmn, newPkmn) -> {
            // This listener is on the joueurActif.pokemonActifProperty()
            // It fires when the Pokemon instance in the active slot changes.
            if (oldPkmn != null) {
                ObservableMap<String, List<String>> oldEnergieMap = oldPkmn.energieProperty();
                if (oldEnergieMap != null && this.energiePokemonActifListener != null) { // NPE check
                    oldEnergieMap.removeListener(this.energiePokemonActifListener);
                }
            }
            placerPokemonActif(); // This will set button text and display energy for newPkmn
            if (newPkmn != null) {
                ObservableMap<String, List<String>> newEnergieMap = newPkmn.energieProperty();
                if (newEnergieMap != null && this.energiePokemonActifListener != null) { // NPE check
                    newEnergieMap.addListener(this.energiePokemonActifListener);
                }
            } else {
                // No new active Pokemon, ensure energy display is cleared
                if (energiePokemonActifHBox != null) {
                    energiePokemonActifHBox.getChildren().clear();
                }
            }
        };

        this.mainDuJoueurActifChangeListener = (ListChangeListener.Change<? extends ICarte> c) -> {
            // Call new method that processes the Change object
            updatePanneauMain(c);
        };

        this.changementBancJoueur = (ListChangeListener.Change<? extends IPokemon> c) -> {
            updatePanneauBanc(c);
        };

        this.joueurActifGlobalChangeListener = (observable, oldJoueur, newJoueur) -> {
            // Detach all listeners from oldJoueur and its properties
            if (oldJoueur != null) {
                ObjectProperty<? extends IPokemon> oldActivePokemonProp = oldJoueur.pokemonActifProperty();
                if (oldActivePokemonProp != null) {
                    oldActivePokemonProp.removeListener(this.pokemonDuJoueurActifChangeListener);
                    IPokemon oldActivePkmnInstance = oldActivePokemonProp.get();
                    if (oldActivePkmnInstance != null) {
                        ObservableMap<String, List<String>> oldEnergieMap = oldActivePkmnInstance.energieProperty();
                        if (oldEnergieMap != null) {
                            oldEnergieMap.removeListener(this.energiePokemonActifListener);
                        }
                    }
                }
                if (oldJoueur.getMain() != null) {
                    oldJoueur.getMain().removeListener(this.mainDuJoueurActifChangeListener);
                }
                if (oldJoueur.getBanc() != null) {
                    oldJoueur.getBanc().removeListener(this.changementBancJoueur);
                    // TODO: If individual benched Pokemon energy listeners were added, remove them here.
                }
            }

            // Update displays for newJoueur - placerPokemonActif also handles energy
            placerPokemonActif();
            reconstruirePanneauMainComplet();
            reconstruirePanneauBancComplet(); // Renamed from placerBanc

            // Attach all listeners to newJoueur and its properties
            if (newJoueur != null) {
                ObjectProperty<? extends IPokemon> newActivePokemonProp = newJoueur.pokemonActifProperty();
                if (newActivePokemonProp != null) {
                    newActivePokemonProp.addListener(this.pokemonDuJoueurActifChangeListener);
                    IPokemon newActivePkmnInstance = newActivePokemonProp.get(); // Get current active to attach energy listener
                    if (newActivePkmnInstance != null) {
                        ObservableMap<String, List<String>> newEnergieMap = newActivePkmnInstance.energieProperty();
                        if (newEnergieMap != null) {
                            newEnergieMap.addListener(this.energiePokemonActifListener);
                        }
                    }
                }
                if (newJoueur.getMain() != null) {
                    newJoueur.getMain().addListener(this.mainDuJoueurActifChangeListener);
                }
                if (newJoueur.getBanc() != null) {
                    newJoueur.getBanc().addListener(this.changementBancJoueur);
                    // TODO: If individual benched Pokemon energy listeners are to be added, do it here.
                }
            } else { // No new player (e.g. game end), clear displays
                if (energiePokemonActifHBox != null) energiePokemonActifHBox.getChildren().clear();
            }
        };
        this.joueurActifProperty.addListener(this.joueurActifGlobalChangeListener);
    }

    public void placerPokemonActif() {
        String texteAffichage = "Aucun Pokémon actif";
        IPokemon currentActivePokemon = null; // To hold the current active Pokemon instance
        IJoueur joueurCourant = joueurActifProperty.get();

        if (joueurCourant != null) {
            ObjectProperty<? extends IPokemon> pokemonProperty = joueurCourant.pokemonActifProperty();
            if (pokemonProperty != null) {
                currentActivePokemon = pokemonProperty.get();
                if (currentActivePokemon != null && currentActivePokemon.getCartePokemon() != null) {
                    texteAffichage = currentActivePokemon.getCartePokemon().getNom();
                }
            }
        }
        if (pokemonActifButton != null) {
            pokemonActifButton.setText(texteAffichage);
        }

        // Display energy for active Pokemon
        if (energiePokemonActifHBox != null) {
            energiePokemonActifHBox.getChildren().clear();
            if (currentActivePokemon != null) {
                ObservableMap<String, List<String>> energieMap = currentActivePokemon.energieProperty();
                if (energieMap != null) {
                    for (java.util.Map.Entry<String, List<String>> entry : energieMap.entrySet()) {
                        Label energyLabel = new Label(entry.getKey() + " x" + entry.getValue().size());
                        energyLabel.setStyle("-fx-font-size: 10px; -fx-padding: 2px; -fx-border-color: lightgray;");
                        energiePokemonActifHBox.getChildren().add(energyLabel);
                    }
                }
            }
        }
    }

    @FXML
    private void onPokemonActifButtonClick(ActionEvent event) {
        System.out.println("pokemonActifButton clicked. Active Pokémon: " + (pokemonActifButton != null ? pokemonActifButton.getText() : "N/A") + ". Action to be defined.");
        // Further actions to be defined based on game requirements.
    }

    private Button creerBoutonCarte(ICarte carte) {
        Button boutonCarte = new Button(carte.getNom());
        boutonCarte.setUserData(carte); // Store card data for identification
        boutonCarte.getStyleClass().add("text-18px");
        boutonCarte.setOnAction(event -> {
            if (this.jeu != null) {
                this.jeu.uneCarteDeLaMainAEteChoisie(carte.getId());
            }
        });
        return boutonCarte;
    }

    private void updatePanneauMain(ListChangeListener.Change<? extends ICarte> change) {
        if (panneauMainHBox == null) return;

        while (change.next()) {
            if (change.wasPermutated() || change.wasUpdated()) {
                // Simplest way to handle permutation or update for now: rebuild all.
                System.out.println("Main change type (permutation/update) triggered full rebuild of main.");
                reconstruirePanneauMainComplet();
                return; // Exit after rebuild
            } else {
                // Handle wasRemoved and wasAdded
                if (change.wasRemoved()) {
                    List<? extends ICarte> removedCards = change.getRemoved();
                    panneauMainHBox.getChildren().removeIf(node -> {
                        if (node.getUserData() instanceof ICarte) {
                            ICarte cardData = (ICarte) node.getUserData();
                            // Check if this node's card ID is in the list of removed card IDs
                            return removedCards.stream().anyMatch(rc -> rc.getId().equals(cardData.getId()));
                        }
                        return false;
                    });
                }
                if (change.wasAdded()) {
                    List<? extends ICarte> addedCards = change.getAddedSubList();
                    int startIndex = change.getFrom();
                    for (int i = 0; i < addedCards.size(); ++i) {
                        ICarte carte = addedCards.get(i);
                        Button boutonCarte = creerBoutonCarte(carte);
                        // Add at correct index if list isn't implicitly sorted after add
                        if (startIndex + i < panneauMainHBox.getChildren().size()) {
                            panneauMainHBox.getChildren().add(startIndex + i, boutonCarte);
                        } else {
                            panneauMainHBox.getChildren().add(boutonCarte);
                        }
                    }
                }
            }
        }
    }

    public void reconstruirePanneauMainComplet() {
        if (panneauMainHBox == null) return;
        panneauMainHBox.getChildren().clear();
        IJoueur joueurCourant = joueurActifProperty.get();

        if (joueurCourant != null) {
            ObservableList<? extends ICarte> mainDuJoueur = joueurCourant.getMain();
            if (mainDuJoueur != null) {
                for (ICarte carte : mainDuJoueur) {
                    panneauMainHBox.getChildren().add(creerBoutonCarte(carte));
                }
            }
        }
    }

    private javafx.scene.Node creerPokemonBancNode(IPokemon pokemon) {
        Button boutonPokemonBanc = new Button(pokemon.getCartePokemon().getNom());
        boutonPokemonBanc.getStyleClass().add("text-18px");
        boutonPokemonBanc.setOnAction(event -> {
            System.out.println("Bouton Pokémon du banc cliqué : " + pokemon.getCartePokemon().getNom() + " (ID: " + pokemon.getCartePokemon().getId() + "). Action à définir.");
        });

        VBox pokemonCardContainer = new VBox(2);
        pokemonCardContainer.setAlignment(Pos.CENTER);
        pokemonCardContainer.setUserData(pokemon); // Store pokemon for identification in updatePanneauBanc

        HBox energieBancPokemonHBox = new HBox(2);
        energieBancPokemonHBox.setAlignment(Pos.CENTER);

        ObservableMap<String, List<String>> energieMap = pokemon.energieProperty();
        if (energieMap != null) {
            for (java.util.Map.Entry<String, List<String>> entry : energieMap.entrySet()) {
                Label energyLabel = new Label(entry.getKey() + " x" + entry.getValue().size());
                energyLabel.setStyle("-fx-font-size: 9px; -fx-padding: 1px; -fx-border-color: lightgray;");
                energieBancPokemonHBox.getChildren().add(energyLabel);
            }
        }
        pokemonCardContainer.getChildren().addAll(boutonPokemonBanc, energieBancPokemonHBox);
        return pokemonCardContainer;
    }

    private void updatePanneauBanc(ListChangeListener.Change<? extends IPokemon> change) {
        if (panneauBancHBox == null) return;

        while (change.next()) {
            if (change.wasPermutated() || change.wasUpdated()) {
                System.out.println("Banc change type (permutation/update) triggered full rebuild of banc.");
                reconstruirePanneauBancComplet();
                return;
            } else {
                if (change.wasRemoved()) {
                    List<? extends IPokemon> removedPokemons = change.getRemoved();
                    panneauBancHBox.getChildren().removeIf(node -> {
                        if (node.getUserData() instanceof IPokemon) {
                            IPokemon pokemonData = (IPokemon) node.getUserData();
                            // Assuming IPokemon instances are unique enough or have a unique ID for comparison
                            // If not, this might remove wrong elements if multiple identical (by content) Pokemon exist
                            return removedPokemons.contains(pokemonData);
                        }
                        return false;
                    });
                }
                if (change.wasAdded()) {
                    List<? extends IPokemon> addedPokemons = change.getAddedSubList();
                    int startIndex = change.getFrom();
                    for (int i = 0; i < addedPokemons.size(); ++i) {
                        IPokemon pokemon = addedPokemons.get(i);
                        javafx.scene.Node pokemonNode = creerPokemonBancNode(pokemon);
                        if (startIndex + i < panneauBancHBox.getChildren().size()) {
                            panneauBancHBox.getChildren().add(startIndex + i, pokemonNode);
                        } else {
                            panneauBancHBox.getChildren().add(pokemonNode);
                        }
                    }
                }
            }
        }
    }

    public void reconstruirePanneauBancComplet() {
        if (panneauBancHBox == null) return;
        panneauBancHBox.getChildren().clear();
        IJoueur joueurCourant = joueurActifProperty.get();

        if (joueurCourant != null && joueurCourant.getBanc() != null) {
            for (IPokemon pokemon : joueurCourant.getBanc()) {
                if (pokemon != null && pokemon.getCartePokemon() != null) {
                    panneauBancHBox.getChildren().add(creerPokemonBancNode(pokemon));
                }
            }
        }
    }
}