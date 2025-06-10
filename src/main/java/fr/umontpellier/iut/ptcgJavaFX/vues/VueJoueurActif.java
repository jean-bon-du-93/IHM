package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
// import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemon; // Commented out if no longer needed
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
import javafx.scene.Node; // Added for mettreAJourStyleSelectionPokemon
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte; // Added for mettreAJourStyleSelectionPokemon

import java.io.IOException;
import java.util.ArrayList; // Added for listener cleanup
import java.util.HashMap; // Added for new Maps
import java.util.List; // Added for type in MapChangeListener
import java.util.Map; // Added for new Maps

public class VueJoueurActif extends VBox {

    private static final int MAX_BENCH_SLOTS = 5; // Added constant

    private IJeu jeu;
    private ObjectProperty<IJoueur> joueurActifProperty;
    @FXML
    Label nomDuJoueurLabel;
    @FXML
    Button pokemonActifButton;
    @FXML
    HBox energiePokemonActifHBox; // Added
    @FXML
    HBox panneauMainHBox;
    @FXML
    HBox panneauBancHBox;

    private ChangeListener<IJoueur> joueurActifGlobalChangeListener;
    private ChangeListener<IPokemon> pokemonDuJoueurActifChangeListener;
    private ListChangeListener<ICarte> mainDuJoueurActifChangeListener;
    private ListChangeListener<IPokemon> changementBancJoueur;
    private MapChangeListener<String, List<String>> energiePokemonActifListener; // For active Pokemon
    // private ChangeListener<CartePokemon> carteDuPokemonActifListener; // RETIRÉ

    // Fields for benched Pokemon energy listeners
    private final Map<IPokemon, MapChangeListener<String, List<String>>> benchEnergyListeners = new HashMap<>();
    private final Map<IPokemon, HBox> benchPokemonEnergyUI = new HashMap<>();
    // private final Map<IPokemon, ChangeListener<CartePokemon>> benchPokemonCardListeners = new HashMap<>(); // RETIRÉ
    // private final Map<IPokemon, Button> benchPokemonButtons = new HashMap<>(); // RETIRÉ


    @FXML
    Button passerButton;

    // IJeu jeu field should already exist from previous refactoring, ensure it's not final if it was.
    // private IJeu jeu; // Ensure this field is present

    public VueJoueurActif() { // No-arg constructor
        // Initialize maps here if they were not final or directly initialized
        // benchEnergyListeners = new HashMap<>();
        // benchPokemonEnergyUI = new HashMap<>();
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

        if (this.jeu != null && this.jeu instanceof fr.umontpellier.iut.ptcgJavaFX.mecanique.Jeu) {
            fr.umontpellier.iut.ptcgJavaFX.mecanique.Jeu jeuConcret = (fr.umontpellier.iut.ptcgJavaFX.mecanique.Jeu) this.jeu;
            jeuConcret.carteSelectionneeProperty().addListener((obs, oldSelection, newSelection) -> {
                mettreAJourStyleSelectionPokemon(newSelection);
            });
            // Appel initial pour mise à jour au cas où une sélection existe déjà
            mettreAJourStyleSelectionPokemon(jeuConcret.carteSelectionneeProperty().get());
        }
    }

    @FXML
    void actionPasserParDefaut(ActionEvent event) { // Already package-private, no change needed, but keeping the search pattern to ensure this is the intended state
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

        // Initialisation de carteDuPokemonActifListener RETIRÉE
        // this.carteDuPokemonActifListener = (obsCard, oldCard, newCard) -> {
        //     placerPokemonActif();
        // };

        this.pokemonDuJoueurActifChangeListener = (obsPokemon, oldPkmn, newPkmn) -> { // obsPokemon for clarity
            // This listener is on the joueurActif.pokemonActifProperty()
            // It fires when the Pokemon instance in the active slot changes.
            if (oldPkmn != null) {
                // Detach energy listener
                ObservableMap<String, List<String>> oldEnergieMap = oldPkmn.energieProperty();
                if (oldEnergieMap != null && this.energiePokemonActifListener != null) {
                    oldEnergieMap.removeListener(this.energiePokemonActifListener);
                }
                // Détachement de carteDuPokemonActifListener RETIRÉ
                // if (oldPkmn.cartePokemonProperty() != null && this.carteDuPokemonActifListener != null) {
                //    oldPkmn.cartePokemonProperty().removeListener(this.carteDuPokemonActifListener);
                // }
            }

            placerPokemonActif(); // Met à jour l'affichage (nom, etc.) pour le newPkmn

            // Attachement des listeners au nouveau Pokémon actif
            if (newPkmn != null) {
                // Attach energy listener
                ObservableMap<String, List<String>> newEnergieMap = newPkmn.energieProperty();
                if (newEnergieMap != null && this.energiePokemonActifListener != null) {
                    newEnergieMap.addListener(this.energiePokemonActifListener);
                }
                // Attachement de carteDuPokemonActifListener RETIRÉ
                // if (newPkmn.cartePokemonProperty() != null && this.carteDuPokemonActifListener != null) {
                //    newPkmn.cartePokemonProperty().addListener(this.carteDuPokemonActifListener);
                // }
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
                        if (oldEnergieMap != null && this.energiePokemonActifListener != null) {
                            oldEnergieMap.removeListener(this.energiePokemonActifListener);
                        }
                        // Détachement de carteDuPokemonActifListener RETIRÉ
                        // if (oldActivePkmnInstance.cartePokemonProperty() != null && this.carteDuPokemonActifListener != null) {
                        //    oldActivePkmnInstance.cartePokemonProperty().removeListener(this.carteDuPokemonActifListener);
                        // }
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
                        if (newEnergieMap != null && this.energiePokemonActifListener != null) {
                            newEnergieMap.addListener(this.energiePokemonActifListener);
                        }
                        // Attachement de carteDuPokemonActifListener RETIRÉ
                        // if (newActivePkmnInstance.cartePokemonProperty() != null && this.carteDuPokemonActifListener != null) {
                        //     newActivePkmnInstance.cartePokemonProperty().addListener(this.carteDuPokemonActifListener);
                        // }
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
                        // energyLabel.setStyle("-fx-font-size: 10px; -fx-padding: 2px; -fx-border-color: lightgray;"); // Removed inline style
                        energyLabel.getStyleClass().add("energy-tag"); // Added style class
                        energiePokemonActifHBox.getChildren().add(energyLabel);
                    }
                }
            }
        }
    }

    @FXML
    private void onPokemonActifButtonClick(ActionEvent event) {
        if (this.jeu != null && this.joueurActifProperty != null) {
            IJoueur joueurCourant = this.joueurActifProperty.get();
            if (joueurCourant != null) {
                IPokemon activePokemon = joueurCourant.pokemonActifProperty().get();
                if (activePokemon != null && activePokemon.getCartePokemon() != null && activePokemon.getCartePokemon().getId() != null) {
                    String idCarteCliquee = activePokemon.getCartePokemon().getId();
                    this.jeu.carteSurTerrainCliquee(idCarteCliquee); // Appel existant pour la sélection visuelle

                    // AJOUT: Si une carte est en jeu et attend une cible, notifier ce choix.
                    if (joueurCourant.carteEnJeuProperty().get() != null) {
                        this.jeu.uneCarteComplementaireAEteChoisie(idCarteCliquee);
                    }
                } else {
                    System.err.println("Clic sur Pokémon actif du joueur, mais pas de Pokémon/carte/ID trouvé.");
                }
            }
        }
    }

    private Button creerBoutonCarte(ICarte carte) {
        Button boutonCarte = new Button(carte.getNom());
        boutonCarte.setUserData(carte); // Store card data for identification
        // boutonCarte.getStyleClass().add("text-18px"); // Replaced by setAll
        boutonCarte.getStyleClass().setAll("card-button", "text-18px");
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
        // boutonPokemonBanc.getStyleClass().add("text-18px"); // Replaced by setAll
        boutonPokemonBanc.getStyleClass().setAll("card-button", "text-18px");
        boutonPokemonBanc.setOnAction(event -> {
            if (this.jeu != null && pokemon != null && pokemon.getCartePokemon() != null && pokemon.getCartePokemon().getId() != null) {
                String idCarteCliquee = pokemon.getCartePokemon().getId();
                this.jeu.carteSurTerrainCliquee(idCarteCliquee); // Appel existant pour la sélection visuelle

                // AJOUT: Si une carte est en jeu et attend une cible, notifier ce choix.
                IJoueur joueurCourant = VueJoueurActif.this.joueurActifProperty.get();
                if (joueurCourant != null && joueurCourant.carteEnJeuProperty().get() != null) {
                    this.jeu.uneCarteComplementaireAEteChoisie(idCarteCliquee);
                }
            } else {
                System.err.println("Clic sur Pokémon de banc du joueur, mais pas de Pokémon/carte/ID trouvé.");
            }
        });

        VBox pokemonCardContainer = new VBox(2);
        pokemonCardContainer.setAlignment(Pos.CENTER);
        // Conserver l'ID de la carte pour la sélection de style, mais la clé des maps sera IPokemon
        if (pokemon != null && pokemon.getCartePokemon() != null && pokemon.getCartePokemon().getId() != null) {
            pokemonCardContainer.setUserData(pokemon.getCartePokemon().getId());
        }
        pokemonCardContainer.getStyleClass().add("pokemon-node-display");

        HBox energieBancPokemonHBox = new HBox(2);
        energieBancPokemonHBox.setAlignment(Pos.CENTER);

        // Populate energy for the first time
        populateBenchPokemonEnergy(energieBancPokemonHBox, pokemon);

        // Store HBox for future updates by listener
        this.benchPokemonEnergyUI.put(pokemon, energieBancPokemonHBox);

        // Create and attach listener for energy changes
        if (pokemon != null && pokemon.energieProperty() != null) {
            MapChangeListener<String, List<String>> energyListener = change -> {
                // When energy changes, re-populate the specific HBox for this pokemon
                populateBenchPokemonEnergy(this.benchPokemonEnergyUI.get(pokemon), pokemon);
            };
            pokemon.energieProperty().addListener(energyListener);
            this.benchEnergyListeners.put(pokemon, energyListener); // Store listener for cleanup
        }

        pokemonCardContainer.getChildren().addAll(boutonPokemonBanc, energieBancPokemonHBox);
        return pokemonCardContainer;
    }

    private void updatePanneauBanc(ListChangeListener.Change<? extends IPokemon> change) {
        // Simplified to always rebuild due to fixed slot display logic
        reconstruirePanneauBancComplet();
    }

    public void reconstruirePanneauBancComplet() {
        // Clean up old listeners and UI references for the bench
        for (Map.Entry<IPokemon, MapChangeListener<String, List<String>>> entry : this.benchEnergyListeners.entrySet()) {
            IPokemon oldPokemon = entry.getKey();
            MapChangeListener<String, List<String>> listener = entry.getValue();
            if (oldPokemon != null && oldPokemon.energieProperty() != null && listener != null) {
                oldPokemon.energieProperty().removeListener(listener);
            }
        }
        this.benchEnergyListeners.clear();
        this.benchPokemonEnergyUI.clear();

        if (panneauBancHBox == null) return;
        panneauBancHBox.getChildren().clear();
        IJoueur joueurCourant = joueurActifProperty.get();
        ObservableList<? extends IPokemon> banc = (joueurCourant != null) ? joueurCourant.getBanc() : null;

        for (int slotIndex = 0; slotIndex < MAX_BENCH_SLOTS; slotIndex++) {
            IPokemon pokemonInSlot = (banc != null && slotIndex < banc.size()) ? banc.get(slotIndex) : null;

            if (pokemonInSlot != null && pokemonInSlot.getCartePokemon() != null) {
                panneauBancHBox.getChildren().add(creerPokemonBancNode(pokemonInSlot));
            } else {
                Button emptySlotButton = new Button("Vide " + (slotIndex + 1));
                // emptySlotButton.getStyleClass().add("text-18px"); // Replaced by setAll
                emptySlotButton.getStyleClass().setAll("empty-bench-slot");
                // emptySlotButton.setPrefWidth(100); // Removed, should be controlled by CSS
                // emptySlotButton.setPrefHeight(100); // Removed, should be controlled by CSS
                final int finalSlotIndex = slotIndex; // For use in lambda
                emptySlotButton.setOnAction(event -> {
                    if (this.jeu != null) {
                        // Assuming the game logic expects a String for the slot index
                        this.jeu.unEmplacementVideDuBancAEteChoisi(String.valueOf(finalSlotIndex));
                    }
                });
                panneauBancHBox.getChildren().add(emptySlotButton);
            }
        }
    }

    private void populateBenchPokemonEnergy(HBox energyHBoxContainer, IPokemon pokemon) {
        if (energyHBoxContainer == null || pokemon == null) return;
        energyHBoxContainer.getChildren().clear();
        ObservableMap<String, List<String>> energieMap = pokemon.energieProperty();
        if (energieMap != null) {
            for (Map.Entry<String, List<String>> entry : energieMap.entrySet()) {
                Label energyLabel = new Label(entry.getKey() + " x" + entry.getValue().size());
                energyLabel.getStyleClass().add("energy-tag");
                energyHBoxContainer.getChildren().add(energyLabel);
            }
        }
    }

    private void mettreAJourStyleSelectionPokemon(Carte carteActuellementSelectionnee) {
        String idCarteSelectionnee = (carteActuellementSelectionnee == null) ? null : carteActuellementSelectionnee.getId();

        // Pokémon Actif
        if (pokemonActifButton != null && joueurActifProperty != null && joueurActifProperty.get() != null) {
            IJoueur joueurCourant = joueurActifProperty.get();
            if (joueurCourant != null) {
                IPokemon pkmnActif = joueurCourant.pokemonActifProperty().get();
                if (pkmnActif != null && pkmnActif.getCartePokemon() != null && pkmnActif.getCartePokemon().getId() != null) {
                    if (pkmnActif.getCartePokemon().getId().equals(idCarteSelectionnee)) {
                        pokemonActifButton.getStyleClass().add("pokemon-selectionne");
                    } else {
                        pokemonActifButton.getStyleClass().removeAll("pokemon-selectionne");
                    }
                } else { // Pas de Pokémon actif, s'assurer qu'il n'a pas le style
                    pokemonActifButton.getStyleClass().removeAll("pokemon-selectionne");
                }
            } else { // Pas de joueur courant, s'assurer que le bouton n'a pas le style
                 pokemonActifButton.getStyleClass().removeAll("pokemon-selectionne");
            }
        } else if (pokemonActifButton != null) { // S'assurer que le bouton est nettoyé si pas de joueur actif
             pokemonActifButton.getStyleClass().removeAll("pokemon-selectionne");
        }


        // Pokémon du Banc
        if (panneauBancHBox != null) {
            for (Node nodePokemonBanc : panneauBancHBox.getChildren()) {
                // On a stocké l'ID sur le VBox (pokemonCardContainer)
                if (nodePokemonBanc.getUserData() instanceof String) {
                    String idCarteNode = (String) nodePokemonBanc.getUserData();
                    if (idCarteNode.equals(idCarteSelectionnee)) {
                        nodePokemonBanc.getStyleClass().add("pokemon-selectionne");
                    } else {
                        nodePokemonBanc.getStyleClass().removeAll("pokemon-selectionne");
                    }
                } else {
                    // Cas des boutons "Vide" ou autres nœuds non-pokemon, s'assurer qu'ils n'ont pas le style
                    nodePokemonBanc.getStyleClass().removeAll("pokemon-selectionne");
                }
            }
        }
    }
}