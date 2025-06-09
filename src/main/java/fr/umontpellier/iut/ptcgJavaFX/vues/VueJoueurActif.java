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
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class VueJoueurActif extends VBox {

    private IJeu jeu;
    private ObjectProperty<IJoueur> joueurActifProperty;
    private Label nomDuJoueurLabel;
    private Label pokemonActifLabel;
    private HBox panneauMainHBox;

    private ChangeListener<IJoueur> joueurActifGlobalChangeListener;
    private ChangeListener<IPokemon> pokemonDuJoueurActifChangeListener;
    private ListChangeListener<ICarte> mainDuJoueurActifChangeListener;

    private Button passer;
    private final EventHandler<ActionEvent> actionPasserParDefaut;

    public VueJoueurActif(IJeu jeu) {
        this.jeu = jeu;
        this.nomDuJoueurLabel = new Label("Pas de joueur actif");
        this.pokemonActifLabel = new Label("Aucun Pokémon actif");
        this.panneauMainHBox = new HBox();
        this.panneauMainHBox.setSpacing(5);
        this.passer = new Button("Passer");
        actionPasserParDefaut = event -> {
            if (this.jeu != null) {
                this.jeu.passerAEteChoisi();
            }
        };
        passer.setOnAction(actionPasserParDefaut);

        this.setSpacing(10);
        getChildren().addAll(nomDuJoueurLabel, pokemonActifLabel, panneauMainHBox, passer);

        initialiserProprietesEtListeners();
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
        nomDuJoueurLabel.textProperty().bind(nomJoueurBinding);

        this.pokemonDuJoueurActifChangeListener = (obs, oldPkmn, newPkmn) -> {
            placerPokemonActif();
        };

        this.mainDuJoueurActifChangeListener = (ListChangeListener.Change<? extends ICarte> c) -> {
            placerMain();
        };

        this.joueurActifGlobalChangeListener = (observable, oldJoueur, newJoueur) -> {
            if (oldJoueur != null) {
                if (oldJoueur.pokemonActifProperty() != null) {
                    oldJoueur.pokemonActifProperty().removeListener(this.pokemonDuJoueurActifChangeListener);
                }
                if (oldJoueur.getMain() != null) {
                    oldJoueur.getMain().removeListener(this.mainDuJoueurActifChangeListener);
                }
            }

            placerPokemonActif();
            placerMain();

            if (newJoueur != null) {
                if (newJoueur.pokemonActifProperty() != null) {
                    newJoueur.pokemonActifProperty().addListener(this.pokemonDuJoueurActifChangeListener);
                }
                if (newJoueur.getMain() != null) {
                    newJoueur.getMain().addListener(this.mainDuJoueurActifChangeListener);
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
        pokemonActifLabel.setText(texteAffichage);
    }

    public void placerMain() {
        panneauMainHBox.getChildren().clear();
        IJoueur joueurCourant = joueurActifProperty.get();

        if (joueurCourant != null) {
            ObservableList<? extends ICarte> mainDuJoueur = joueurCourant.getMain();
            if (mainDuJoueur != null) {
                for (ICarte carte : mainDuJoueur) {
                    Button boutonCarte = new Button(carte.getNom());
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
}