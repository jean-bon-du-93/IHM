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
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito; // Added import
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
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

    // @BeforeAll
    // static void initToolkit() {
    //     new JFXPanel(); // Initializes JavaFX environment
    // }

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
        when(mockOpponent.pokemonActifProperty()).thenReturn((SimpleObjectProperty)opponentActivePokemonProperty);
        when(mockOpponent.getBanc()).thenReturn((ObservableList)opponentBenchList); // Returns ObservableList
        when(mockOpponent.getMain()).thenReturn((ObservableList)opponentHandList); // Returns ObservableList
        when(mockOpponent.piocheProperty()).thenReturn((ObservableList)opponentDeckList); // Returns ObservableList (or ListProperty)
        when(mockOpponent.defausseProperty()).thenReturn((ObservableList)opponentDiscardList);
        when(mockOpponent.recompensesProperty()).thenReturn((ObservableList)opponentPrizesList);

        // Mock active Pokemon details
        when(mockOpponentActivePokemon.getCartePokemon()).thenReturn(mockOpponentActivePokemonCarte);
        when(mockOpponentActivePokemonCarte.getNom()).thenReturn("OpponentPika");
        when(mockOpponentActivePokemonCarte.getId()).thenReturn("opponentActiveCardId_test"); // Added mock

        // Mock bench Pokemon details (optional for initial setup, but good for bench test)
        when(mockOpponentBenchPokemon.getCartePokemon()).thenReturn(mockOpponentBenchPokemonCarte);
        when(mockOpponentBenchPokemonCarte.getNom()).thenReturn("OpponentMagikarp");
        when(mockOpponentBenchPokemonCarte.getId()).thenReturn("opponentBenchCardId_test"); // Added mock
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
    void testFxmlLoadingAndFieldInjection() throws InterruptedException { // Added throws InterruptedException
        assertNotNull(vueAdversaire.nomAdversaireLabel, "nomAdversaireLabel should be injected");
        assertNotNull(vueAdversaire.opponentPokemonActifButton, "opponentPokemonActifButton should be injected"); // Changed field name
        assertNotNull(vueAdversaire.bancAdversaireHBox, "bancAdversaireHBox should be injected");
        assertNotNull(vueAdversaire.mainAdversaireLabel, "mainAdversaireLabel should be injected");
        assertNotNull(vueAdversaire.deckAdversaireLabel, "deckAdversaireLabel should be injected");
        assertNotNull(vueAdversaire.defausseAdversaireLabel, "defausseAdversaireLabel should be injected");
        assertNotNull(vueAdversaire.prixAdversaireLabel, "prixAdversaireLabel should be injected");
    }

    @Test
    void testInitialDisplayCountsAndName() throws InterruptedException { // Added throws InterruptedException
        assertEquals("Test Opponent", vueAdversaire.nomAdversaireLabel.getText());
        // Active Pokemon initially null
        assertEquals("Aucun", vueAdversaire.opponentPokemonActifButton.getText(), "Initial active Pokemon should be 'Aucun'"); // Changed field name

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
        assertEquals("OpponentPika", vueAdversaire.opponentPokemonActifButton.getText()); // Changed field name

        Platform.runLater(() -> {
            opponentActivePokemonProperty.set(null); // Remove active Pokemon
        });
        Thread.sleep(500);
        assertEquals("Aucun", vueAdversaire.opponentPokemonActifButton.getText()); // Changed field name
    }

    @Test
    void testOpponentBenchIsVisibleAndShowsPokemon() throws InterruptedException {
        // 1. Verify that vueAdversaire.bancAdversaireHBox is not null after setup.
        assertNotNull(vueAdversaire.bancAdversaireHBox, "Opponent bench HBox should not be null after setup");

        // 2. Add mockOpponentBenchPokemon to the opponentBenchList.
        Platform.runLater(() -> {
            opponentBenchList.add(mockOpponentBenchPokemon);
        });

        // 3. Use Platform.runLater to trigger the UI update and Thread.sleep(500) to wait.
        Thread.sleep(500); // Wait for UI update

        // 4. Assert that vueAdversaire.bancAdversaireHBox.isVisible() is true.
        assertTrue(vueAdversaire.bancAdversaireHBox.isVisible(), "Opponent bench HBox should be visible");

        // 5. Assert that vueAdversaire.bancAdversaireHBox.getChildren() is not empty.
        assertFalse(vueAdversaire.bancAdversaireHBox.getChildren().isEmpty(), "Opponent bench HBox should have children after adding a Pokemon");

        // 6. Get the first child node from bancAdversaireHBox.getChildren().
        Node childNode = vueAdversaire.bancAdversaireHBox.getChildren().get(0);

        // 7. Assert that this child node is not null and childNode.isVisible() is true.
        assertNotNull(childNode, "Child node (pokemon VBox) in bench should not be null");
        assertTrue(childNode.isVisible(), "Child node (pokemon VBox) in bench should be visible");

        // 8. Cast the child node to javafx.scene.layout.VBox.
        assertTrue(childNode instanceof VBox, "Child node should be an instance of VBox");
        VBox pokemonNodeVBox = (VBox) childNode;

        // 9. Assert that this VBox has children (e.g., the Pokémon name Label).
        assertFalse(pokemonNodeVBox.getChildren().isEmpty(), "Pokemon VBox should have children (e.g., name Label)");

        // 10. Get the first child of the VBox (expected to be a javafx.scene.control.Label) and assert that it is also visible.
        Node labelNode = pokemonNodeVBox.getChildren().get(0);
        assertNotNull(labelNode, "Label node in Pokemon VBox should not be null");
        assertTrue(labelNode.isVisible(), "Label node in Pokemon VBox should be visible");
        // Optionally, check the label text if needed, e.g.
        // assertTrue(labelNode instanceof javafx.scene.control.Label);
        // assertEquals("OpponentMagikarp", ((javafx.scene.control.Label) labelNode).getText());
    }

    @Test
    void testOpponentActivePokemonClickActionShouldTriggerJeuInteraction() {
        // This test defines that a future click handler for the opponent's active Pokemon
        // in VueAdversaire.java should ultimately call uneCarteDeLaMainAEteChoisie
        // on the IJeu instance with the specific card ID.
        // This test WILL FAIL until that functionality is implemented.
        Mockito.verify(mockJeu).uneCarteDeLaMainAEteChoisie("opponentActiveCardId_test");
    }

    @Test
    void testOpponentBenchedPokemonClickActionShouldTriggerJeuInteraction() {
        // This test defines that a future click handler for an opponent's benched Pokemon
        // in VueAdversaire.java should ultimately call uneCarteDeLaMainAEteChoisie
        // on the IJeu instance with the specific card ID.
        // This test WILL FAIL until that functionality is implemented.
        Mockito.verify(mockJeu).uneCarteDeLaMainAEteChoisie("opponentBenchCardId_test");
    }
}
