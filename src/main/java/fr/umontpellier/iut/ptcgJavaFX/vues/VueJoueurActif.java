package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemon; // Ensure this is uncommented or present
import javafx.beans.binding.Bindings; // Added import
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
import javafx.scene.image.ImageView; // Added for image display
import javafx.scene.layout.FlowPane; // Added for attaquesPane
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node; // Added for mettreAJourStyleSelectionPokemon
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte; // Added for mettreAJourStyleSelectionPokemon
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.Attaque; // Added for attaques
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type; // Added for attaque cost display
import fr.umontpellier.iut.ptcgJavaFX.vues.VueUtils; // Added for image utilities

import java.io.IOException;
import java.util.ArrayList; // Added for listener cleanup
import java.util.HashMap; // Added for new Maps
import java.util.List; // Added for type in MapChangeListener
import java.util.Map; // Added for new Maps

public class VueJoueurActif extends VBox {

    private static final int MAX_BENCH_SLOTS = 5; // Added constant

    // Constants for image sizes
    private static final double LARGEUR_CARTE_MAIN = 150;
    private static final double HAUTEUR_CARTE_MAIN = 225;
    private static final double LARGEUR_PKMN_ACTIF = 150;
    private static final double HAUTEUR_PKMN_ACTIF = 225;
    private static final double LARGEUR_PKMN_BANC = 100;
    private static final double HAUTEUR_PKMN_BANC = 140;
    private static final double TAILLE_ICONE_ENERGIE = 18;
    private static final double LARGEUR_DOS_PIOCHE_RECOMPENSE = 45;
    private static final double HAUTEUR_DOS_PIOCHE_RECOMPENSE = 63;

    private IJeu jeu;
    private ObjectProperty<IJoueur> joueurActifProperty;
    @FXML
    Label nomDuJoueurLabel;
    @FXML
    Button pokemonActifButton;
    @FXML
    HBox energiePokemonActifHBox; // Added
    @FXML
    private VBox pokemonActifVBox; // Added for HP display
    @FXML
    HBox panneauMainHBox;
    @FXML
    HBox panneauBancHBox;
    @FXML
    private FlowPane attaquesPane; // Added for displaying attacks
    @FXML
    private ImageView piocheJoueurActifImageView;
    @FXML
    private Label piocheJoueurActifLabel;
    @FXML
    private ImageView recompensesJoueurActifImageView;
    @FXML
    private Label recompensesJoueurActifLabel;

    private ChangeListener<IJoueur> joueurActifGlobalChangeListener;
    private ChangeListener<IPokemon> pokemonDuJoueurActifChangeListener;
    private ListChangeListener<String> attaquesActivesListener; // Added listener for active pokemon's attacks
    private ListChangeListener<ICarte> mainDuJoueurActifChangeListener;
    private ListChangeListener<IPokemon> changementBancJoueur;
    private MapChangeListener<String, List<String>> energiePokemonActifListener; // For active Pokemon
    private ListChangeListener<ICarte> piocheListener; // Listener for player's deck
    private ListChangeListener<ICarte> recompensesListener; // Listener for player's prize cards

    // Fields for benched Pokemon energy listeners
    private final Map<IPokemon, MapChangeListener<String, List<String>>> benchEnergyListeners = new HashMap<>();
    private final Map<IPokemon, HBox> benchPokemonEnergyUI = new HashMap<>();
    // private final Map<IPokemon, ChangeListener<CartePokemon>> benchPokemonCardListeners = new HashMap<>(); // RETIRÉ
    // private final Map<IPokemon, Button> benchPokemonButtons = new HashMap<>(); // RETIRÉ

    private boolean isChoosingNewActivePokemon = false; // Added state variable
    private static final String CLICKABLE_ENERGY_STYLE_CLASS = "clickable-energy-icon";

    @FXML
    Button passerButton;
    @FXML
    private Button retreatButton; // Added for retreat functionality

    // private VueEnergieSelection vueEnergieSelection; // REMOVED

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

        // Add listener for instruction changes
        if (this.jeu != null && this.jeu.instructionProperty() != null) {
            this.jeu.instructionProperty().addListener((obs, oldInstruction, newInstruction) -> {
                handleInstructionChange(newInstruction);
            });
            // Process initial instruction
            handleInstructionChange(this.jeu.instructionProperty().get());
        }

        // Setup action for the retreat button
        if (retreatButton != null) {
            retreatButton.setOnAction(event -> {
                if (this.jeu != null) {
                    this.jeu.retraiteAEteChoisie();
                }
            });
        }

        // Initialize deck and prize card images
        if (piocheJoueurActifImageView != null) {
            piocheJoueurActifImageView.setImage(VueUtils.creerImageViewPourDosCarte(LARGEUR_DOS_PIOCHE_RECOMPENSE, HAUTEUR_DOS_PIOCHE_RECOMPENSE).getImage());
            piocheJoueurActifImageView.setVisible(false); // Initially hidden, listener will update
        }
        if (recompensesJoueurActifImageView != null) {
            recompensesJoueurActifImageView.setImage(VueUtils.creerImageViewPourDosCarte(LARGEUR_DOS_PIOCHE_RECOMPENSE, HAUTEUR_DOS_PIOCHE_RECOMPENSE).getImage());
            recompensesJoueurActifImageView.setVisible(false); // Initially hidden, listener will update
        }

        // Load VueEnergieSelection - REMOVED
        // try {
        //     FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VueEnergieSelection.fxml"));
        //     VBox energySelectionNodeAsVBox = loader.load();
        //     vueEnergieSelection = loader.getController();

        //     this.getChildren().add(energySelectionNodeAsVBox);
        //     vueEnergieSelection.setPaneVisible(false);
        // } catch (java.io.IOException e) {
        //     throw new RuntimeException("Failed to load VueEnergieSelection.fxml", e);
        // }
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

        this.pokemonDuJoueurActifChangeListener = (obsPokemon, oldPkmn, newPkmn) -> {
            if (oldPkmn != null) {
                if (oldPkmn.energieProperty() != null && this.energiePokemonActifListener != null) {
                    oldPkmn.energieProperty().removeListener(this.energiePokemonActifListener);
                }
                if (oldPkmn.attaquesProperty() != null && this.attaquesActivesListener != null) { // Attaques listener detach
                    oldPkmn.attaquesProperty().removeListener(this.attaquesActivesListener);
                }
            }

            placerPokemonActif();
            // afficherAttaquesJouables(); // Will be called by attacksProperty listener or explicitly after this block

            if (newPkmn != null) {
                if (newPkmn.energieProperty() != null && this.energiePokemonActifListener != null) {
                    newPkmn.energieProperty().addListener(this.energiePokemonActifListener);
                }
                if (newPkmn.attaquesProperty() != null && this.attaquesActivesListener != null) { // Attaques listener attach
                    newPkmn.attaquesProperty().addListener(this.attaquesActivesListener);
                }
            } else {
                if (energiePokemonActifHBox != null) energiePokemonActifHBox.getChildren().clear();
                if (attaquesPane != null) attaquesPane.getChildren().clear();
            }
            afficherAttaquesJouables(); // Display/update attacks for newPkmn (or clear if null)
        };

        this.attaquesActivesListener = (ListChangeListener.Change<? extends String> change) -> {
            afficherAttaquesJouables();
        };

        this.mainDuJoueurActifChangeListener = (ListChangeListener.Change<? extends ICarte> c) -> {
            // Call new method that processes the Change object
            updatePanneauMain(c);
        };

        this.changementBancJoueur = (ListChangeListener.Change<? extends IPokemon> c) -> {
            updatePanneauBanc(c);
        };

        this.piocheListener = change -> {
            IJoueur joueurCourant = joueurActifProperty.get();
            if (joueurCourant != null && piocheJoueurActifLabel != null && piocheJoueurActifImageView != null) {
                int taillePioche = joueurCourant.piocheProperty().size();
                piocheJoueurActifLabel.setText("P: " + taillePioche);
                piocheJoueurActifImageView.setVisible(taillePioche > 0);
            } else if (piocheJoueurActifLabel != null && piocheJoueurActifImageView != null) {
                piocheJoueurActifLabel.setText("P: 0");
                piocheJoueurActifImageView.setVisible(false);
            }
        };
        this.recompensesListener = change -> {
            IJoueur joueurCourant = joueurActifProperty.get();
            if (joueurCourant != null && recompensesJoueurActifLabel != null && recompensesJoueurActifImageView != null) {
                int tailleRecompenses = joueurCourant.recompensesProperty().size();
                recompensesJoueurActifLabel.setText("R: " + tailleRecompenses);
                recompensesJoueurActifImageView.setVisible(tailleRecompenses > 0);
            } else if (recompensesJoueurActifLabel != null && recompensesJoueurActifImageView != null) {
                recompensesJoueurActifLabel.setText("R: 0");
                recompensesJoueurActifImageView.setVisible(false);
            }
        };


        this.joueurActifGlobalChangeListener = (observable, oldJoueur, newJoueur) -> {
            // Detach all listeners from oldJoueur and its properties
            if (oldJoueur != null) {
                ObjectProperty<? extends IPokemon> oldActivePokemonProp = oldJoueur.pokemonActifProperty();
                if (oldActivePokemonProp != null) {
                    oldActivePokemonProp.removeListener(this.pokemonDuJoueurActifChangeListener);
                    IPokemon oldActivePkmnInstance = oldActivePokemonProp.get();
                    if (oldActivePkmnInstance != null) {
                        if (oldActivePkmnInstance.energieProperty() != null && this.energiePokemonActifListener != null) {
                            oldActivePkmnInstance.energieProperty().removeListener(this.energiePokemonActifListener);
                        }
                        if (oldActivePkmnInstance.attaquesProperty() != null && this.attaquesActivesListener != null) {
                           oldActivePkmnInstance.attaquesProperty().removeListener(this.attaquesActivesListener);
                        }
                    }
                }
                if (oldJoueur.getMain() != null) {
                    oldJoueur.getMain().removeListener(this.mainDuJoueurActifChangeListener);
                }
                if (oldJoueur.getBanc() != null) {
                    oldJoueur.getBanc().removeListener(this.changementBancJoueur);
                }
                if (oldJoueur.piocheProperty() != null && this.piocheListener != null) {
                    oldJoueur.piocheProperty().removeListener(this.piocheListener);
                }
                if (oldJoueur.recompensesProperty() != null && this.recompensesListener != null) {
                    oldJoueur.recompensesProperty().removeListener(this.recompensesListener);
                }
                // Unbind retreat button from old player - REMOVED as per instruction
                // if (retreatButton != null) {
                //     retreatButton.disableProperty().unbind();
                // }
            }

            // Update displays for newJoueur
            placerPokemonActif();
            reconstruirePanneauMainComplet();
            reconstruirePanneauBancComplet();
            afficherAttaquesJouables();

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
                        if (newActivePkmnInstance.attaquesProperty() != null && this.attaquesActivesListener != null) { // Attach attaque listener
                            newActivePkmnInstance.attaquesProperty().addListener(this.attaquesActivesListener);
                        }
                    }
                }
                if (newJoueur.getMain() != null) {
                    newJoueur.getMain().addListener(this.mainDuJoueurActifChangeListener);
                }
                if (newJoueur.getBanc() != null) {
                    newJoueur.getBanc().addListener(this.changementBancJoueur);
                }
                if (newJoueur.piocheProperty() != null && this.piocheListener != null) {
                    newJoueur.piocheProperty().addListener(this.piocheListener);
                    // Initial update for deck
                    int taillePioche = newJoueur.piocheProperty().size();
                    if (piocheJoueurActifLabel != null) piocheJoueurActifLabel.setText("P: " + taillePioche);
                    if (piocheJoueurActifImageView != null) piocheJoueurActifImageView.setVisible(taillePioche > 0);
                }
                if (newJoueur.recompensesProperty() != null && this.recompensesListener != null) {
                    newJoueur.recompensesProperty().addListener(this.recompensesListener);
                    // Initial update for prizes
                    int tailleRecompenses = newJoueur.recompensesProperty().size();
                    if (recompensesJoueurActifLabel != null) recompensesJoueurActifLabel.setText("R: " + tailleRecompenses);
                    if (recompensesJoueurActifImageView != null) recompensesJoueurActifImageView.setVisible(tailleRecompenses > 0);
                }
                // Bind retreat button to new player's peutRetraiteProperty - REMOVED as per instruction
                // if (newJoueur.peutRetraiteProperty() != null && retreatButton != null) {
                //     retreatButton.disableProperty().bind(newJoueur.peutRetraiteProperty().not());
                // } else if (retreatButton != null) {
                //     retreatButton.disableProperty().unbind();
                //     retreatButton.setDisable(true);
                // }

            } else { // No new player (e.g. game end), clear displays
                if (energiePokemonActifHBox != null) energiePokemonActifHBox.getChildren().clear();
                if (attaquesPane != null) attaquesPane.getChildren().clear();
                if (piocheJoueurActifLabel != null) piocheJoueurActifLabel.setText("P: 0");
                if (piocheJoueurActifImageView != null) piocheJoueurActifImageView.setVisible(false);
                if (recompensesJoueurActifLabel != null) recompensesJoueurActifLabel.setText("R: 0");
                if (recompensesJoueurActifImageView != null) recompensesJoueurActifImageView.setVisible(false);
                // Disable retreat button if no active player - REMOVED as per instruction
                // if (retreatButton != null) {
                //    retreatButton.disableProperty().unbind();
                //    retreatButton.setDisable(true);
                // }
            }
            // Call updateUserInteractivity whenever the active player changes
            updateUserInteractivity();
        };
        this.joueurActifProperty.addListener(this.joueurActifGlobalChangeListener);
    }

    public void placerPokemonActif() {
        IPokemon currentActivePokemon = null;
        IJoueur joueurCourant = (joueurActifProperty != null) ? joueurActifProperty.get() : null;

        if (joueurCourant != null && joueurCourant.pokemonActifProperty() != null) {
            currentActivePokemon = joueurCourant.pokemonActifProperty().get();
        }

        // Remove existing HP label if present
        if (pokemonActifVBox != null) {
            pokemonActifVBox.getChildren().removeIf(node -> "hpLabelActif".equals(node.getId()));
            pokemonActifVBox.getChildren().removeIf(node -> "weaknessLabelActif".equals(node.getId()));
            pokemonActifVBox.getChildren().removeIf(node -> "resistanceLabelActif".equals(node.getId()));
            pokemonActifVBox.getChildren().removeIf(node -> "retreatLabelActif".equals(node.getId()));
            pokemonActifVBox.getChildren().removeIf(node -> "statusBoxActif".equals(node.getId())); // Remove status HBox
        }

        if (pokemonActifButton != null) {
            if (currentActivePokemon != null && currentActivePokemon.getCartePokemon() != null) {
                ImageView imageView = VueUtils.creerImageViewPourCarte(currentActivePokemon.getCartePokemon(), LARGEUR_PKMN_ACTIF, HAUTEUR_PKMN_ACTIF);
                pokemonActifButton.setGraphic(imageView);
                pokemonActifButton.setText(null); // Remove text if image is present

                // Add HP Label
                if (pokemonActifVBox != null) {
                    Label hpLabel = new Label();
                    hpLabel.setId("hpLabelActif"); // For future removal
                    hpLabel.getStyleClass().add("hp-label"); // Add style class
                    // Final variable for use in lambda expression
                    final IPokemon pokemonForBinding = currentActivePokemon;
                    // Bind HP text property to the pointsDeVieProperty of the pokemonForBinding
                    hpLabel.textProperty().bind(
                        Bindings.createStringBinding(
                            () -> "HP: " + pokemonForBinding.pointsDeVieProperty().get(),
                            pokemonForBinding.pointsDeVieProperty() // Dependency
                        )
                    );
                    // Add HP label at index 1 (after button, before energy HBox)
                    // Ensure there's at least one child (the button) before trying to add at index 1
                    if (pokemonActifVBox.getChildren().size() >= 1 && energiePokemonActifHBox != null) {
                         // Find index of energiePokemonActifHBox and insert before it
                        int energyBoxIndex = pokemonActifVBox.getChildren().indexOf(energiePokemonActifHBox);
                        if (energyBoxIndex != -1) {
                            pokemonActifVBox.getChildren().add(energyBoxIndex, hpLabel);
                        } else {
                            // Fallback: if energy box not found (e.g. not added yet), add after button or at end
                             int buttonIndex = pokemonActifVBox.getChildren().indexOf(pokemonActifButton);
                             if (buttonIndex != -1 && buttonIndex + 1 <= pokemonActifVBox.getChildren().size()) {
                                 pokemonActifVBox.getChildren().add(buttonIndex + 1, hpLabel);
                             } else {
                                 pokemonActifVBox.getChildren().add(hpLabel);
                             }
                        }
                    } else if (pokemonActifVBox.getChildren().contains(pokemonActifButton)) {
                        // Only button is present, add HP label after it
                        int buttonIndex = pokemonActifVBox.getChildren().indexOf(pokemonActifButton);
                        pokemonActifVBox.getChildren().add(buttonIndex + 1, hpLabel);
                    }
                    else {
                        pokemonActifVBox.getChildren().add(hpLabel); // Add as first element if VBox is empty or button not found
                    }

                    // Get ICarte for additional properties
                    ICarte carte = pokemonForBinding.getCartePokemon();

                    // Weakness Display
                    fr.umontpellier.iut.ptcgJavaFX.mecanique.Type faiblesseType = carte.getFaiblesse();
                    Label weaknessLabel = new Label();
                    weaknessLabel.setId("weaknessLabelActif");
                    weaknessLabel.getStyleClass().add("hp-label"); // Using same style for now
                    if (faiblesseType != null) {
                        weaknessLabel.setText("Weakness: " + faiblesseType.name());
                    } else {
                        weaknessLabel.setText("Weakness: None");
                    }
                    pokemonActifVBox.getChildren().add(weaknessLabel);

                    // Resistance Display
                    fr.umontpellier.iut.ptcgJavaFX.mecanique.Type resistanceType = carte.getResistance();
                    Label resistanceLabel = new Label();
                    resistanceLabel.setId("resistanceLabelActif");
                    resistanceLabel.getStyleClass().add("hp-label"); // Using same style for now
                    if (resistanceType != null) {
                        resistanceLabel.setText("Resistance: " + resistanceType.name());
                    } else {
                        resistanceLabel.setText("Resistance: None");
                    }
                    pokemonActifVBox.getChildren().add(resistanceLabel);

                    // Retreat Cost Display
                    int retreatCost = carte.getCoutRetraite();
                    Label retreatLabel = new Label();
                    retreatLabel.setId("retreatLabelActif");
                    retreatLabel.getStyleClass().add("hp-label"); // Using same style for now
                    retreatLabel.setText("Retreat: " + retreatCost);
                    pokemonActifVBox.getChildren().add(retreatLabel);

                    // Status Conditions Display
                    HBox statusConditionsHBox = new HBox();
                    statusConditionsHBox.setId("statusBoxActif");
                    statusConditionsHBox.setSpacing(5);

                    // Burned
                    Label brnLabel = new Label("BRN");
                    brnLabel.getStyleClass().add("status-label");
                    brnLabel.visibleProperty().bind(pokemonForBinding.estBruleProperty());
                    statusConditionsHBox.getChildren().add(brnLabel);

                    // Poisoned
                    Label psnLabel = new Label("PSN");
                    psnLabel.getStyleClass().add("status-label");
                    psnLabel.visibleProperty().bind(pokemonForBinding.estEmpoisonneProperty());
                    statusConditionsHBox.getChildren().add(psnLabel);

                    // Asleep
                    Label slpLabel = new Label("SLP");
                    slpLabel.getStyleClass().add("status-label");
                    slpLabel.visibleProperty().bind(pokemonForBinding.estEndormiProperty());
                    statusConditionsHBox.getChildren().add(slpLabel);

                    // Paralyzed
                    Label parLabel = new Label("PAR");
                    parLabel.getStyleClass().add("status-label");
                    parLabel.visibleProperty().bind(pokemonForBinding.estParalyseProperty());
                    statusConditionsHBox.getChildren().add(parLabel);

                    // Confused
                    Label cnfLabel = new Label("CNF");
                    cnfLabel.getStyleClass().add("status-label");
                    cnfLabel.visibleProperty().bind(pokemonForBinding.estConfusProperty());
                    statusConditionsHBox.getChildren().add(cnfLabel);

                    pokemonActifVBox.getChildren().add(statusConditionsHBox);
                }
            } else {
                pokemonActifButton.setGraphic(VueUtils.creerImageViewPourDosCarte(LARGEUR_PKMN_ACTIF, HAUTEUR_PKMN_ACTIF)); // Show card back
                pokemonActifButton.setText(null);
                // Ensure HP label is also cleared if no active Pokemon
                if (pokemonActifVBox != null) {
                    pokemonActifVBox.getChildren().removeIf(node -> "hpLabelActif".equals(node.getId()));
                }
            }
        }
        populateActivePokemonEnergy(currentActivePokemon);
    }

    private void populateActivePokemonEnergy(IPokemon activePokemon) {
        if (energiePokemonActifHBox != null) {
            energiePokemonActifHBox.getChildren().clear();
            if (activePokemon != null && activePokemon.cartesProperty() != null) {
                for (ICarte iCarte : activePokemon.cartesProperty()) {
                    Carte concreteCarte = Carte.get(iCarte.getId());
                    if (concreteCarte != null && concreteCarte.getTypeEnergie() != null) {
                        ImageView energyIcon = VueUtils.creerImageViewPourIconeEnergie(concreteCarte.getTypeEnergie(), TAILLE_ICONE_ENERGIE);
                        energyIcon.setUserData(concreteCarte.getId());
                        // Potentially add click handler or tooltip if needed in the future
                        energiePokemonActifHBox.getChildren().add(energyIcon);
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
        ImageView imageView = VueUtils.creerImageViewPourCarte(carte, LARGEUR_CARTE_MAIN, HAUTEUR_CARTE_MAIN);
        Button boutonCarte = new Button();
        boutonCarte.setGraphic(imageView);
        boutonCarte.setUserData(carte); // Keep card data for logic
        boutonCarte.getStyleClass().clear(); // Remove default button styling
        boutonCarte.getStyleClass().add("card-button-in-hand"); // Add custom class for styling (e.g., transparent background)
        // Example of inline style if not using CSS class:
        // boutonCarte.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

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
        ImageView imageViewPkmnBanc = VueUtils.creerImageViewPourCarte(pokemon.getCartePokemon(), LARGEUR_PKMN_BANC, HAUTEUR_PKMN_BANC);
        Button boutonPokemonBanc = new Button();
        boutonPokemonBanc.setGraphic(imageViewPkmnBanc);
        boutonPokemonBanc.getStyleClass().clear();
        boutonPokemonBanc.getStyleClass().add("card-button-on-bench");
        // Optionally, set tooltip for name:
        // Tooltip tooltip = new Tooltip(pokemon.getCartePokemon().getNom());
        // boutonPokemonBanc.setTooltip(tooltip);

        boutonPokemonBanc.setOnAction(event -> {
            if (this.jeu != null && pokemon != null && pokemon.getCartePokemon() != null && pokemon.getCartePokemon().getId() != null) {
                String idCarteCliquee = pokemon.getCartePokemon().getId();
                if (isChoosingNewActivePokemon) {
                    // As per correction request, call uneCarteDeLaMainAEteChoisie for promotion.
                    this.jeu.uneCarteDeLaMainAEteChoisie(idCarteCliquee);
                } else {
                    this.jeu.carteSurTerrainCliquee(idCarteCliquee); // Original logic
                    IJoueur joueurCourant = VueJoueurActif.this.joueurActifProperty.get();
                    if (joueurCourant != null && joueurCourant.carteEnJeuProperty().get() != null) {
                        this.jeu.uneCarteComplementaireAEteChoisie(idCarteCliquee);
                    }
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

        // HP Label for benched Pokemon
        Label hpLabel = new Label();
        hpLabel.getStyleClass().add("hp-label"); // Add style class
        // Bind HP Label text
        hpLabel.textProperty().bind(
            Bindings.createStringBinding(() -> {
                if (pokemon != null && pokemon.getCartePokemon() != null) { // Ensure pokemon and its card are not null
                    return "HP: " + pokemon.pointsDeVieProperty().get();
                }
                return "HP: --";
            }, pokemon.pointsDeVieProperty(), pokemon.cartePokemonProperty()) // Observe these for changes
        );

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

        pokemonCardContainer.getChildren().addAll(boutonPokemonBanc, hpLabel, energieBancPokemonHBox);
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
        if (pokemon.cartesProperty() != null) {
            for (ICarte iCarte : pokemon.cartesProperty()) {
                Carte concreteCarte = Carte.get(iCarte.getId());
                if (concreteCarte != null && concreteCarte.getTypeEnergie() != null) {
                    ImageView energyIcon = VueUtils.creerImageViewPourIconeEnergie(concreteCarte.getTypeEnergie(), TAILLE_ICONE_ENERGIE);
                    energyIcon.setUserData(concreteCarte.getId());
                    // Potentially add click handler or tooltip if needed in the future
                    energyHBoxContainer.getChildren().add(energyIcon);
                }
            }
        }
    }

    private void afficherAttaquesJouables() {
       if (attaquesPane == null) {
           return;
       }
       attaquesPane.getChildren().clear();
       IJoueur joueurCourant = (joueurActifProperty != null) ? joueurActifProperty.get() : null;

       if (joueurCourant == null || joueurCourant.pokemonActifProperty() == null || joueurCourant.pokemonActifProperty().get() == null) {
           return;
       }

       IPokemon pokemonActif = joueurCourant.pokemonActifProperty().get();
       if (pokemonActif.getCartePokemon() == null) { // Pokemon instance exists, but no card (e.g. placeholder)
            return;
       }

       List<Attaque> toutesLesAttaquesDeLaCarte = ((fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemon) pokemonActif.getCartePokemon()).getAttaques();
       ObservableList<String> nomsAttaquesJouables = pokemonActif.attaquesProperty(); // These are the ones that can be used

       for (String nomAttaqueJouable : nomsAttaquesJouables) {
           Attaque attaqueComplete = null;
           for (Attaque atk : toutesLesAttaquesDeLaCarte) {
               if (atk.getNom().equals(nomAttaqueJouable)) {
                   attaqueComplete = atk;
                   break;
               }
           }

           if (attaqueComplete != null) {
               StringBuilder coutStr = new StringBuilder();
               if (attaqueComplete.getCoutEnergie().isEmpty()){
                   coutStr.append("(Coût: 0)");
               } else {
                   coutStr.append("(Coût: ");
                   boolean first = true;
                   for (Map.Entry<fr.umontpellier.iut.ptcgJavaFX.mecanique.Type, Integer> entry : attaqueComplete.getCoutEnergie().entrySet()) {
                       if (!first) coutStr.append(", ");
                       coutStr.append(entry.getKey().asLetter()).append(":").append(entry.getValue());
                       first = false;
                   }
                   coutStr.append(")");
               }

               Button boutonAttaque = new Button(nomAttaqueJouable + " " + coutStr.toString());
               boutonAttaque.getStyleClass().add("attack-button");
               final String finalNomAttaque = nomAttaqueJouable; // For lambda
               boutonAttaque.setOnAction(event -> {
                   if (this.jeu != null) {
                       this.jeu.uneAttaqueAEteChoisie(finalNomAttaque);
                   }
               });
               // boutonAttaque.setDisable(!peutAttaquerCeTour); // Game logic will handle if attack is allowed via states
               attaquesPane.getChildren().add(boutonAttaque);
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

    private boolean isEnergySelectionInstruction(String instruction) {
        if (instruction == null) return false;
        String lowerInstruction = instruction.toLowerCase();
        // Covers "Défaussez une énergie..." and "Défaussez X énergie(s)"
        return lowerInstruction.startsWith("défaussez") && lowerInstruction.contains("énergie");
    }

    private void handleInstructionChange(String newInstruction) {
        isChoosingNewActivePokemon = "Choisissez un nouveau pokémon actif.".equals(newInstruction);

        boolean isEnergyInstruction = isEnergySelectionInstruction(newInstruction);

        if (isEnergyInstruction) {
            // Make energies on active Pokemon clickable
            if (energiePokemonActifHBox != null) {
                for (Node node : energiePokemonActifHBox.getChildren()) {
                    if (node instanceof ImageView) {
                        ImageView imageView = (ImageView) node;
                        // Clear previous handler to be safe, though populate should recreate ImageViews
                        imageView.setOnMouseClicked(null);
                        imageView.setOnMouseClicked(event -> {
                            String cardId = (String) imageView.getUserData();
                            if (this.jeu != null && cardId != null) {
                                this.jeu.uneCarteEnergieAEteChoisie(cardId);
                            }
                        });
                        if (!imageView.getStyleClass().contains(CLICKABLE_ENERGY_STYLE_CLASS)) {
                            imageView.getStyleClass().add(CLICKABLE_ENERGY_STYLE_CLASS);
                        }
                    }
                }
            }
        } else {
            // Remove click handlers and style from active Pokemon's energies if not selecting
            if (energiePokemonActifHBox != null) {
                for (Node node : energiePokemonActifHBox.getChildren()) {
                    if (node instanceof ImageView) {
                        ImageView imageView = (ImageView) node;
                        imageView.setOnMouseClicked(null);
                        imageView.getStyleClass().remove(CLICKABLE_ENERGY_STYLE_CLASS);
                    }
                }
            }
        }
        updateUserInteractivity();
    }

    private void updateUserInteractivity() {
        // boolean energySelectionIsActive = (vueEnergieSelection != null && vueEnergieSelection.isVisible()); // REMOVED
        boolean instructionIsForEnergySelection = false;
        if (this.jeu != null && this.jeu.instructionProperty() != null) {
             instructionIsForEnergySelection = isEnergySelectionInstruction(this.jeu.instructionProperty().get());
        }
        boolean disableDueToPokemonChoice = isChoosingNewActivePokemon;

        if (panneauMainHBox != null) {
            panneauMainHBox.setDisable(disableDueToPokemonChoice || instructionIsForEnergySelection);
        }
        if (pokemonActifButton != null) {
            pokemonActifButton.setDisable(disableDueToPokemonChoice || instructionIsForEnergySelection);
        }
        if (attaquesPane != null) {
            attaquesPane.setDisable(disableDueToPokemonChoice || instructionIsForEnergySelection);
        }
        if (passerButton != null) {
            passerButton.setDisable(disableDueToPokemonChoice || instructionIsForEnergySelection);
        }

        if (retreatButton != null) {
            IJoueur joueurCourant = (joueurActifProperty != null) ? joueurActifProperty.get() : null;
            boolean canPlayerCurrentlyRetreat = false;
            if (joueurCourant != null && joueurCourant.peutRetraiteProperty() != null) {
                canPlayerCurrentlyRetreat = joueurCourant.peutRetraiteProperty().get();
            }

            if (disableDueToPokemonChoice || instructionIsForEnergySelection) {
                retreatButton.setDisable(true);
            } else {
                retreatButton.setDisable(!canPlayerCurrentlyRetreat);
            }
        }

        // Optionally, highlight the bench or provide other cues
        if (panneauBancHBox != null) {
            if (isChoosingNewActivePokemon) {
                panneauBancHBox.getStyleClass().add("banc-selection-active");
            } else {
                panneauBancHBox.getStyleClass().removeAll("banc-selection-active");
            }
            // Ensure bench buttons themselves are not disabled if they are the target
            // This is implicitly handled as we only disable *other* areas.
        }
    }
}