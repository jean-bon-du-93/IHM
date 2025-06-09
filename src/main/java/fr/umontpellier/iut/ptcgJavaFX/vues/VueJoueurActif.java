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
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class VueJoueurActif extends VBox {

    private IJeu jeu;
    private ObjectProperty<IJoueur> joueurActifProperty;
    @FXML
    private Label nomDuJoueurLabel;
    @FXML
    private Label pokemonActifLabel;
    @FXML
    private HBox panneauMainHBox;
    @FXML
    private HBox panneauBancHBox;

    private ChangeListener<IJoueur> joueurActifGlobalChangeListener;
    private ChangeListener<IPokemon> pokemonDuJoueurActifChangeListener;
    private ListChangeListener<ICarte> mainDuJoueurActifChangeListener;
    private ListChangeListener<IPokemon> changementBancJoueur;


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


        this.pokemonDuJoueurActifChangeListener = (obs, oldPkmn, newPkmn) -> {
            placerPokemonActif();
        };

        this.mainDuJoueurActifChangeListener = (ListChangeListener.Change<? extends ICarte> c) -> {
            placerMain();
        };

        this.changementBancJoueur = (ListChangeListener.Change<? extends IPokemon> c) -> {
            placerBanc();
        };

        this.joueurActifGlobalChangeListener = (observable, oldJoueur, newJoueur) -> {
            if (oldJoueur != null) {
                if (oldJoueur.pokemonActifProperty() != null) {
                    oldJoueur.pokemonActifProperty().removeListener(this.pokemonDuJoueurActifChangeListener);
                }
                if (oldJoueur.getMain() != null) {
                    oldJoueur.getMain().removeListener(this.mainDuJoueurActifChangeListener);
                }
                if (oldJoueur.getBanc() != null) {
                    oldJoueur.getBanc().removeListener(this.changementBancJoueur);
                }
            }

            placerPokemonActif();
            placerMain();
            placerBanc();

            if (newJoueur != null) {
                if (newJoueur.pokemonActifProperty() != null) {
                    newJoueur.pokemonActifProperty().addListener(this.pokemonDuJoueurActifChangeListener);
                }
                if (newJoueur.getMain() != null) {
                    newJoueur.getMain().addListener(this.mainDuJoueurActifChangeListener);
                }
                if (newJoueur.getBanc() != null) {
                    newJoueur.getBanc().addListener(this.changementBancJoueur);
                }
            }
        };
        this.joueurActifProperty.addListener(this.joueurActifGlobalChangeListener);
    }

    public void placerPokemonActif() {
        String texteAffichage = "Aucun Pokémon actif";
        IJoueur joueurCourant = joueurActifProperty.get();

        if (joueurCourant != null) {
            ObjectProperty<? extends IPokemon> pokemonProperty = joueurCourant.pokemonActifProperty();
            if (pokemonProperty != null) {
                IPokemon pkmn = pokemonProperty.get();
                if (pkmn != null && pkmn.getCartePokemon() != null) {
                    texteAffichage = pkmn.getCartePokemon().getNom();
                }
            }
        }
        if (pokemonActifLabel != null) {
            pokemonActifLabel.setText(texteAffichage);
        }
    }

    public void placerMain() {
        if (panneauMainHBox == null) return; // panneauMainHBox might not be initialized if FXML loading failed
        panneauMainHBox.getChildren().clear();
        IJoueur joueurCourant = joueurActifProperty.get();

        if (joueurCourant != null) {
            ObservableList<? extends ICarte> mainDuJoueur = joueurCourant.getMain();
            if (mainDuJoueur != null) {
                for (ICarte carte : mainDuJoueur) {
                    Button boutonCarte = new Button(carte.getNom());
                    boutonCarte.getStyleClass().add("text-18px");
                    boutonCarte.setOnAction(event -> {
                        if (this.jeu != null) {
                            this.jeu.uneCarteDeLaMainAEteChoisie(carte.getId());
                        }
                    });
                    panneauMainHBox.getChildren().add(boutonCarte);
                }
            }
        }
    }

    public void placerBanc() {
        if (panneauBancHBox == null) return;
        panneauBancHBox.getChildren().clear();
        IJoueur joueurCourant = joueurActifProperty.get();

        if (joueurCourant != null && joueurCourant.getBanc() != null) {
            for (IPokemon pokemon : joueurCourant.getBanc()) {
                if (pokemon != null && pokemon.getCartePokemon() != null) { // Check for null pokemon and its card
                    Button boutonPokemonBanc = new Button(pokemon.getCartePokemon().getNom());
                    boutonPokemonBanc.getStyleClass().add("text-18px");
                    boutonPokemonBanc.setOnAction(event -> {
                        if (this.jeu != null) {
                            // IPokemon does not have getId(), but ICarte does.
                            // So, we use pokemon.getCartePokemon().getId().
                            this.jeu.unPokemonDuBancAEteChoisi(pokemon.getCartePokemon().getId());
                        }
                    });
                    panneauBancHBox.getChildren().add(boutonPokemonBanc);
                }
            }
        }
    }
}