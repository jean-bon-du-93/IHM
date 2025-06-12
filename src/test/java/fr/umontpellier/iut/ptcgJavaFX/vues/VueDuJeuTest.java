package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur; // Required for VueJoueurActif
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VueDuJeuTest {

    @Mock
    private IJeu mockJeu;
    @Mock
    private IJoueur mockJoueur; // For VueJoueurActif's needs if its init is triggered

    // To mock properties that VueDuJeu and VueJoueurActif will interact with
    private SimpleStringProperty instructionProperty;
    private SimpleObjectProperty<IJoueur> joueurActifPropertyJeu;


    @BeforeAll // Temporarily commented to avoid JavaFX initialization issues
    static void initToolkit() {
        new JFXPanel(); // Initializes JavaFX environment to prevent IllegalStateException
    }

    @BeforeEach
    void setUp() {
        // Initialize properties for mockJeu
        instructionProperty = new SimpleStringProperty("Initial instruction");
        joueurActifPropertyJeu = new SimpleObjectProperty<>(null); // Initially no active player

        // Define default behavior for mockJeu properties
        // These are crucial as VueDuJeu's initialize and creerBindings will try to access them
        doReturn(instructionProperty).when(mockJeu).instructionProperty();

        // VueJoueurActif (created by VueDuJeu's FXML) will need joueurActifProperty from IJeu
        // and the player will need its own properties.
        // For simplicity in VueDuJeuTest, we mainly care that VueJoueurActif can be instantiated.
        // Its detailed testing is for VueJoueurActifTest.
        // So, mockJeu.joueurActifProperty() is needed by VueJoueurActif.postInit() -> lierAuJoueurActifDuJeu()
        doReturn(joueurActifPropertyJeu).when(mockJeu).joueurActifProperty();
    }

    @Test
    void testFxmlLoadingAndFieldInjection() throws InterruptedException {
        final VueDuJeu[] vueDuJeu = new VueDuJeu[1];
        Platform.runLater(() -> {
            vueDuJeu[0] = new VueDuJeu(mockJeu);
            assertNotNull(vueDuJeu[0].instructionLabel, "instructionLabel should be injected by FXML in VueDuJeu");
            assertNotNull(vueDuJeu[0].panneauDuJoueurActif, "panneauDuJoueurActif should be injected by FXML in VueDuJeu");
            // assertNotNull(vueDuJeu[0].boutonPasserVueDuJeu, "boutonPasserVueDuJeu should be injected by FXML"); // Removed as button is gone
        });
        Thread.sleep(500); // Allow Platform.runLater to execute
    }

    @Test
    void testCreerBindingsInstructionLabel() throws InterruptedException {
        final VueDuJeu[] vueDuJeu = new VueDuJeu[1];
        Platform.runLater(() -> {
            vueDuJeu[0] = new VueDuJeu(mockJeu); // This calls initialize(), which calls creerBindings()
            // Check initial text
            org.junit.jupiter.api.Assertions.assertEquals("Initial instruction", vueDuJeu[0].instructionLabel.getText(), "Instruction label should display initial text.");
            // Change property and check if label updates
            instructionProperty.set("New instruction");
            org.junit.jupiter.api.Assertions.assertEquals("New instruction", vueDuJeu[0].instructionLabel.getText(), "Instruction label should update when property changes.");
        });
        Thread.sleep(500); // Allow Platform.runLater to execute
    }

    // Test for actionPasserVueDuJeu removed as the button and its handler are no longer in VueDuJeu
}
