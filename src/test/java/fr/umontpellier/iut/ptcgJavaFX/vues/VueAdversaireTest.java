package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;

import javafx.application.Platform; // Added/Ensured
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel; // Added/Ensured
// import javafx.scene.Node; // Was present, might not be needed now
// import javafx.scene.Scene;
import javafx.scene.layout.VBox; // Added/Ensured
import javafx.scene.layout.HBox; // Added/Ensured
import javafx.scene.Parent; // Added/Ensured
import javafx.scene.control.Button; // Added/Ensured
// import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeAll; // Added/Ensured
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test; // Added/Ensured
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch; // Added
import java.util.concurrent.TimeUnit; // Added

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn; // Added import

@ExtendWith(MockitoExtension.class)
public class VueAdversaireTest {

    @Mock private IJeu mockJeu;
    @Mock private IJoueur mockOpponent;
    @Mock private IPokemon mockOpponentActivePokemon;
    @Mock private ICarte mockOpponentActivePokemonCarte;
    @Mock private IPokemon mockOpponentBenchPokemon;
    @Mock private ICarte mockOpponentBenchPokemonCarte;

    private SimpleObjectProperty<IPokemon> opponentActivePokemonProperty;
    private ObservableList<IPokemon> opponentBenchList;
    private ObservableList<ICarte> opponentHandList;
    private ObservableList<ICarte> opponentDeckList;
    private ObservableList<ICarte> opponentDiscardList;
    private ObservableList<ICarte> opponentPrizesList;

    private VueAdversaire vueAdversaire;

    @BeforeAll
    static void initToolkit() {
        new JFXPanel(); // Initializes JavaFX environment
    }

    @BeforeEach
    void setUp() throws InterruptedException { // Added throws InterruptedException
        opponentActivePokemonProperty = new SimpleObjectProperty<>(null);
        opponentBenchList = FXCollections.observableArrayList();
        opponentHandList = FXCollections.observableArrayList();
        opponentDeckList = FXCollections.observableArrayList();
        opponentDiscardList = FXCollections.observableArrayList();
        opponentPrizesList = FXCollections.observableArrayList();

        when(mockOpponent.getNom()).thenReturn("Test Opponent"); // This one is likely fine as String has no complex generics
        doReturn(opponentActivePokemonProperty).when(mockOpponent).pokemonActifProperty();
        doReturn(opponentBenchList).when(mockOpponent).getBanc();
        doReturn(opponentHandList).when(mockOpponent).getMain();
        doReturn(opponentDeckList).when(mockOpponent).piocheProperty();
        doReturn(opponentDiscardList).when(mockOpponent).defausseProperty();
        doReturn(opponentPrizesList).when(mockOpponent).recompensesProperty();

        doReturn(mockOpponentActivePokemonCarte).when(mockOpponentActivePokemon).getCartePokemon();
        when(mockOpponentActivePokemonCarte.getNom()).thenReturn("OpponentPika"); // Fine
        when(mockOpponentActivePokemonCarte.getId()).thenReturn("opponentActiveCardId_test"); // Fine

        doReturn(mockOpponentBenchPokemonCarte).when(mockOpponentBenchPokemon).getCartePokemon();
        when(mockOpponentBenchPokemonCarte.getNom()).thenReturn("OpponentMagikarp"); // Fine
        when(mockOpponentBenchPokemonCarte.getId()).thenReturn("opponentBenchCardId_test"); // Fine
        doReturn(FXCollections.observableHashMap()).when(mockOpponentBenchPokemon).energieProperty();

        // Instantiate VueAdversaire on JavaFX thread
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                vueAdversaire = new VueAdversaire();
                vueAdversaire.setJeu(mockJeu);
                vueAdversaire.setAdversaire(mockOpponent);
                // Assuming VueAdversaire constructor or an internal initialize method handles bindings.
                // If explicit call to creerBindings() or postInit() is needed, it would go here.
                // vueAdversaire.creerBindings();
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(5, TimeUnit.SECONDS)) {
            fail("Timeout waiting for VueAdversaire instantiation on FX thread");
        }
    }

    @Test
    void testOpponentActivePokemonAndBenchAreOnSameLine() {
        assertNotNull(vueAdversaire, "vueAdversaire should be initialized.");
        Button opponentPokemonActifButton = (Button) vueAdversaire.lookup("#opponentPokemonActifButton");
        HBox bancAdversaireHBox = (HBox) vueAdversaire.lookup("#bancAdversaireHBox");

        assertNotNull(opponentPokemonActifButton, "Opponent active Pokemon button should exist.");
        assertNotNull(bancAdversaireHBox, "Opponent bench HBox should exist.");

        Parent parentActivePokemon = opponentPokemonActifButton.getParent();
        Parent parentBench = bancAdversaireHBox.getParent();

        assertNotNull(parentActivePokemon, "Parent of opponent active Pokemon button should not be null.");
        assertNotNull(parentBench, "Parent of opponent bench HBox should not be null.");

        assertTrue(parentActivePokemon instanceof VBox, "Parent of opponent active Pokemon button should be a VBox.");
        assertTrue(parentBench instanceof VBox, "Parent of opponent bench HBox should be a VBox.");

        assertNotNull(parentActivePokemon.getParent(), "Grandparent of opponent active Pokemon button should not be null.");
        assertTrue(parentActivePokemon.getParent() instanceof HBox, "Grandparent of opponent active Pokemon (parent of its VBox) should be an HBox.");
        assertSame(parentActivePokemon.getParent(), parentBench.getParent(), "The VBox of opponent active Pokemon and the VBox of opponent bench should share the same parent HBox.");
    }

    // Other tests remain commented out as per original state and subtask focus.
    // @Test
    // void testFxmlLoadingAndFieldInjection() { ... }
    // @Test
    // void testInitialDisplayCountsAndName() { ... }
    // @Test
    // void testActivePokemonDisplayUpdate() { ... }
    // @Test
    // void testOpponentBenchIsVisibleAndShowsPokemon() throws InterruptedException { ... }
    // @Test
    // void testOpponentActivePokemonClickActionShouldTriggerJeuInteraction() { ... }
    // @Test
    // void testOpponentBenchedPokemonClickActionShouldTriggerJeuInteraction() { ... }
}
