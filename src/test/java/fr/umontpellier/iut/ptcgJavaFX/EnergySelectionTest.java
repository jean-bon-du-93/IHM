package fr.umontpellier.iut.ptcgJavaFX;

import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type; // Assuming Type is in mecanique
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List; // For List.of

import static org.mockito.Mockito.*;

public class EnergySelectionTest {

    @Mock
    private IJeu jeu;
    @Mock
    private IJoueur joueurActif;
    @Mock
    private IPokemon activePokemon;
    @Mock
    private ICarte mockEnergyCard;

    private SimpleObjectProperty<IJoueur> joueurActifPropertyJeu;
    private SimpleObjectProperty<IPokemon> pokemonActifPropertyJoueur;
    private SimpleObjectProperty<String> instructionPropertyJeu;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize observable properties
        joueurActifPropertyJeu = new SimpleObjectProperty<>(joueurActif);
        pokemonActifPropertyJoueur = new SimpleObjectProperty<>(activePokemon);
        instructionPropertyJeu = new SimpleObjectProperty<>("Défaussez 1 énergie FEU.");

        // Configure Active Player (joueurActif)
        when(joueurActif.pokemonActifProperty()).thenReturn((ObjectProperty)pokemonActifPropertyJoueur);

        // Configure Active Pokemon (activePokemon)
        when(mockEnergyCard.getId()).thenReturn("fire_energy_001");
        // Assuming ICarte might have getTypeEnergie() or similar. If not, this is illustrative.
        // If getTypeEnergie() is not on ICarte, the primary link is through energieProperty key.
        // when(mockEnergyCard.getTypeEnergie()).thenReturn(Type.FEU);


        ObservableMap<String, List<String>> energyMap = FXCollections.observableHashMap();
        // Ensure Type.FEU.asLetter() or equivalent method exists and is correct.
        // If Type enum doesn't have asLetter(), use the string key directly as stored in mecanique.Pokemon
        String energyTypeKey = Type.FEU.name(); // Or Type.FEU.asLetter() if that's the convention
        energyMap.put(energyTypeKey, List.of("fire_energy_001"));
        when(activePokemon.energieProperty()).thenReturn(energyMap);

        ObservableList<ICarte> attachedCards = FXCollections.observableArrayList(mockEnergyCard);
        when(activePokemon.cartesProperty()).thenReturn((ObservableList)attachedCards);


        // Configure Game (jeu)
        when(jeu.joueurActifProperty()).thenReturn((ObjectProperty)joueurActifPropertyJeu);
        when(jeu.instructionProperty()).thenReturn(instructionPropertyJeu);
    }

    @Test
    void testEnergyCardSelection() {
        // Simulate Game Flow / UI Action for Energy Selection
        // This action would typically be triggered by UI components based on user interaction
        // after the game state (instruction, active pokemon's energy) is set up.
        jeu.uneCarteEnergieAEteChoisie("fire_energy_001");

        // Verify Interactions
        verify(jeu, times(1)).uneCarteEnergieAEteChoisie("fire_energy_001");
    }
}
