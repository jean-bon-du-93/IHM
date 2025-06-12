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
import javafx.scene.image.ImageView; // Added for image display
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte; // Added import
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type; // Added for energy icons
import javafx.event.ActionEvent; // Added import
import javafx.scene.layout.HBox;
// import javafx.scene.layout.Region; // For placeholders or card backs - ImageView will be used
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List; // For energieProperty value type
import java.util.Map; // For iterating energy map

public class VueAdversaire extends VBox {
    private static final int MAX_BENCH_SLOTS = 5; // Matching VueJoueurActif

    // Constants for image sizes
    private static final double LARGEUR_PKMN_ACTIF_ADV = 150;
    private static final double HAUTEUR_PKMN_ACTIF_ADV = 225;
    private static final double LARGEUR_PKMN_BANC_ADV = 100;
    private static final double HAUTEUR_PKMN_BANC_ADV = 140;
    private static final double LARGEUR_CARTE_MAIN_ADV = 150;
    private static final double HAUTEUR_CARTE_MAIN_ADV = 225;
    private static final double TAILLE_ICONE_ENERGIE_ADV = 18;
    private static final double LARGEUR_DOS_PETIT = 30;
    private static final double HAUTEUR_DOS_PETIT = 42;

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
    @FXML ImageView deckAdversaireImageView; // Added FXML field
    @FXML Label defausseAdversaireLabel;
    @FXML Label prixAdversaireLabel;
    @FXML ImageView prixAdversaireImageView; // Added FXML field

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
            if (deckAdversaireImageView != null) deckAdversaireImageView.setVisible(false); // Hide if no adversary
            if (defausseAdversaireLabel != null) defausseAdversaireLabel.setText("Défausse Adv.: N/A");
            if (prixAdversaireLabel != null) prixAdversaireLabel.setText("Prix Adv.: N/A");
            if (prixAdversaireImageView != null) prixAdversaireImageView.setVisible(false); // Hide if no adversary
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

        // Set up images for deck and prize card backs
        if (deckAdversaireImageView != null) {
            deckAdversaireImageView.setImage(VueUtils.creerImageViewPourDosCarte(LARGEUR_DOS_PETIT, HAUTEUR_DOS_PETIT).getImage());
        }
        if (prixAdversaireImageView != null) {
            prixAdversaireImageView.setImage(VueUtils.creerImageViewPourDosCarte(LARGEUR_DOS_PETIT, HAUTEUR_DOS_PETIT).getImage());
        }

        placerPokemonActifAdversaire();
        placerBancAdversaire();
        placerMainAdversaire(); // Initial placement of hand representation
        mettreAJourComptesCartesAdversaire(); // Initial update of counts and image visibility

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

        if (opponentPokemonActifButton != null) {
            if (pkmnActif != null && pkmnActif.getCartePokemon() != null) {
                ImageView imageView = VueUtils.creerImageViewPourCarte(pkmnActif.getCartePokemon(), LARGEUR_PKMN_ACTIF_ADV, HAUTEUR_PKMN_ACTIF_ADV);
                opponentPokemonActifButton.setGraphic(imageView);
                opponentPokemonActifButton.setText(null); // Remove text
                // Tooltip tooltip = new Tooltip(pkmnActif.getCartePokemon().getNom());
                // opponentPokemonActifButton.setTooltip(tooltip);
            } else {
                // Display card back or clear
                opponentPokemonActifButton.setGraphic(VueUtils.creerImageViewPourDosCarte(LARGEUR_PKMN_ACTIF_ADV, HAUTEUR_PKMN_ACTIF_ADV));
                opponentPokemonActifButton.setText(null);
            }
        }
        rafraichirEnergiePokemonActifAdversaire();
    }

    private void rafraichirEnergiePokemonActifAdversaire() {
        if (energiePokemonActifAdversaireHBox == null) return;
        energiePokemonActifAdversaireHBox.getChildren().clear();
        IPokemon pkmnActif = (this.adversaire != null && this.adversaire.pokemonActifProperty() != null) ? this.adversaire.pokemonActifProperty().get() : null;
        if (pkmnActif != null) {
            ObservableMap<String, List<String>> energieMap = pkmnActif.energieProperty();
            if (energieMap != null) {
                for (Map.Entry<String, List<String>> entry : energieMap.entrySet()) {
                    String typeLetter = entry.getKey();
                    int count = entry.getValue().size();
                    if (count > 0) {
                        Type typeEnum = null;
                        for (Type t : Type.values()) {
                            if (t.asLetter().equals(typeLetter)) {
                                typeEnum = t;
                                break;
                            }
                        }
                        if (typeEnum != null) {
                            ImageView iconeEnergie = VueUtils.creerImageViewPourIconeEnergie(typeEnum, TAILLE_ICONE_ENERGIE_ADV);
                            Label countLabel = new Label("x" + count);
                            HBox energyEntryBox = new HBox(iconeEnergie, countLabel);
                            energyEntryBox.setSpacing(2);
                            energiePokemonActifAdversaireHBox.getChildren().add(energyEntryBox);
                        } else { // Fallback for unknown type letters
                            Label energyLabel = new Label(typeLetter + " x" + count);
                            energyLabel.getStyleClass().add("energy-tag");
                            energiePokemonActifAdversaireHBox.getChildren().add(energyLabel);
                        }
                    }
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

        ImageView imageViewPkmnBanc = VueUtils.creerImageViewPourCarte(pokemon.getCartePokemon(), LARGEUR_PKMN_BANC_ADV, HAUTEUR_PKMN_BANC_ADV);
        Button pokemonButton = new Button();
        pokemonButton.setGraphic(imageViewPkmnBanc);
        pokemonButton.getStyleClass().clear(); // Remove default button styling
        pokemonButton.getStyleClass().add("card-button-on-bench"); // Add custom class for styling
        // Tooltip tooltip = new Tooltip(pokemon.getCartePokemon().getNom());
        // pokemonButton.setTooltip(tooltip);

        pokemonButton.setOnAction(actionEvent -> {
            if (this.jeu != null && pokemon != null && pokemon.getCartePokemon() != null && pokemon.getCartePokemon().getId() != null) {
                this.jeu.carteSurTerrainCliquee(pokemon.getCartePokemon().getId());
            } else {
                System.err.println("Clic sur Pokémon de banc adverse, mais pas de Pokémon/carte/ID trouvé.");
            }
        });

        HBox energieHBox = new HBox(2);
        energieHBox.setAlignment(Pos.CENTER);
        ObservableMap<String, List<String>> energieMap = pokemon.energieProperty();
        if (energieMap != null) {
            for (Map.Entry<String, List<String>> entry : energieMap.entrySet()) {
                 String typeLetter = entry.getKey();
                 int count = entry.getValue().size();
                 if (count > 0) {
                     Type typeEnum = null;
                     for (Type t : Type.values()) {
                         if (t.asLetter().equals(typeLetter)) {
                             typeEnum = t;
                             break;
                         }
                     }
                     if (typeEnum != null) {
                         ImageView iconeEnergie = VueUtils.creerImageViewPourIconeEnergie(typeEnum, TAILLE_ICONE_ENERGIE_ADV);
                         Label countLabel = new Label("x" + count);
                         HBox energyEntryBox = new HBox(iconeEnergie, countLabel);
                         energyEntryBox.setSpacing(2);
                         energieHBox.getChildren().add(energyEntryBox);
                     } else {
                        Label energyLabel = new Label(typeLetter + " x" + count);
                        energyLabel.getStyleClass().add("energy-tag");
                        energieHBox.getChildren().add(energyLabel);
                     }
                 }
            }
        }
        pokemonCardContainer.getChildren().addAll(pokemonButton, energieHBox);

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
                ImageView dosCarteView = VueUtils.creerImageViewPourDosCarte(LARGEUR_CARTE_MAIN_ADV, HAUTEUR_CARTE_MAIN_ADV);
                panneauMainAdversaireHBox.getChildren().add(dosCarteView);
            }
        }
    }

    private void mettreAJourComptesCartesAdversaire() {
        if (adversaire == null) {
            // Labels are already set to "N/A" or similar in initialiserAffichage if adversaire is null.
            // Ensure images are hidden too.
            if (deckAdversaireImageView != null) deckAdversaireImageView.setVisible(false);
            if (prixAdversaireImageView != null) prixAdversaireImageView.setVisible(false);
            return;
        }

        mainAdversaireLabel.setText("Main Adv.: " + (adversaire.getMain() != null ? adversaire.getMain().size() : "0"));

        int taillePioche = (adversaire.piocheProperty() != null) ? adversaire.piocheProperty().size() : 0;
        deckAdversaireLabel.setText("Deck Adv.: " + taillePioche);
        if (deckAdversaireImageView != null) {
            deckAdversaireImageView.setVisible(taillePioche > 0);
        }

        defausseAdversaireLabel.setText("Défausse Adv.: " + (adversaire.defausseProperty() != null ? adversaire.defausseProperty().size() : "0"));

        int taillePrix = (adversaire.recompensesProperty() != null) ? adversaire.recompensesProperty().size() : 0;
        prixAdversaireLabel.setText("Prix Adv.: " + taillePrix);
        if (prixAdversaireImageView != null) {
            prixAdversaireImageView.setVisible(taillePrix > 0);
        }
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
