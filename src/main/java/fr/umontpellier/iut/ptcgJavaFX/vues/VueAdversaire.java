package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import javafx.beans.binding.Bindings; // Added import
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
    private static final int MAX_EMPLACEMENTS_BANC_ADVERSAIRE = 5; // Matching VueJoueurActif

    // Constants for image sizes
    private static final double LARGEUR_POKEMON_ACTIF_ADVERSAIRE = 150;
    private static final double HAUTEUR_POKEMON_ACTIF_ADVERSAIRE = 225;
    private static final double LARGEUR_POKEMON_BANC_ADVERSAIRE = 100;
    private static final double HAUTEUR_POKEMON_BANC_ADVERSAIRE = 140;
    private static final double LARGEUR_CARTE_MAIN_ADVERSAIRE = 150;
    private static final double HAUTEUR_CARTE_MAIN_ADVERSAIRE = 225;
    private static final double TAILLE_ICONE_ENERGIE_ADVERSAIRE = 18;
    private static final double LARGEUR_DOS_CARTE_PETIT = 30;
    private static final double HAUTEUR_DOS_CARTE_PETIT = 42;

    private IJeu jeuEnCours;
    private IJoueur joueurAdverse;

    @FXML Label nomAdversaireLabel;
    @FXML Button opponentPokemonActifButton; // Changed from Label to Button and renamed
    @FXML HBox energiePokemonActifAdversaireHBox; // Added
    @FXML private VBox opponentPokemonActifVBox; // Added for HP display
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
    private ChangeListener<IPokemon> observateurPokemonActifAdv;
    private ListChangeListener<IPokemon> observateurBancAdv;
    // private ListChangeListener<ICarte> mainListener; // Replaced by cardCountChangeListener
    // private ListChangeListener<ICarte> deckListener; // Replaced by cardCountChangeListener
    // private ListChangeListener<ICarte> defausseListener; // Replaced by cardCountChangeListener
    // private ListChangeListener<ICarte> prixListener; // Replaced by cardCountChangeListener
    private ListChangeListener<ICarte> observateurComptesCartes; // For main, deck, discard, prizes
    private MapChangeListener<String, List<String>> observateurEnergiePokemonActifAdv; // Added field
    private IPokemon pokemonActifAdvPourObservateurEnergie = null; // To keep track of the Pokemon whose energy is being listened to


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
        this.jeuEnCours = jeu;
        // If VueAdversaire needs to react to game-wide changes that affect the opponent,
        // bindings to 'jeu' would be set up here.
    }

    public void setAdversaire(IJoueur adversaire) {
        if (this.joueurAdverse != null) {
            // Remove old listeners if switching opponent
            nettoyerLiaisonsEtObservateursAdversaire();
        }
        this.joueurAdverse = adversaire;
        initialiserAffichageAdversaire();
    }

    private void initialiserAffichageAdversaire() {
        if (joueurAdverse == null) {
            nomAdversaireLabel.setText("Adversaire non défini"); // Texte déjà en français
            if (opponentPokemonActifButton != null) opponentPokemonActifButton.setText("N/D"); // "Non Défini" ou "Non Disponible"
            if (energiePokemonActifAdversaireHBox != null) energiePokemonActifAdversaireHBox.getChildren().clear();
            if (panneauMainAdversaireHBox != null) panneauMainAdversaireHBox.getChildren().clear();
            if (bancAdversaireHBox != null) bancAdversaireHBox.getChildren().clear();
            if (mainAdversaireLabel != null) mainAdversaireLabel.setText("Main Adv. : N/D"); // Traduit
            if (deckAdversaireLabel != null) deckAdversaireLabel.setText("Pioche Adv. : N/D"); // Traduit
            if (deckAdversaireImageView != null) deckAdversaireImageView.setVisible(false); // Cacher si pas d'adversaire
            if (defausseAdversaireLabel != null) defausseAdversaireLabel.setText("Défausse Adv. : N/D"); // Traduit
            if (prixAdversaireLabel != null) prixAdversaireLabel.setText("Récomp. Adv. : N/D"); // Traduit
            if (prixAdversaireImageView != null) prixAdversaireImageView.setVisible(false); // Cacher si pas d'adversaire
            return;
        }

        // Initialiser l'observateur partagé pour les comptes de cartes
        this.observateurComptesCartes = c -> {
            mettreAJourComptesCartesAdversaire(); // Met à jour les étiquettes de compte
            placerMainAdversaire(); // Met à jour l'affichage visuel de la main
        };

        // Configuration initiale de l'UI
        if (nomAdversaireLabel != null) nomAdversaireLabel.setText(joueurAdverse.getNom());

        // L'ancien gestionnaire setOnMouseClicked pour pokemonActifAdversaireDisplay a été retiré d'ici.
        // Le nouveau Bouton utilisera onAction spécifié dans FXML.

        // Configurer les images pour les dos de pioche et cartes récompense
        if (deckAdversaireImageView != null) {
            deckAdversaireImageView.setImage(VueUtils.creerVueImagePourDosCarte(LARGEUR_DOS_CARTE_PETIT, HAUTEUR_DOS_CARTE_PETIT).getImage());
        }
        if (prixAdversaireImageView != null) {
            prixAdversaireImageView.setImage(VueUtils.creerVueImagePourDosCarte(LARGEUR_DOS_CARTE_PETIT, HAUTEUR_DOS_CARTE_PETIT).getImage());
        }

        afficherPokemonActifAdversaire();
        afficherBancAdversaire();
        afficherMainAdversaire(); // Placement initial de la représentation de la main
        actualiserCompteursCartesAdversaire(); // Mise à jour initiale des comptes et de la visibilité des images

        // Configurer les observateurs
        configurerObservateursAdversaire(); // Renamed setupListeners

        if (this.jeuEnCours != null && this.jeuEnCours instanceof fr.umontpellier.iut.ptcgJavaFX.mecanique.Jeu) {
            fr.umontpellier.iut.ptcgJavaFX.mecanique.Jeu jeuConcret = (fr.umontpellier.iut.ptcgJavaFX.mecanique.Jeu) this.jeuEnCours;
            // Pour éviter d'ajouter plusieurs observateurs si initialiserAffichage est appelé plusieurs fois avec le même jeu,
            // il serait mieux de passer l'observateur à nettoyerLiaisonsEtObservateurs pour le retirer.
            // Pour l'instant, on va simplement l'ajouter.
            // Une solution plus robuste serait de gérer cet observateur dans setJeu ou de s'assurer qu'il est retiré si VueAdversaire peut être réassignée à une nouvelle instance de Jeu.
            // Ou, vérifier si un observateur est déjà attaché avant d'en ajouter un.
            // Par souci de simplicité pour cette tâche, simple ajout :
            jeuConcret.carteSelectionneeProperty().addListener((obs, ancienneSelection, nouvelleSelection) -> { // Renamed
                actualiserStyleSelectionPokemonAdversaire(nouvelleSelection);
            });
            actualiserStyleSelectionPokemonAdversaire(jeuConcret.carteSelectionneeProperty().get());
        }
    }

    private void configurerObservateursAdversaire() { // Renamed setupListeners
        if (this.joueurAdverse == null) return; // Renforcement anti-NPE

        // Définir l'observateur pour les changements d'énergie sur le Pokémon actif
        this.observateurEnergiePokemonActifAdv = changement -> { // Renamed change
            mettreAJourEnergiesPokemonActifAdversaire();
        };

        // Observateur pour les changements de Pokémon actif
        ObjectProperty<? extends IPokemon> propPokemonActif = joueurAdverse.pokemonActifProperty(); // Renamed
        if (propPokemonActif != null) {
            observateurPokemonActifAdv = (obs, ancienneValeur, nouvelleValeur) -> { // Renamed
                // Détacher l'observateur de l'énergie de l'ancien Pokémon actif
                if (ancienneValeur != null && ancienneValeur.energieProperty() != null) {
                    ancienneValeur.energieProperty().removeListener(this.observateurEnergiePokemonActifAdv);
                }
                pokemonActifAdvPourObservateurEnergie = nouvelleValeur; // Mettre à jour le Pokémon suivi

                afficherPokemonActifAdversaire(); // Ceci appellera aussi mettreAJourEnergiesPokemonActifAdversaire

                // Attacher l'observateur à l'énergie du nouveau Pokémon actif
                if (nouvelleValeur != null && nouvelleValeur.energieProperty() != null) {
                    nouvelleValeur.energieProperty().addListener(this.observateurEnergiePokemonActifAdv);
                }
            };
            propPokemonActif.addListener(observateurPokemonActifAdv);
            // Attachement initial à l'énergie du Pokémon actif actuel (s'il existe)
            IPokemon pokemonActifActuel = propPokemonActif.get(); // Renamed
            if (pokemonActifActuel != null && pokemonActifActuel.energieProperty() != null) {
                pokemonActifActuel.energieProperty().addListener(this.observateurEnergiePokemonActifAdv);
                pokemonActifAdvPourObservateurEnergie = pokemonActifActuel;
            }
        }

        // Observateur pour le banc
        ObservableList<? extends IPokemon> listeBanc = joueurAdverse.getBanc(); // Renamed
        if (listeBanc != null) {
            observateurBancAdv = changement -> placerBancAdversaire(); // Renamed
            listeBanc.addListener(observateurBancAdv);
        }

        // Observateurs pour les comptes de cartes utilisant l'observateur partagé
        // En supposant que IJoueur a des méthodes xxxProperty() retournant ObservableList ou ListProperty
        // Si IJoueur fournit par ex. mainProperty() qui est une ReadOnlyListProperty<ICarte>,
        // alors .addListener(this.cardCountChangeListener) est correct.
        // Si c'est juste getMain() retournant ObservableList, c'est aussi bien.
        // La tâche implique que des méthodes de propriété existent pour celles-ci.
        // Correction de main pour utiliser getMain() selon les nouvelles instructions
        ObservableList<? extends ICarte> listeMain = joueurAdverse.getMain(); // Renamed
        if (listeMain != null) {
            listeMain.addListener(this.observateurComptesCartes);
        }
        if (joueurAdverse.piocheProperty() != null) {
            joueurAdverse.piocheProperty().addListener(this.observateurComptesCartes);
        }
        if (joueurAdverse.defausseProperty() != null) {
            joueurAdverse.defausseProperty().addListener(this.observateurComptesCartes);
        }
        if (joueurAdverse.recompensesProperty() != null) {
            joueurAdverse.recompensesProperty().addListener(this.observateurComptesCartes);
        }
    }

    private void nettoyerLiaisonsEtObservateursAdversaire() { // Renamed clearBindingsAndListeners
        if (joueurAdverse == null) return;

        ObjectProperty<? extends IPokemon> propPokemonActif = joueurAdverse.pokemonActifProperty(); // Renamed
        if (propPokemonActif != null && observateurPokemonActifAdv != null) {
            propPokemonActif.removeListener(observateurPokemonActifAdv);
        }
        // Détacher l'observateur d'énergie du dernier Pokémon actif connu
        if (pokemonActifAdvPourObservateurEnergie != null && pokemonActifAdvPourObservateurEnergie.energieProperty() != null && observateurEnergiePokemonActifAdv != null) {
            pokemonActifAdvPourObservateurEnergie.energieProperty().removeListener(observateurEnergiePokemonActifAdv);
        }
        pokemonActifAdvPourObservateurEnergie = null;


        ObservableList<? extends IPokemon> listeBanc = joueurAdverse.getBanc(); // Renamed
        if (listeBanc != null && observateurBancAdv != null) {
            listeBanc.removeListener(observateurBancAdv);
        }

        // Retirer l'observateur de compte de cartes partagé
        if (this.observateurComptesCartes != null) {
            // Correction de main pour utiliser getMain()
            ObservableList<? extends ICarte> listeMain = joueurAdverse.getMain(); // Renamed
            if (listeMain != null) {
                listeMain.removeListener(this.observateurComptesCartes);
            }
            if (joueurAdverse.piocheProperty() != null) {
                joueurAdverse.piocheProperty().removeListener(this.observateurComptesCartes);
            }
            if (joueurAdverse.defausseProperty() != null) {
                joueurAdverse.defausseProperty().removeListener(this.observateurComptesCartes);
            }
            if (joueurAdverse.recompensesProperty() != null) {
                joueurAdverse.recompensesProperty().removeListener(this.observateurComptesCartes);
            }
        }
    }


    private void afficherPokemonActifAdversaire() {
        IPokemon pokemonActifAdv = null; // Renamed pkmnActif
        if (joueurAdverse != null && joueurAdverse.pokemonActifProperty() != null) {
            pokemonActifAdv = joueurAdverse.pokemonActifProperty().get();
        }

        // Enlever les étiquettes existantes si présentes
        if (opponentPokemonActifVBox != null) {
            opponentPokemonActifVBox.getChildren().removeIf(noeud -> "hpLabelOpponentActif".equals(noeud.getId()));
            opponentPokemonActifVBox.getChildren().removeIf(noeud -> "weaknessLabelOpponentActif".equals(noeud.getId()));
            opponentPokemonActifVBox.getChildren().removeIf(noeud -> "resistanceLabelOpponentActif".equals(noeud.getId()));
            opponentPokemonActifVBox.getChildren().removeIf(noeud -> "retreatLabelOpponentActif".equals(noeud.getId()));
            opponentPokemonActifVBox.getChildren().removeIf(noeud -> "statusBoxOpponentActif".equals(noeud.getId())); // Enlever la HBox des statuts
        }

        if (opponentPokemonActifButton != null) { // Ce bouton est dans opponentPokemonActifVBox
            if (pokemonActifAdv != null && pokemonActifAdv.getCartePokemon() != null) {
                ImageView vueImage = VueUtils.creerVueImagePourCarte(pokemonActifAdv.getCartePokemon(), LARGEUR_POKEMON_ACTIF_ADVERSAIRE, HAUTEUR_POKEMON_ACTIF_ADVERSAIRE); // Renamed
                opponentPokemonActifButton.setGraphic(vueImage);
                opponentPokemonActifButton.setText(null); // Enlever le texte

                // Ajouter l'étiquette des PV
                if (opponentPokemonActifVBox != null) {
                    Label etiquettePV = new Label(); // Renamed
                    etiquettePV.setId("hpLabelOpponentActif");
                    etiquettePV.getStyleClass().add("hp-label");

                    // Variable finale pour utilisation dans l'expression lambda
                    final IPokemon pokemonPourLiaison = pokemonActifAdv; // Renamed
                    // Lier la propriété text à pointsDeVieProperty du pokemonPourLiaison
                    etiquettePV.textProperty().bind(
                        Bindings.createStringBinding(
                            () -> "PV : " + pokemonPourLiaison.pointsDeVieProperty().get(), // Translated
                            pokemonPourLiaison.pointsDeVieProperty() // Dépendance
                        )
                    );

                    // La VBox contient : Label (titre), Button (pokemon), HBox (énergie)
                    // Nous voulons ajouter l'étiquette PV après le bouton, donc à l'index 2 (ou avant energiePokemonActifAdversaireHBox)
                    int indiceBoiteEnergie = opponentPokemonActifVBox.getChildren().indexOf(energiePokemonActifAdversaireHBox); // Renamed
                    if (indiceBoiteEnergie != -1) {
                         opponentPokemonActifVBox.getChildren().add(indiceBoiteEnergie, etiquettePV);
                    } else {
                        // Repli : si la boite d'énergie n'est pas trouvée, essayer d'ajouter après le bouton ou à la fin
                        int indiceBouton = opponentPokemonActifVBox.getChildren().indexOf(opponentPokemonActifButton); // Renamed
                        if (indiceBouton != -1 && indiceBouton + 1 <= opponentPokemonActifVBox.getChildren().size() ) {
                            opponentPokemonActifVBox.getChildren().add(indiceBouton + 1, etiquettePV);
                        } else {
                            opponentPokemonActifVBox.getChildren().add(etiquettePV); // Ajouter à la fin
                        }
                    }

                    // Obtenir ICarte pour les propriétés additionnelles
                    ICarte carteDuPokemon = pokemonPourLiaison.getCartePokemon(); // Renamed

                    // Affichage de la Faiblesse
                    fr.umontpellier.iut.ptcgJavaFX.mecanique.Type typeFaiblesse = carteDuPokemon.getFaiblesse(); // Renamed
                    Label etiquetteFaiblesse = new Label(); // Renamed
                    etiquetteFaiblesse.setId("weaknessLabelOpponentActif");
                    etiquetteFaiblesse.getStyleClass().add("hp-label"); // Utilise le même style pour l'instant
                    if (typeFaiblesse != null) {
                        etiquetteFaiblesse.setText("Faiblesse : " + typeFaiblesse.name()); // Translated
                    } else {
                        etiquetteFaiblesse.setText("Faiblesse : Aucune"); // Translated
                    }
                    opponentPokemonActifVBox.getChildren().add(etiquetteFaiblesse);

                    // Affichage de la Résistance
                    fr.umontpellier.iut.ptcgJavaFX.mecanique.Type typeResistance = carteDuPokemon.getResistance(); // Renamed
                    Label etiquetteResistance = new Label(); // Renamed
                    etiquetteResistance.setId("resistanceLabelOpponentActif");
                    etiquetteResistance.getStyleClass().add("hp-label"); // Utilise le même style pour l'instant
                    if (typeResistance != null) {
                        etiquetteResistance.setText("Résistance : " + typeResistance.name()); // Translated
                    } else {
                        etiquetteResistance.setText("Résistance : Aucune"); // Translated
                    }
                    opponentPokemonActifVBox.getChildren().add(etiquetteResistance);

                    // Affichage du Coût de Retraite
                    int coutRetraiteVal = carteDuPokemon.getCoutRetraite(); // Renamed
                    Label etiquetteCoutRetraite = new Label(); // Renamed
                    etiquetteCoutRetraite.setId("retreatLabelOpponentActif");
                    etiquetteCoutRetraite.getStyleClass().add("hp-label"); // Utilise le même style pour l'instant
                    etiquetteCoutRetraite.setText("Retraite : " + coutRetraiteVal); // Translated
                    opponentPokemonActifVBox.getChildren().add(etiquetteCoutRetraite);

                    // Affichage des Conditions de Statut
                    HBox boiteStatutsH = new HBox(); // Renamed
                    boiteStatutsH.setId("statusBoxOpponentActif");
                    boiteStatutsH.setSpacing(5);

                    // Brûlé
                    Label etiqBRN = new Label("BRN"); // Renamed
                    etiqBRN.getStyleClass().add("status-label");
                    etiqBRN.visibleProperty().bind(pokemonPourLiaison.estBruleProperty());
                    boiteStatutsH.getChildren().add(etiqBRN);

                    // Empoisonné
                    Label etiqPSN = new Label("PSN"); // Renamed
                    etiqPSN.getStyleClass().add("status-label");
                    etiqPSN.visibleProperty().bind(pokemonPourLiaison.estEmpoisonneProperty());
                    boiteStatutsH.getChildren().add(etiqPSN);

                    // Endormi
                    Label etiqSLP = new Label("SLP"); // Renamed
                    etiqSLP.getStyleClass().add("status-label");
                    etiqSLP.visibleProperty().bind(pokemonPourLiaison.estEndormiProperty());
                    boiteStatutsH.getChildren().add(etiqSLP);

                    // Paralysé
                    Label etiqPAR = new Label("PAR"); // Renamed
                    etiqPAR.getStyleClass().add("status-label");
                    etiqPAR.visibleProperty().bind(pokemonPourLiaison.estParalyseProperty());
                    boiteStatutsH.getChildren().add(etiqPAR);

                    // Confus
                    Label etiqCNF = new Label("CNF"); // Renamed
                    etiqCNF.getStyleClass().add("status-label");
                    etiqCNF.visibleProperty().bind(pokemonPourLiaison.estConfusProperty());
                    boiteStatutsH.getChildren().add(etiqCNF);

                    opponentPokemonActifVBox.getChildren().add(boiteStatutsH);
                }
            } else {
                // Afficher le dos de carte ou effacer
                opponentPokemonActifButton.setGraphic(VueUtils.creerVueImagePourDosCarte(LARGEUR_POKEMON_ACTIF_ADVERSAIRE, HAUTEUR_POKEMON_ACTIF_ADVERSAIRE));
                opponentPokemonActifButton.setText(null);
                // S'assurer que l'étiquette PV est aussi effacée si pas de Pokémon actif
                if (opponentPokemonActifVBox != null) {
                    opponentPokemonActifVBox.getChildren().removeIf(noeud -> "hpLabelOpponentActif".equals(noeud.getId()));
                }
            }
        }
        mettreAJourEnergiesPokemonActifAdversaire();
    }

    private void peuplerBoiteEnergiesAdversaire(HBox boiteEnergiesCible, IPokemon pokemonConcerne) {
        if (boiteEnergiesCible == null || pokemonConcerne == null) {
            if (boiteEnergiesCible != null) {
                boiteEnergiesCible.getChildren().clear(); // Effacer si pas de pokémon
            }
            return;
        }
        boiteEnergiesCible.getChildren().clear();

        ObservableMap<String, List<String>> carteEnergies = pokemonConcerne.energieProperty();
        if (carteEnergies != null) {
            for (Map.Entry<String, List<String>> entree : carteEnergies.entrySet()) {
                String lettreType = entree.getKey();
                int nombre = entree.getValue().size();
                if (nombre > 0) {
                    Type typeEnumVal = null;
                    for (Type t : Type.values()) {
                        if (t.asLetter().equals(lettreType)) {
                            typeEnumVal = t;
                            break;
                        }
                    }
                    if (typeEnumVal != null) {
                        ImageView vueIconeEnergie = VueUtils.creerVueImagePourIconeEnergie(typeEnumVal, TAILLE_ICONE_ENERGIE_ADVERSAIRE);
                        Label etiquetteNombre = new Label("x" + nombre);
                        HBox boiteEntreeEnergie = new HBox(vueIconeEnergie, etiquetteNombre);
                        boiteEntreeEnergie.setSpacing(2); // Cohérent avec l'ancien code
                        boiteEntreeEnergie.setAlignment(Pos.CENTER); // Assurer un alignement cohérent
                        boiteEnergiesCible.getChildren().add(boiteEntreeEnergie);
                    } else { // Repli pour les lettres de type inconnues
                        Label etiquetteEnergie = new Label(lettreType + " x" + nombre);
                        etiquetteEnergie.getStyleClass().add("energy-tag"); // Style existant
                        boiteEnergiesCible.getChildren().add(etiquetteEnergie);
                    }
                }
            }
        }
    }

    private void mettreAJourEnergiesPokemonActifAdversaire() {
        IPokemon pokemonActifAdv = (this.joueurAdverse != null && this.joueurAdverse.pokemonActifProperty() != null) ? this.joueurAdverse.pokemonActifProperty().get() : null;
        peuplerBoiteEnergiesAdversaire(energiePokemonActifAdversaireHBox, pokemonActifAdv);
    }

    private Node creerVuePokemonBancAdversaire(IPokemon pokemon) { // Parameter 'pokemon' not translated (type)
        VBox conteneurCartePokemon = new VBox(2); // Renamed
        conteneurCartePokemon.getStyleClass().add("pokemon-node-display");
        conteneurCartePokemon.setAlignment(Pos.CENTER);
        if (pokemon != null && pokemon.getCartePokemon() != null && pokemon.getCartePokemon().getId() != null) {
            conteneurCartePokemon.setUserData(pokemon.getCartePokemon().getId()); // Stocker l'ID de la carte
        }

        ImageView vueImagePokemonBanc = VueUtils.creerVueImagePourCarte(pokemon.getCartePokemon(), LARGEUR_POKEMON_BANC_ADVERSAIRE, HAUTEUR_POKEMON_BANC_ADVERSAIRE); // Renamed
        Button boutonPokemon = new Button(); // Renamed
        boutonPokemon.setGraphic(vueImagePokemonBanc);
        boutonPokemon.getStyleClass().clear(); // Enlever le style de bouton par défaut
        boutonPokemon.getStyleClass().add("card-button-on-bench"); // Ajouter une classe personnalisée pour le style
        // Tooltip infobulle = new Tooltip(pokemon.getCartePokemon().getNom()); // Commentaire traduit
        // pokemonButton.setTooltip(infobulle);

        boutonPokemon.setOnAction(actionEvent -> {
            if (this.jeuEnCours != null && pokemon != null && pokemon.getCartePokemon() != null && pokemon.getCartePokemon().getId() != null) {
                this.jeuEnCours.carteSurTerrainCliquee(pokemon.getCartePokemon().getId());
            } else {
                System.err.println("Clic sur Pokémon de banc adverse, mais pas de Pokémon/carte/ID trouvé."); // Traduit
            }
        });

        // Étiquette PV pour les Pokémon du banc de l'adversaire
        Label etiquettePV = new Label(); // Renamed
        etiquettePV.getStyleClass().add("hp-label");
        etiquettePV.textProperty().bind(
            Bindings.createStringBinding(() -> {
                if (pokemon != null && pokemon.getCartePokemon() != null) { // Vérifier pokémon & carte
                    return "PV : " + pokemon.pointsDeVieProperty().get(); // Translated
                }
                return "PV : --"; // Translated
            }, pokemon.pointsDeVieProperty(), pokemon.cartePokemonProperty()) // Observer ces propriétés pour les changements
        );

        HBox boiteEnergiesH = new HBox(2); // Renamed
        boiteEnergiesH.setAlignment(Pos.CENTER);
        // L'ancien code de peuplement de boiteEnergiesH est retiré ici.
        // Appel à la nouvelle méthode utilitaire :
        peuplerBoiteEnergiesAdversaire(boiteEnergiesH, pokemon);

        conteneurCartePokemon.getChildren().addAll(boutonPokemon, etiquettePV, boiteEnergiesH);

        return conteneurCartePokemon;
    }

    private void placerBancAdversaire() {
        if (bancAdversaireHBox == null) return;
        bancAdversaireHBox.getChildren().clear();
        if (joueurAdverse != null && joueurAdverse.getBanc() != null) {
            for (IPokemon pokemonSurBanc : joueurAdverse.getBanc()) { // Renamed
                if (pokemonSurBanc != null && pokemonSurBanc.getCartePokemon() != null) {
                    bancAdversaireHBox.getChildren().add(creerVuePokemonBancAdversaire(pokemonSurBanc));
                }
            }
            // Pour les emplacements fixes (afficher des placeholders vides), boucler sur MAX_EMPLACEMENTS_BANC
            // et ajouter des placeholders si pokemon est null. Plus simple pour l'instant : n'afficher que les Pokémon réels.
        }
    }

    private void placerMainAdversaire() {
        if (panneauMainAdversaireHBox == null) return;
        panneauMainAdversaireHBox.getChildren().clear();
        if (joueurAdverse != null && joueurAdverse.getMain() != null) {
            int tailleMain = joueurAdverse.getMain().size(); // Renamed
            for (int i = 0; i < tailleMain; i++) {
                ImageView vueDosCarte = VueUtils.creerVueImagePourDosCarte(LARGEUR_CARTE_MAIN_ADVERSAIRE, HAUTEUR_CARTE_MAIN_ADVERSAIRE); // Renamed
                panneauMainAdversaireHBox.getChildren().add(vueDosCarte);
            }
        }
    }

    private void mettreAJourComptesCartesAdversaire() {
        if (joueurAdverse == null) {
            // Les étiquettes sont déjà mises à "N/D" ou similaire dans initialiserAffichage si adversaire est null.
            // S'assurer que les images sont aussi cachées.
            if (deckAdversaireImageView != null) deckAdversaireImageView.setVisible(false);
            if (prixAdversaireImageView != null) prixAdversaireImageView.setVisible(false);
            return;
        }

        mainAdversaireLabel.setText("Main Adv. : " + (joueurAdverse.getMain() != null ? joueurAdverse.getMain().size() : "0")); // Traduit

        int tailleDeLaPioche = (joueurAdverse.piocheProperty() != null) ? joueurAdverse.piocheProperty().size() : 0; // Renamed
        deckAdversaireLabel.setText("Pioche Adv. : " + tailleDeLaPioche); // Traduit
        if (deckAdversaireImageView != null) {
            deckAdversaireImageView.setVisible(tailleDeLaPioche > 0);
        }

        defausseAdversaireLabel.setText("Défausse Adv. : " + (joueurAdverse.defausseProperty() != null ? joueurAdverse.defausseProperty().size() : "0")); // Traduit

        int nombreDePrix = (joueurAdverse.recompensesProperty() != null) ? joueurAdverse.recompensesProperty().size() : 0; // Renamed
        prixAdversaireLabel.setText("Récomp. Adv. : " + nombreDePrix); // Traduit
        if (prixAdversaireImageView != null) {
            prixAdversaireImageView.setVisible(nombreDePrix > 0);
        }
    }

    @FXML
    void handleOpponentActivePokemonClick(ActionEvent event) {
        if (this.jeuEnCours != null && this.joueurAdverse != null) {
            IPokemon pokemonActifAdv = this.joueurAdverse.pokemonActifProperty().get(); // Renamed
            if (pokemonActifAdv != null && pokemonActifAdv.getCartePokemon() != null && pokemonActifAdv.getCartePokemon().getId() != null) {
                this.jeuEnCours.carteSurTerrainCliquee(pokemonActifAdv.getCartePokemon().getId());
            } else {
                // Optionnel: Gérer le cas où il n'y a pas de Pokémon actif cliquable
                System.err.println("Clic sur Pokémon actif adverse, mais pas de Pokémon/carte/ID trouvé."); // Traduit
            }
        }
    }

    private void actualiserStyleSelectionPokemonAdversaire(Carte carteActuellementSelectionnee) {
        String idCarteSelectionnee = (carteActuellementSelectionnee == null) ? null : carteActuellementSelectionnee.getId();

        // Pokémon Actif de l'adversaire
        if (opponentPokemonActifButton != null && this.joueurAdverse != null) {
            IPokemon pkmnActifAdv = this.joueurAdverse.pokemonActifProperty().get(); // Renamed
            if (pkmnActifAdv != null && pkmnActifAdv.getCartePokemon() != null && pkmnActifAdv.getCartePokemon().getId() != null) {
                if (pkmnActifAdv.getCartePokemon().getId().equals(idCarteSelectionnee)) {
                    opponentPokemonActifButton.getStyleClass().add("pokemon-selectionne");
                } else {
                    opponentPokemonActifButton.getStyleClass().removeAll("pokemon-selectionne");
                }
            } else {
                opponentPokemonActifButton.getStyleClass().removeAll("pokemon-selectionne"); // Pas de Pokémon actif, s'assurer qu'il n'a pas le style
            }
        } else if (opponentPokemonActifButton != null) { // S'assurer que le bouton est nettoyé si pas d'adversaire ou de bouton
             opponentPokemonActifButton.getStyleClass().removeAll("pokemon-selectionne");
        }


        // Pokémon du Banc de l'adversaire
        if (bancAdversaireHBox != null) {
            for (Node noeudPokemonBanc : bancAdversaireHBox.getChildren()) { // Renamed
                if (noeudPokemonBanc.getUserData() instanceof String) {
                    String idCarteNoeud = (String) noeudPokemonBanc.getUserData(); // Renamed
                    if (idCarteNoeud.equals(idCarteSelectionnee)) {
                        noeudPokemonBanc.getStyleClass().add("pokemon-selectionne");
                    } else {
                        noeudPokemonBanc.getStyleClass().removeAll("pokemon-selectionne");
                    }
                } else {
                    // Cas des nœuds non-pokemon (ne devraient pas avoir d'ID String comme UserData, ou autres types de nœuds)
                    noeudPokemonBanc.getStyleClass().removeAll("pokemon-selectionne");
                }
            }
        }
    }
}
