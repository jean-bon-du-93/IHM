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

    private static final int MAX_EMPLACEMENTS_BANC = 5; // Constante ajoutée pour le nombre maximum de Pokémon sur le banc

    // Constantes pour les tailles des images utilisées dans cette vue
    private static final double LARGEUR_CARTE_MAIN = 150; // Largeur d'une carte dans la main
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
    HBox energiePokemonActifHBox; // HBox pour les énergies du Pokémon actif
    @FXML
    private VBox pokemonActifVBox; // VBox pour les détails du Pokémon actif (PV, etc.)
    @FXML
    HBox panneauMainHBox;
    @FXML
    HBox panneauBancHBox;
    @FXML
    private FlowPane attaquesPane; // FlowPane pour afficher les attaques disponibles
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
    private ListChangeListener<String> attaquesActivesListener; // Observateur pour les attaques du Pokémon actif
    private ListChangeListener<ICarte> mainDuJoueurActifChangeListener;
    private ListChangeListener<IPokemon> changementBancJoueur;
    private MapChangeListener<String, List<String>> energiePokemonActifListener; // Observateur pour l'énergie du Pokémon actif
    private ListChangeListener<ICarte> piocheListener; // Observateur pour la pioche du joueur
    private ListChangeListener<ICarte> recompensesListener; // Observateur pour les cartes récompense du joueur

    // Champs pour les observateurs d'énergie des Pokémon de banc
    private final Map<IPokemon, MapChangeListener<String, List<String>>> observateursEnergieBanc = new HashMap<>(); // Renamed benchEnergyListeners
    private final Map<IPokemon, HBox> uiEnergiePokemonBanc = new HashMap<>(); // Renamed benchPokemonEnergyUI
    // private final Map<IPokemon, ChangeListener<CartePokemon>> observateursCartePokemonBanc = new HashMap<>(); // Commentaire traduit et variable renommée
    // private final Map<IPokemon, Button> boutonsPokemonBanc = new HashMap<>(); // Commentaire traduit et variable renommée

    private boolean estChoixNouveauPokemonActif = false; // Variable d'état pour le choix d'un nouveau Pokémon actif
    private boolean estSelectionEnergieADefausser = false; // Variable d'état pour la sélection d'énergie à défausser

    @FXML
    Button passerButton; // Bouton FXML, ne pas traduire le nom de la variable ici

    // Le champ 'jeu' devrait déjà exister. S'assurer qu'il n'est pas final s'il l'était.
    // private IJeu jeu; // Champ pour la référence au jeu

    public VueJoueurActif() { // Constructeur sans argument
        // Initialiser les maps ici si elles n'étaient pas 'final' ou initialisées directement lors de leur déclaration
        // observateursEnergieBanc = new HashMap<>();
        // uiEnergiePokemonBanc = new HashMap<>();
        FXMLLoader chargeurFxml = new FXMLLoader(getClass().getResource("/fxml/VueJoueurActif.fxml")); // Renamed loader
        chargeurFxml.setRoot(this); // Définit la racine du FXML comme étant cette instance
        chargeurFxml.setController(this); // Définit ce fichier comme contrôleur du FXML
        try {
            chargeurFxml.load(); // Charge le FXML
        } catch (IOException e) { // Renamed exception
            // Enveloppe l'IOException dans une RuntimeException pour simplifier la gestion des erreurs
            throw new RuntimeException(e);
        }
        // NE PAS appeler initialiserProprietesEtObservateurs() ou lierAuJoueurActifDuJeu() ici,
        // car 'jeu' n'est pas encore défini à ce stade.
    }

    public void setJeu(IJeu jeu) {
        this.jeu = jeu;
    }

    public void postInit() {
        initialiserProprietesEtListeners();
        lierAuJoueurActifDuJeu();

        if (this.jeu != null && this.jeu instanceof fr.umontpellier.iut.ptcgJavaFX.mecanique.Jeu) {
            fr.umontpellier.iut.ptcgJavaFX.mecanique.Jeu jeuConcret = (fr.umontpellier.iut.ptcgJavaFX.mecanique.Jeu) this.jeu; // Renamed
            jeuConcret.carteSelectionneeProperty().addListener((obs, selectionPrecedente, selectionActuelle) -> { // Renamed
                mettreAJourStyleSelectionPokemon(selectionActuelle);
            });
            // Appel initial pour la mise à jour au cas où une sélection existe déjà
            mettreAJourStyleSelectionPokemon(jeuConcret.carteSelectionneeProperty().get());
        }

        // Ajouter un observateur pour les changements d'instruction du jeu
        if (this.jeu != null && this.jeu.instructionProperty() != null) {
            this.jeu.instructionProperty().addListener((obs, instructionPrecedente, instructionActuelle) -> { // Renamed
                traiterChangementInstruction(instructionActuelle); // Méthode pour gérer le changement d'instruction
            });
            // Traiter l'instruction initiale au cas où elle serait déjà définie
            traiterChangementInstruction(this.jeu.instructionProperty().get());
        }

        // Initialiser les images pour la pioche et les cartes récompense
        if (piocheJoueurActifImageView != null) {
            piocheJoueurActifImageView.setImage(VueUtils.creerVueImagePourDosCarte(LARGEUR_DOS_PIOCHE_RECOMPENSE, HAUTEUR_DOS_PIOCHE_RECOMPENSE).getImage());
            piocheJoueurActifImageView.setVisible(false); // Initialement cachée, la visibilité sera gérée par un observateur
        }
        if (recompensesJoueurActifImageView != null) {
            recompensesJoueurActifImageView.setImage(VueUtils.creerVueImagePourDosCarte(LARGEUR_DOS_PIOCHE_RECOMPENSE, HAUTEUR_DOS_PIOCHE_RECOMPENSE).getImage());
            recompensesJoueurActifImageView.setVisible(false); // Initialement cachée, la visibilité sera gérée par un observateur
        }
    }

    @FXML
    void actionPasserParDefaut(ActionEvent event) { // Action pour le bouton "Passer"
        System.out.println("Bouton Passer cliqué dans VueJoueurActif, appel de jeu.passerAEteChoisi()"); // Message de log en français
        if (this.jeu != null) {
            this.jeu.passerAEteChoisi(); // Notifie le jeu que le joueur a choisi de passer
        }
    }

    public void lierAuJoueurActifDuJeu() {
        if (this.jeu != null && this.jeu.joueurActifProperty() != null) {
            this.joueurActifProperty.bind(this.jeu.joueurActifProperty());
        }
    }

    private void initialiserProprietesEtObservateurs() { // Renommé initialiserProprietesEtListeners
        this.joueurActifProperty = new SimpleObjectProperty<>(null);

        StringBinding liaisonNomJoueur = new StringBinding() {
            {
                super.bind(VueJoueurActif.this.joueurActifProperty);
            }

            @Override
            protected String computeValue() {
                IJoueur joueurActuel = VueJoueurActif.this.joueurActifProperty.get();
                return (joueurActuel == null) ? "Aucun joueur actif" : joueurActuel.getNom();
            }
        };
        // S'assurer que nomDuJoueurLabel est initialisé par FXML avant cette ligne
        if (nomDuJoueurLabel != null) {
            nomDuJoueurLabel.textProperty().bind(liaisonNomJoueur);
        }

        this.energiePokemonActifListener = changement -> {
            // Cet observateur est sur l'ObservableMap elle-même pour le Pokémon actif actuel.
            // Rappeler placerPokemonActif mettra à jour son affichage d'énergie.
            placerPokemonActif();
        };

        // Initialisation de carteDuPokemonActifListener RETIRÉE (commentaire traduit)
        // this.carteDuPokemonActifListener = (obsCarte, ancienneCarte, nouvelleCarte) -> {
        //     placerPokemonActif();
        // };

        this.pokemonDuJoueurActifChangeListener = (obsPokemon, ancienPkmn, nouveauPkmn) -> {
            if (ancienPkmn != null) {
                if (ancienPkmn.energieProperty() != null && this.energiePokemonActifListener != null) {
                    ancienPkmn.energieProperty().removeListener(this.energiePokemonActifListener);
                }
                // Détacher l'observateur d'attaques
                if (ancienPkmn.attaquesProperty() != null && this.attaquesActivesListener != null) {
                    ancienPkmn.attaquesProperty().removeListener(this.attaquesActivesListener);
                }
            }

            placerPokemonActif();
            // afficherAttaquesJouables(); // Sera appelé par l'observateur de attaquesProperty ou explicitement après ce bloc

            if (nouveauPkmn != null) {
                if (nouveauPkmn.energieProperty() != null && this.energiePokemonActifListener != null) {
                    nouveauPkmn.energieProperty().addListener(this.energiePokemonActifListener);
                }
                 // Attacher l'observateur d'attaques
                if (nouveauPkmn.attaquesProperty() != null && this.attaquesActivesListener != null) {
                    nouveauPkmn.attaquesProperty().addListener(this.attaquesActivesListener);
                }
            } else {
                if (energiePokemonActifHBox != null) energiePokemonActifHBox.getChildren().clear();
                if (attaquesPane != null) attaquesPane.getChildren().clear();
            }
            // Afficher/mettre à jour les attaques pour nouveauPkmn (ou effacer si null)
            afficherAttaquesJouables();
        };

        this.attaquesActivesListener = (ListChangeListener.Change<? extends String> changementAttaques) -> {
            afficherAttaquesJouables();
        };

        this.mainDuJoueurActifChangeListener = (ListChangeListener.Change<? extends ICarte> changementMain) -> {
            // Appeler la nouvelle méthode qui traite l'objet Change
            updatePanneauMain(changementMain);
        };

        this.changementBancJoueur = (ListChangeListener.Change<? extends IPokemon> changementBanc) -> {
            updatePanneauBanc(changementBanc);
        };

        this.piocheListener = changement -> {
            IJoueur leJoueurActif = joueurActifProperty.get();
            if (leJoueurActif != null && piocheJoueurActifLabel != null && piocheJoueurActifImageView != null) {
                int tailleDeLaPioche = leJoueurActif.piocheProperty().size(); // Renamed taillePioche
                piocheJoueurActifLabel.setText("Pioche : " + tailleDeLaPioche);
                piocheJoueurActifImageView.setVisible(tailleDeLaPioche > 0);
            } else if (piocheJoueurActifLabel != null && piocheJoueurActifImageView != null) {
                piocheJoueurActifLabel.setText("Pioche : 0");
                piocheJoueurActifImageView.setVisible(false);
            }
        };
        this.recompensesListener = changement -> {
            IJoueur leJoueurActif = joueurActifProperty.get();
            if (leJoueurActif != null && recompensesJoueurActifLabel != null && recompensesJoueurActifImageView != null) {
                int nombreDeRecompenses = leJoueurActif.recompensesProperty().size(); // Renamed tailleRecompenses
                recompensesJoueurActifLabel.setText("Récomp. : " + nombreDeRecompenses);
                recompensesJoueurActifImageView.setVisible(nombreDeRecompenses > 0);
            } else if (recompensesJoueurActifLabel != null && recompensesJoueurActifImageView != null) {
                recompensesJoueurActifLabel.setText("Récomp. : 0");
                recompensesJoueurActifImageView.setVisible(false);
            }
        };


        this.joueurActifGlobalChangeListener = (observable, ancienJoueur, nouveauJoueur) -> {
            // Détacher tous les observateurs de l'ancienJoueur et de ses propriétés
            if (ancienJoueur != null) {
                ObjectProperty<? extends IPokemon> anciennePropPokemonActif = ancienJoueur.pokemonActifProperty();
                if (anciennePropPokemonActif != null) {
                    anciennePropPokemonActif.removeListener(this.pokemonDuJoueurActifChangeListener);
                    IPokemon ancienneInstancePokemonActif = anciennePropPokemonActif.get(); // Renamed oldActivePkmnInstance
                    if (ancienneInstancePokemonActif != null) {
                        if (ancienneInstancePokemonActif.energieProperty() != null && this.energiePokemonActifListener != null) {
                            ancienneInstancePokemonActif.energieProperty().removeListener(this.energiePokemonActifListener);
                        }
                        if (ancienneInstancePokemonActif.attaquesProperty() != null && this.attaquesActivesListener != null) {
                           ancienneInstancePokemonActif.attaquesProperty().removeListener(this.attaquesActivesListener);
                        }
                    }
                }
                if (ancienJoueur.getMain() != null) {
                    ancienJoueur.getMain().removeListener(this.mainDuJoueurActifChangeListener);
                }
                if (ancienJoueur.getBanc() != null) {
                    ancienJoueur.getBanc().removeListener(this.changementBancJoueur);
                }
                if (ancienJoueur.piocheProperty() != null && this.piocheListener != null) {
                    ancienJoueur.piocheProperty().removeListener(this.piocheListener);
                }
                if (ancienJoueur.recompensesProperty() != null && this.recompensesListener != null) {
                    ancienJoueur.recompensesProperty().removeListener(this.recompensesListener);
                }
            }

            // Mettre à jour les affichages pour le nouveauJoueur
            placerPokemonActif();
            reconstruirePanneauMainComplet();
            reconstruirePanneauBancComplet();
            afficherAttaquesJouables();

            // Attacher tous les observateurs au nouveauJoueur et à ses propriétés
            if (nouveauJoueur != null) {
                ObjectProperty<? extends IPokemon> nouvellePropPokemonActif = nouveauJoueur.pokemonActifProperty(); // Renamed
                if (nouvellePropPokemonActif != null) {
                    nouvellePropPokemonActif.addListener(this.pokemonDuJoueurActifChangeListener);
                     // Obtenir l'instance actuelle du Pokémon actif pour attacher l'observateur d'énergie
                    IPokemon nouvelleInstancePkmnActif = nouvellePropPokemonActif.get(); // Renamed
                    if (nouvelleInstancePkmnActif != null) {
                        ObservableMap<String, List<String>> nouvelleCarteEnergie = nouvelleInstancePkmnActif.energieProperty(); // Renamed
                        if (nouvelleCarteEnergie != null && this.energiePokemonActifListener != null) {
                            nouvelleCarteEnergie.addListener(this.energiePokemonActifListener);
                        }
                        // Attachement de carteDuPokemonActifListener RETIRÉ
                         // Attacher l'observateur d'attaques
                        if (nouvelleInstancePkmnActif.attaquesProperty() != null && this.attaquesActivesListener != null) {
                            nouvelleInstancePkmnActif.attaquesProperty().addListener(this.attaquesActivesListener);
                        }
                    }
                }
                if (nouveauJoueur.getMain() != null) {
                    nouveauJoueur.getMain().addListener(this.mainDuJoueurActifChangeListener);
                }
                if (nouveauJoueur.getBanc() != null) {
                    nouveauJoueur.getBanc().addListener(this.changementBancJoueur);
                }
                if (nouveauJoueur.piocheProperty() != null && this.piocheListener != null) {
                    nouveauJoueur.piocheProperty().addListener(this.piocheListener);
                    // Mise à jour initiale pour la pioche
                    int taillePioche = nouveauJoueur.piocheProperty().size();
                    if (piocheJoueurActifLabel != null) piocheJoueurActifLabel.setText("Pioche : " + taillePioche); // Translated
                    if (piocheJoueurActifImageView != null) piocheJoueurActifImageView.setVisible(taillePioche > 0);
                }
                if (nouveauJoueur.recompensesProperty() != null && this.recompensesListener != null) {
                    nouveauJoueur.recompensesProperty().addListener(this.recompensesListener);
                    // Mise à jour initiale pour les récompenses
                    int tailleRecompenses = nouveauJoueur.recompensesProperty().size();
                    if (recompensesJoueurActifLabel != null) recompensesJoueurActifLabel.setText("Récomp. : " + tailleRecompenses); // Translated
                    if (recompensesJoueurActifImageView != null) recompensesJoueurActifImageView.setVisible(tailleRecompenses > 0);
                }
            } else { // Pas de nouveau joueur (ex: fin de partie), effacer les affichages
                if (energiePokemonActifHBox != null) energiePokemonActifHBox.getChildren().clear();
                if (attaquesPane != null) attaquesPane.getChildren().clear();
                if (piocheJoueurActifLabel != null) piocheJoueurActifLabel.setText("Pioche : 0"); // Translated
                if (piocheJoueurActifImageView != null) piocheJoueurActifImageView.setVisible(false);
                if (recompensesJoueurActifLabel != null) recompensesJoueurActifLabel.setText("Récomp. : 0"); // Translated
                if (recompensesJoueurActifImageView != null) recompensesJoueurActifImageView.setVisible(false);
            }
        };
        this.joueurActifProperty.addListener(this.joueurActifGlobalChangeListener);
    }

    private void initialiserProprietesEtObservateurs() { // Renamed method
        this.joueurActifProperty = new SimpleObjectProperty<>(null);

        StringBinding liaisonNomJoueur = new StringBinding() { // Renamed nomJoueurBinding
            {
                super.bind(VueJoueurActif.this.joueurActifProperty);
            }

            @Override
            protected String computeValue() {
                IJoueur joueurActuel = VueJoueurActif.this.joueurActifProperty.get(); // Renamed currentPlayer
                return (joueurActuel == null) ? "Aucun joueur actif" : joueurActuel.getNom(); // Translated
            }
        };
        // S'assurer que nomDuJoueurLabel est initialisé par FXML avant cette ligne
        if (nomDuJoueurLabel != null) {
            nomDuJoueurLabel.textProperty().bind(liaisonNomJoueur);
        }

        this.energiePokemonActifListener = changement -> { // Renamed change
            // Cet observateur est sur l'ObservableMap elle-même pour le Pokémon actif actuel.
            // Rappeler placerPokemonActif mettra à jour son affichage d'énergie.
            placerPokemonActif();
        };

        // Initialisation de carteDuPokemonActifListener RETIRÉE
        // this.carteDuPokemonActifListener = (obsCarte, ancienneCarte, nouvelleCarte) -> {
        //     placerPokemonActif();
        // };

        this.pokemonDuJoueurActifChangeListener = (obsPokemon, ancienPkmn, nouveauPkmn) -> { // Renamed parameters
            if (ancienPkmn != null) {
                if (ancienPkmn.energieProperty() != null && this.energiePokemonActifListener != null) {
                    ancienPkmn.energieProperty().removeListener(this.energiePokemonActifListener);
                }
                // Détacher l'observateur d'attaques
                if (ancienPkmn.attaquesProperty() != null && this.attaquesActivesListener != null) {
                    ancienPkmn.attaquesProperty().removeListener(this.attaquesActivesListener);
                }
            }

            placerPokemonActif();
            // afficherAttaquesJouables(); // Sera appelé par l'observateur de attaquesProperty ou explicitement après ce bloc

            if (nouveauPkmn != null) {
                if (nouveauPkmn.energieProperty() != null && this.energiePokemonActifListener != null) {
                    nouveauPkmn.energieProperty().addListener(this.energiePokemonActifListener);
                }
                 // Attacher l'observateur d'attaques
                if (nouveauPkmn.attaquesProperty() != null && this.attaquesActivesListener != null) {
                    nouveauPkmn.attaquesProperty().addListener(this.attaquesActivesListener);
                }
            } else {
                if (energiePokemonActifHBox != null) energiePokemonActifHBox.getChildren().clear();
                if (attaquesPane != null) attaquesPane.getChildren().clear();
            }
            // Afficher/mettre à jour les attaques pour nouveauPkmn (ou effacer si null)
            afficherAttaquesJouables();
        };

        this.attaquesActivesListener = (ListChangeListener.Change<? extends String> changementAttaques) -> { // Renamed parameter
            afficherAttaquesJouables();
        };

        this.mainDuJoueurActifChangeListener = (ListChangeListener.Change<? extends ICarte> changementMain) -> { // Renamed parameter
            // Appeler la nouvelle méthode qui traite l'objet Change
            updatePanneauMain(changementMain);
        };

        this.changementBancJoueur = (ListChangeListener.Change<? extends IPokemon> changementBanc) -> { // Renamed parameter
            updatePanneauBanc(changementBanc);
        };

        this.piocheListener = changement -> { // Renamed parameter
            IJoueur leJoueurActif = joueurActifProperty.get(); // Renamed joueurCourant
            if (leJoueurActif != null && piocheJoueurActifLabel != null && piocheJoueurActifImageView != null) {
                int taillePioche = leJoueurActif.piocheProperty().size();
                piocheJoueurActifLabel.setText("Pioche : " + taillePioche); // Translated "P: "
                piocheJoueurActifImageView.setVisible(taillePioche > 0);
            } else if (piocheJoueurActifLabel != null && piocheJoueurActifImageView != null) {
                piocheJoueurActifLabel.setText("Pioche : 0"); // Translated "P: 0"
                piocheJoueurActifImageView.setVisible(false);
            }
        };
        this.recompensesListener = changement -> { // Renamed parameter
            IJoueur leJoueurActif = joueurActifProperty.get(); // Renamed joueurCourant
            if (leJoueurActif != null && recompensesJoueurActifLabel != null && recompensesJoueurActifImageView != null) {
                int tailleRecompenses = leJoueurActif.recompensesProperty().size();
                recompensesJoueurActifLabel.setText("Récomp. : " + tailleRecompenses); // Translated "R: "
                recompensesJoueurActifImageView.setVisible(tailleRecompenses > 0);
            } else if (recompensesJoueurActifLabel != null && recompensesJoueurActifImageView != null) {
                recompensesJoueurActifLabel.setText("Récomp. : 0"); // Translated "R: 0"
                recompensesJoueurActifImageView.setVisible(false);
            }
        };


        this.joueurActifGlobalChangeListener = (observable, ancienJoueur, nouveauJoueur) -> { // Renamed parameters
            // Détacher tous les observateurs de l'ancienJoueur et de ses propriétés
            if (ancienJoueur != null) {
                ObjectProperty<? extends IPokemon> anciennePropPokemonActif = ancienJoueur.pokemonActifProperty(); // Renamed
                if (anciennePropPokemonActif != null) {
                    anciennePropPokemonActif.removeListener(this.pokemonDuJoueurActifChangeListener);
                    IPokemon ancienneInstancePkmnActif = anciennePropPokemonActif.get(); // Renamed
                    if (ancienneInstancePkmnActif != null) {
                        if (ancienneInstancePkmnActif.energieProperty() != null && this.energiePokemonActifListener != null) {
                            ancienneInstancePkmnActif.energieProperty().removeListener(this.energiePokemonActifListener);
                        }
                        if (ancienneInstancePkmnActif.attaquesProperty() != null && this.attaquesActivesListener != null) {
                           ancienneInstancePkmnActif.attaquesProperty().removeListener(this.attaquesActivesListener);
                        }
                    }
                }
                if (ancienJoueur.getMain() != null) {
                    ancienJoueur.getMain().removeListener(this.mainDuJoueurActifChangeListener);
                }
                if (ancienJoueur.getBanc() != null) {
                    ancienJoueur.getBanc().removeListener(this.changementBancJoueur);
                }
                if (ancienJoueur.piocheProperty() != null && this.piocheListener != null) {
                    ancienJoueur.piocheProperty().removeListener(this.piocheListener);
                }
                if (ancienJoueur.recompensesProperty() != null && this.recompensesListener != null) {
                    ancienJoueur.recompensesProperty().removeListener(this.recompensesListener);
                }
            }

            // Mettre à jour les affichages pour le nouveauJoueur
            placerPokemonActif();
            reconstruirePanneauMainComplet();
            reconstruirePanneauBancComplet();
            afficherAttaquesJouables();

            // Attacher tous les observateurs au nouveauJoueur et à ses propriétés
            if (nouveauJoueur != null) {
                ObjectProperty<? extends IPokemon> nouvellePropPokemonActif = nouveauJoueur.pokemonActifProperty();
                if (nouvellePropPokemonActif != null) {
                    nouvellePropPokemonActif.addListener(this.pokemonDuJoueurActifChangeListener);
                     // Obtenir l'instance actuelle du Pokémon actif pour attacher l'observateur d'énergie
                    IPokemon nouvelleInstancePokemonActif = nouvellePropPokemonActif.get(); // Renamed newActivePkmnInstance
                    if (nouvelleInstancePokemonActif != null) {
                        ObservableMap<String, List<String>> nouvelleCarteDesEnergies = nouvelleInstancePokemonActif.energieProperty(); // Renamed newEnergieMap
                        if (nouvelleCarteDesEnergies != null && this.energiePokemonActifListener != null) {
                            nouvelleCarteDesEnergies.addListener(this.energiePokemonActifListener);
                        }
                        // Attachement de carteDuPokemonActifListener RETIRÉ (commentaire traduit)
                         // Attacher l'observateur d'attaques
                        if (nouvelleInstancePokemonActif.attaquesProperty() != null && this.attaquesActivesListener != null) {
                            nouvelleInstancePokemonActif.attaquesProperty().addListener(this.attaquesActivesListener);
                        }
                    }
                }
                if (nouveauJoueur.getMain() != null) {
                    nouveauJoueur.getMain().addListener(this.mainDuJoueurActifChangeListener);
                }
                if (nouveauJoueur.getBanc() != null) {
                    nouveauJoueur.getBanc().addListener(this.changementBancJoueur);
                }
                if (nouveauJoueur.piocheProperty() != null && this.piocheListener != null) {
                    nouveauJoueur.piocheProperty().addListener(this.piocheListener);
                    // Mise à jour initiale pour la pioche
                    int tailleDeLaPioche = nouveauJoueur.piocheProperty().size(); // Renamed taillePioche
                    if (piocheJoueurActifLabel != null) piocheJoueurActifLabel.setText("Pioche : " + tailleDeLaPioche);
                    if (piocheJoueurActifImageView != null) piocheJoueurActifImageView.setVisible(tailleDeLaPioche > 0);
                }
                if (nouveauJoueur.recompensesProperty() != null && this.recompensesListener != null) {
                    nouveauJoueur.recompensesProperty().addListener(this.recompensesListener);
                    // Mise à jour initiale pour les récompenses
                    int nombreDeRecompenses = nouveauJoueur.recompensesProperty().size(); // Renamed tailleRecompenses
                    if (recompensesJoueurActifLabel != null) recompensesJoueurActifLabel.setText("Récomp. : " + nombreDeRecompenses);
                    if (recompensesJoueurActifImageView != null) recompensesJoueurActifImageView.setVisible(nombreDeRecompenses > 0);
                }
            } else { // Pas de nouveau joueur (ex: fin de partie), effacer les affichages
                if (energiePokemonActifHBox != null) energiePokemonActifHBox.getChildren().clear();
                if (attaquesPane != null) attaquesPane.getChildren().clear();
                if (piocheJoueurActifLabel != null) piocheJoueurActifLabel.setText("Pioche : 0");
                if (piocheJoueurActifImageView != null) piocheJoueurActifImageView.setVisible(false);
                if (recompensesJoueurActifLabel != null) recompensesJoueurActifLabel.setText("Récomp. : 0");
                if (recompensesJoueurActifImageView != null) recompensesJoueurActifImageView.setVisible(false);
            }
        };
        this.joueurActifProperty.addListener(this.joueurActifGlobalChangeListener);
    }

    public void placerPokemonActif() {
        IJoueur joueurCourant = joueurActifProperty.get(); // Renamed currentJoueur
        IPokemon pokemonActifCourant = (joueurCourant != null && joueurCourant.pokemonActifProperty() != null) ? joueurCourant.pokemonActifProperty().get() : null; // Renamed currentActivePokemon

        // Nettoyer l'ancien label PV s'il existe et que pokemonActifVBox est disponible
        if (pokemonActifVBox != null) {
            pokemonActifVBox.getChildren().removeIf(node -> "hpLabelActif".equals(node.getId()));
            // Potentiellement, nettoyer aussi d'autres labels spécifiques si nécessaire (Faiblesse, Résistance, Coût de Retraite, Statuts)
            // pokemonActifVBox.getChildren().removeIf(node -> "weaknessLabelActif".equals(node.getId()));
            // pokemonActifVBox.getChildren().removeIf(node -> "resistanceLabelActif".equals(node.getId()));
            // pokemonActifVBox.getChildren().removeIf(node -> "retreatCostLabelActif".equals(node.getId()));
            // pokemonActifVBox.getChildren().removeIf(node -> "statusBoxActif".equals(node.getId())); // Pour le HBox des statuts
        }


        if (pokemonActifButton != null) { // S'assurer que pokemonActifButton est initialisé
            if (pokemonActifCourant != null && pokemonActifCourant.cartePokemonProperty() != null && pokemonActifCourant.cartePokemonProperty().get() != null) {
                CartePokemon carteDuPokemon = pokemonActifCourant.cartePokemonProperty().get(); // Renamed pokemonCard
                pokemonActifButton.setText(carteDuPokemon.getNom()); // Met à jour le nom sur le bouton
                ImageView vueImagePokemon = VueUtils.creerVueImagePourCarte(carteDuPokemon, LARGEUR_PKMN_ACTIF, HAUTEUR_PKMN_ACTIF); // Renamed pokemonImageView
                pokemonActifButton.setGraphic(vueImagePokemon);
                pokemonActifButton.setDisable(false); // Réactive le bouton

                // Affichage des PV
                if (pokemonActifVBox != null) {
                    Label hpLabel = new Label();
                    hpLabel.setId("hpLabelActif"); // ID pour suppression future
                    hpLabel.getStyleClass().add("hp-label"); // Classe CSS pour le style

                    // Liaison pour les PV, gérant les cas null
                    final IPokemon pokemonPourLiaison = pokemonActifCourant; // Variable finale pour lambda
                    StringBinding liaisonPv = Bindings.createStringBinding(() -> {
                        if (pokemonPourLiaison != null && pokemonPourLiaison.cartePokemonProperty().get() != null) {
                            return "PV : " + pokemonPourLiaison.pointsDeVieProperty().get();
                        }
                        return "PV : --";
                    }, pokemonPourLiaison.pointsDeVieProperty(), pokemonPourLiaison.cartePokemonProperty());
                    hpLabel.textProperty().bind(liaisonPv);

                    // S'assurer de l'ajouter à la bonne position (ex: après le bouton, avant les énergies)
                    // Si le bouton est à 0, et qu'on veut le label PV ensuite, puis les énergies.
                    // Si energiePokemonActifHBox est déjà là, il faut l'insérer avant.
                    // Si pokemonActifVBox a 2 enfants (bouton, HBox énergie), insérer à l'index 1.
                    // Si seulement le bouton est là, ajouter simplement.
                    if (pokemonActifVBox.getChildren().contains(energiePokemonActifHBox)) {
                         int indexEnergieHBox = pokemonActifVBox.getChildren().indexOf(energiePokemonActifHBox);
                         pokemonActifVBox.getChildren().add(indexEnergieHBox, hpLabel); // Ajoute avant l'HBox d'énergie
                    } else {
                         pokemonActifVBox.getChildren().add(hpLabel); // Ajoute à la fin si pas d'HBox d'énergie (ou après le bouton si c'est le seul)
                    }
                }

                // Afficher les énergies attachées
                afficherEnergiesPokemonActif(pokemonActifCourant);

            } else {
                pokemonActifButton.setText("Pas de Pokémon Actif"); // Texte par défaut si aucun Pokémon actif
                pokemonActifButton.setGraphic(null); // Retire l'image
                pokemonActifButton.setDisable(true); // Désactive le bouton
                if (energiePokemonActifHBox != null) {
                    energiePokemonActifHBox.getChildren().clear(); // Efface les énergies
                }
                // Assurer aussi que le label PV est retiré si le Pokémon devient null
                if (pokemonActifVBox != null) {
                     pokemonActifVBox.getChildren().removeIf(node -> "hpLabelActif".equals(node.getId()));
                }
            }
        }
        // Mettre à jour l'interactivité des éléments UI en fonction de l'état du jeu
        mettreAJourInteractiviteUI();