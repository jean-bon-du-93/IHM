package fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Pokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
// import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.TourNormal; // Or other appropriate next state

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

// import java.util.List;

@ExtendWith(MockitoExtension.class)
public class EtatChoixEnergiePourEnergyGroundingTest {

    @Mock
    private Joueur joueur;
    @Mock
    private Pokemon koPokemon;
    @Mock
    private Pokemon lanturnPokemon;
    @Mock
    private Carte mockEnergyCarte; // The energy card chosen by the player

    private EtatChoixEnergiePourEnergyGrounding etat;

    @BeforeEach
    void setUp() {
        // // Constructor for EtatChoixEnergiePourEnergyGrounding might take the KO'd Pokemon,
        // // the Lanturn, and potentially a list of selectable energies.
        // etat = new EtatChoixEnergiePourEnergyGrounding(joueur, koPokemon, lanturnPokemon);
    }

    /*
    @Test
    void testCarteEnergieChoisie_ShouldMoveEnergyAndTransition() {
        // String energyCardId = "basic_energy_fire_id_01";
        // when(mockEnergyCarte.getId()).thenReturn(energyCardId);

        // // Simulate the state having this card as a valid choice, or that koPokemon has it.
        // // This depends on how the state gets the chosen card object.
        // // For now, assume uneCarteEnergieAEteChoisie(String id) is a method on the state.

        // etat.uneCarteEnergieAEteChoisie(energyCardId); // Or a similar method

        // Verify energy is removed from koPokemon
        // verify(koPokemon).retirerCarte(energyCardId); // Or verify based on the Carte object if it's fetched by ID

        // Verify energy is added to lanturnPokemon
        // verify(lanturnPokemon).ajouterCarte(any(Carte.class)); // Or more specific if the Carte object is passed

        // Verify transition to the next appropriate state
        // verify(joueur).setEtatCourant(any(TourNormal.class)); // Or any(EtatGestionKO.class) etc.
    }
    */
}
