package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VueAdversaireTest {

    @Mock private IJeu mockJeu;
    @Mock private IJoueur mockOpponent;
    @Mock private IPokemon mockOpponentActivePokemon;
    @Mock private ICarte mockOpponentActivePokemonCarte;
    @Mock private IPokemon mockOpponentBenchPokemon;
    @Mock private ICarte mockOpponentBenchPokemonCarte;

    // Properties and Lists for mocking IJoueur
    private SimpleObjectProperty<IPokemon> opponentActivePokemonProperty;
    private ObservableList<IPokemon> opponentBenchList;
    private ObservableList<ICarte> opponentHandList;
    private ObservableList<ICarte> opponentDeckList; // For piocheProperty
    private ObservableList<ICarte> opponentDiscardList; // For defausseProperty
    private ObservableList<ICarte> opponentPrizesList; // For recompensesProperty

    private VueAdversaire vueAdversaire;

    @BeforeAll
    static void initToolkit() {
        new JFXPanel(); // Initializes JavaFX environment
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        // Initialize observable properties & lists
        opponentActivePokemonProperty = new SimpleObjectProperty<>(null);
        opponentBenchList = FXCollections.observableArrayList();
        opponentHandList = FXCollections.observableArrayList();
        opponentDeckList = FXCollections.observableArrayList();
        opponentDiscardList = FXCollections.observableArrayList();
        opponentPrizesList = FXCollections.observableArrayList();

        // Setup mocks for IJoueur methods
        when(mockOpponent.getNom()).thenReturn("Test Opponent");
        when(mockOpponent.pokemonActifProperty()).thenReturn(opponentActivePokemonProperty);
        when(mockOpponent.getBanc()).thenReturn(opponentBenchList); // Returns ObservableList
        when(mockOpponent.getMain()).thenReturn(opponentHandList); // Returns ObservableList
        when(mockOpponent.piocheProperty()).thenReturn(opponentDeckList); // Returns ObservableList (or ListProperty)
        when(mockOpponent.defausseProperty()).thenReturn(opponentDiscardList);
        when(mockOpponent.recompensesProperty()).thenReturn(opponentPrizesList);

        // Mock active Pokemon details
        when(mockOpponentActivePokemon.getCartePokemon()).thenReturn(mockOpponentActivePokemonCarte);
        when(mockOpponentActivePokemonCarte.getNom()).thenReturn("OpponentPika");

        // Mock bench Pokemon details (optional for initial setup, but good for bench test)
        when(mockOpponentBenchPokemon.getCartePokemon()).thenReturn(mockOpponentBenchPokemonCarte);
        when(mockOpponentBenchPokemonCarte.getNom()).thenReturn("OpponentMagikarp");
        // Mock the energieProperty for benched pokemon as it's accessed in creerPokemonBancNode
        when(mockOpponentBenchPokemon.energieProperty()).thenReturn(FXCollections.observableHashMap());


        Platform.runLater(() -> {
            vueAdversaire = new VueAdversaire(); // Loads FXML
            vueAdversaire.setJeu(mockJeu);
            vueAdversaire.setAdversaire(mockOpponent); // Triggers initial display
        });
        Thread.sleep(500); // Allow Platform.runLater & FXML loading to execute
    }

    @Test
    void testFxmlLoadingAndFieldInjection() {
        assertNotNull(vueAdversaire.nomAdversaireLabel, "nomAdversaireLabel should be injected");
        assertNotNull(vueAdversaire.pokemonActifAdversaireLabel, "pokemonActifAdversaireLabel should be injected");
        assertNotNull(vueAdversaire.bancAdversaireHBox, "bancAdversaireHBox should be injected");
        assertNotNull(vueAdversaire.mainAdversaireLabel, "mainAdversaireLabel should be injected");
        assertNotNull(vueAdversaire.deckAdversaireLabel, "deckAdversaireLabel should be injected");
        assertNotNull(vueAdversaire.defausseAdversaireLabel, "defausseAdversaireLabel should be injected");
        assertNotNull(vueAdversaire.prixAdversaireLabel, "prixAdversaireLabel should be injected");
    }

    @Test
    void testInitialDisplayCountsAndName() {
        assertEquals("Test Opponent", vueAdversaire.nomAdversaireLabel.getText());
        // Active Pokemon initially null
        assertEquals("Aucun", vueAdversaire.pokemonActifAdversaireLabel.getText(), "Initial active Pokemon should be 'Aucun'");

        // Card counts
        opponentHandList.add(mockOpponentActivePokemonCarte); // Add 1 card to hand
        opponentDeckList.addAll(mockOpponentActivePokemonCarte, mockOpponentActivePokemonCarte); // 2 to deck
        opponentDiscardList.addAll(mockOpponentActivePokemonCarte, mockOpponentActivePokemonCarte, mockOpponentActivePokemonCarte); // 3 to discard
        opponentPrizesList.addAll(mockOpponentActivePokemonCarte, mockOpponentActivePokemonCarte, mockOpponentActivePokemonCarte, mockOpponentActivePokemonCarte); // 4 prizes

        Platform.runLater(() -> {
             // Trigger re-evaluation of counts if not done automatically by setAdversaire for initial lists
            vueAdversaire.setAdversaire(mockOpponent); // Re-set to trigger updates based on new list sizes
        });
        Thread.sleep(500);

        assertEquals("Main Adv.: 1", vueAdversaire.mainAdversaireLabel.getText());
        assertEquals("Deck Adv.: 2", vueAdversaire.deckAdversaireLabel.getText());
        assertEquals("Défausse Adv.: 3", vueAdversaire.defausseAdversaireLabel.getText());
        assertEquals("Prix Adv.: 4", vueAdversaire.prixAdversaireLabel.getText());
    }

    @Test
    void testActivePokemonDisplayUpdate() throws InterruptedException {
        Platform.runLater(() -> {
            opponentActivePokemonProperty.set(mockOpponentActivePokemon);
        });
        Thread.sleep(500);
        assertEquals("OpponentPika", vueAdversaire.pokemonActifAdversaireLabel.getText());

        Platform.runLater(() -> {
            opponentActivePokemonProperty.set(null); // Remove active Pokemon
        });
        Thread.sleep(500);
        assertEquals("Aucun", vueAdversaire.pokemonActifAdversaireLabel.getText());
    }

    @Test
    void testBenchDisplaySimplified() throws InterruptedException {
        assertTrue(vueAdversaire.bancAdversaireHBox.getChildren().isEmpty(), "Bench should initially be empty");

        Platform.runLater(() -> {
            opponentBenchList.add(mockOpponentBenchPokemon); // Add one Pokemon to bench
        });
        Thread.sleep(500); // Allow listener to process and update UI

        assertEquals(1, vueAdversaire.bancAdversaireHBox.getChildren().size(), "Bench HBox should have 1 child (VBox container)");
        // Further checks could inspect the child node, e.g., the text of the Label within it.
    }
}
