package fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Pokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
// import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.EtatChoixEnergiePourEnergyGrounding; // Assume this state will be created
// import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.TourNormal; // Or other appropriate next state

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class EtatChoixUtiliserEnergyGroundingTest {

    @Mock
    private Joueur joueur;
    @Mock
    private Pokemon koPokemon;
    @Mock
    private Pokemon lanturnPokemon;
    // private List<Carte> energiesSurKOPokemon; // This would be part of the state's constructor or context

    private EtatChoixUtiliserEnergyGrounding etat;

    @BeforeEach
    void setUp() {
        // energiesSurKOPokemon = new ArrayList<>();
        // // Populate with mock energies if needed for state construction
        // etat = new EtatChoixUtiliserEnergyGrounding(joueur, koPokemon, lanturnPokemon, energiesSurKOPokemon);
    }

    /*
    @Test
    void testChoixOui_ShouldTransitionToSelectEnergyState() {
        // Assuming the state has a method like `choixOui()` or `action("oui")`
        // etat.choixOui(); // Or similar action trigger

        // Verify that joueur.setEtatCourant() is called with an instance of EtatChoixEnergiePourEnergyGrounding
        // verify(joueur).setEtatCourant(any(EtatChoixEnergiePourEnergyGrounding.class));
    }
    */

    /*
    @Test
    void testChoixNon_ShouldTransitionToTourNormal() {
        // Assuming the state has a method like `choixNon()` or `action("non")`
        // etat.choixNon(); // Or similar action trigger

        // Verify transition to an appropriate next state, e.g., TourNormal or a KO processing state
        // This depends on the game flow after declining the talent.
        // verify(joueur).setEtatCourant(any(TourNormal.class)); // Or any(EtatGestionKO.class) etc.
    }
    */
}
