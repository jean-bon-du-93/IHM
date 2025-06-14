package fr.umontpellier.iut.ptcgJavaFX;

import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class RetreatTest {

    @Mock
    private IJeu jeu;
    @Mock
    private IJoueur joueurActif;
    @Mock
    private IPokemon pokemonActif;
    @Mock
    private IPokemon pokemonBanc1;
    @Mock
    private ICarte cartePokemonActif;
    @Mock
    private ICarte cartePokemonBanc1;

    private ObjectProperty<IPokemon> pokemonActifProperty;
    private ObjectProperty<IJoueur> joueurActifProperty;
    private ObjectProperty<ICarte> cartePokemonActifProperty;
    private ObjectProperty<ICarte> cartePokemonBanc1Property;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup ObjectProperties for mocking
        pokemonActifProperty = new SimpleObjectProperty<>(pokemonActif);
        joueurActifProperty = new SimpleObjectProperty<>(joueurActif);
        cartePokemonActifProperty = new SimpleObjectProperty<>(cartePokemonActif);
        cartePokemonBanc1Property = new SimpleObjectProperty<>(cartePokemonBanc1);


        // Configure IJoueur mock
        when(joueurActif.pokemonActifProperty()).thenReturn((ObjectProperty)pokemonActifProperty);
        ObservableList<IPokemon> banc = FXCollections.observableArrayList(pokemonBanc1);
        when(joueurActif.getBanc()).thenReturn((ObservableList)banc);
        when(joueurActif.peutRetraiteProperty()).thenReturn(new SimpleBooleanProperty(true));
        when(joueurActif.carteEnJeuProperty()).thenReturn(new SimpleObjectProperty<>(null));

        // Configure IJeu mock
        when(jeu.joueurActifProperty()).thenReturn((ObjectProperty)joueurActifProperty);
        when(jeu.getJoueurs()).thenReturn(new IJoueur[]{joueurActif});

        // Configure IPokemon mocks
        when(pokemonActif.getCartePokemon()).thenReturn(cartePokemonActif);
        when(pokemonActif.cartePokemonProperty()).thenReturn((ObjectProperty)cartePokemonActifProperty);
        when(pokemonBanc1.getCartePokemon()).thenReturn(cartePokemonBanc1);
        when(pokemonBanc1.cartePokemonProperty()).thenReturn((ObjectProperty)cartePokemonBanc1Property);


        // Configure ICarte mocks
        when(cartePokemonActif.getId()).thenReturn("pkmn_active_card");
        when(cartePokemonBanc1.getId()).thenReturn("pkmn_bench_1_card");
    }

    @Test
    void testRetreatAction() {
        // Simulate Game Flow for Retreat
        IJoueur currentJoueurActif = jeu.joueurActifProperty().get();
        jeu.retraiteAEteChoisie();

        IPokemon benchedPokemon1 = currentJoueurActif.getBanc().get(0);
        jeu.uneCarteDeLaMainAEteChoisie(benchedPokemon1.getCartePokemon().getId());

        // Verify Interactions
        verify(jeu, times(1)).retraiteAEteChoisie();
        verify(jeu, times(1)).uneCarteDeLaMainAEteChoisie("pkmn_bench_1_card");
    }
}
