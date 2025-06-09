package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VueJoueurActifTest {

    @Mock
    private IJeu mockJeu;
    @Mock
    private IJoueur mockJoueurActif;
    @Mock
    private IPokemon mockPokemonActifPokemon, mockBenchPokemon1;
    @Mock
    private ICarte mockPokemonActifCarte, mockMainCarte1, mockBenchCarte1;

    private VueJoueurActif vueJoueurActif;

    // Observable properties for mocking
    private SimpleObjectProperty<IJoueur> joueurActifPropertyJeu;
    private SimpleObjectProperty<IPokemon> pokemonActifPropertyJoueur;
    private ObservableList<ICarte> mainDuJoueurList;
    private ObservableList<IPokemon> bancDuJoueurList;


    @BeforeAll
    static void initToolkit() {
        new JFXPanel(); // Initializes JavaFX environment
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        // Initialize observable properties
        joueurActifPropertyJeu = new SimpleObjectProperty<>(null);
        pokemonActifPropertyJoueur = new SimpleObjectProperty<>(null);
        mainDuJoueurList = FXCollections.observableArrayList();
        bancDuJoueurList = FXCollections.observableArrayList();

        // Configure mock IJoueur properties
        when(mockJoueurActif.pokemonActifProperty()).thenReturn(pokemonActifPropertyJoueur);
        when(mockJoueurActif.getMain()).thenReturn(mainDuJoueurList);
        when(mockJoueurActif.getBanc()).thenReturn(bancDuJoueurList);
        when(mockJoueurActif.getNom()).thenReturn("Test Player");


        // Configure mock IJeu to return the joueurActifProperty
        when(mockJeu.joueurActifProperty()).thenReturn(joueurActifPropertyJeu);

        // Instantiate VueJoueurActif on FX thread
        Platform.runLater(() -> {
            vueJoueurActif = new VueJoueurActif(); // Loads its own FXML
            vueJoueurActif.setJeu(mockJeu);      // Set the game instance
            vueJoueurActif.postInit();          // Initialize listeners and bindings
        });
        Thread.sleep(500); // Allow Platform.runLater to complete and FXML to load
    }

    @Test
    void testFxmlLoadingAndFieldInjection() {
        assertNotNull(vueJoueurActif.nomDuJoueurLabel, "nomDuJoueurLabel should be injected");
        assertNotNull(vueJoueurActif.pokemonActifLabel, "pokemonActifLabel should be injected");
        assertNotNull(vueJoueurActif.panneauMainHBox, "panneauMainHBox should be injected");
        assertNotNull(vueJoueurActif.panneauBancHBox, "panneauBancHBox should be injected");
        assertNotNull(vueJoueurActif.passerButton, "passerButton should be injected");
    }

    @Test
    void testInitialDisplayWithNoPlayer() {
        assertEquals("Pas de joueur actif", vueJoueurActif.nomDuJoueurLabel.getText());
        assertEquals("Aucun Pokémon actif", vueJoueurActif.pokemonActifLabel.getText());
        assertTrue(vueJoueurActif.panneauMainHBox.getChildren().isEmpty());
        assertTrue(vueJoueurActif.panneauBancHBox.getChildren().isEmpty());
    }

    @Test
    void testDisplayWhenPlayerBecomesActive() throws InterruptedException {
        // Mock Pokemon actif details
        when(mockPokemonActifPokemon.getCartePokemon()).thenReturn(mockPokemonActifCarte);
        when(mockPokemonActifCarte.getNom()).thenReturn("Pikachu");
        pokemonActifPropertyJoueur.set(mockPokemonActifPokemon); // Set Pokemon actif for player

        // Mock Main card details
        when(mockMainCarte1.getNom()).thenReturn("Potion");
        mainDuJoueurList.add(mockMainCarte1);

        // Mock Bench Pokemon details
        when(mockBenchPokemon1.getCartePokemon()).thenReturn(mockBenchCarte1);
        when(mockBenchCarte1.getNom()).thenReturn("Magikarp");
        bancDuJoueurList.add(mockBenchPokemon1);

        Platform.runLater(() -> {
            joueurActifPropertyJeu.set(mockJoueurActif); // Set player as active in the game
        });
        Thread.sleep(500); // Allow listeners to fire and UI to update

        assertEquals("C'est au tour de : Test Player", vueJoueurActif.nomDuJoueurLabel.getText());
        assertEquals("Pokémon actif : Pikachu", vueJoueurActif.pokemonActifLabel.getText());
        assertEquals(1, vueJoueurActif.panneauMainHBox.getChildren().size(), "Main should have 1 card");
        assertEquals("Potion", ((Button) vueJoueurActif.panneauMainHBox.getChildren().get(0)).getText());
        assertEquals(1, vueJoueurActif.panneauBancHBox.getChildren().size(), "Bench should have 1 Pokemon");
        assertEquals("Magikarp", ((Button) vueJoueurActif.panneauBancHBox.getChildren().get(0)).getText());
    }

    @Test
    void testPlacerMainButtonAction() throws InterruptedException {
        when(mockMainCarte1.getNom()).thenReturn("PotionCard");
        when(mockMainCarte1.getId()).thenReturn("potion1");
        mainDuJoueurList.add(mockMainCarte1);

        Platform.runLater(() -> joueurActifPropertyJeu.set(mockJoueurActif));
        Thread.sleep(500);

        assertFalse(vueJoueurActif.panneauMainHBox.getChildren().isEmpty(), "Main HBox should not be empty");
        Button cardButton = (Button) vueJoueurActif.panneauMainHBox.getChildren().get(0);

        Platform.runLater(cardButton::fire);
        Thread.sleep(500);

        verify(mockJeu).uneCarteDeLaMainAEteChoisie("potion1");
    }

    @Test
    void testPlacerBancButtonAction() throws InterruptedException {
        when(mockBenchPokemon1.getCartePokemon()).thenReturn(mockBenchCarte1);
        when(mockBenchCarte1.getNom()).thenReturn("BenchMon");
        when(mockBenchCarte1.getId()).thenReturn("benchMon1"); // ICarte has getId()
        bancDuJoueurList.add(mockBenchPokemon1);

        Platform.runLater(() -> joueurActifPropertyJeu.set(mockJoueurActif));
        Thread.sleep(500);

        assertFalse(vueJoueurActif.panneauBancHBox.getChildren().isEmpty(), "Banc HBox should not be empty");
        Button benchPokemonButton = (Button) vueJoueurActif.panneauBancHBox.getChildren().get(0);

        Platform.runLater(benchPokemonButton::fire);
        Thread.sleep(500);

        verify(mockJeu).unPokemonDuBancAEteChoisi("benchMon1");
    }


    @Test
    void testActionPasserParDefaut() throws InterruptedException {
         // Ensure vueJoueurActif.jeu is set, which is done in setUp()
        Platform.runLater(() -> {
             // Directly call the FXML action handler method
            vueJoueurActif.actionPasserParDefaut(new ActionEvent());
            verify(mockJeu).passerAEteChoisi();
        });
        Thread.sleep(500);
    }
}
