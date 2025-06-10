package fr.umontpellier.iut.ptcgJavaFX.mecanique;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.energie.EnergieFeu; // Exemple d'énergie concrète
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.Salameche; // Exemple de Pokémon concret
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.TourNormal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

// Note: PikachuTestPokemon stub is not needed here as it's not used by this specific test class.

public class JoueurTest {

    private Jeu jeu;
    private Joueur joueur1;
    private Pokemon pokemonJ1Actif;
    private EnergieFeu energieFeuEnMain;

    @BeforeEach
    void setUp() {
        // Cartes pour le deck initial (peut être vide si on ajoute les cartes en main manuellement)
        List<Carte> deckInitialJ1 = new ArrayList<>();

        joueur1 = new Joueur("Test Joueur 1", deckInitialJ1);

        // Créer un Pokémon et le mettre en jeu comme actif pour joueur1
        Salameche salamecheCarte = new Salameche();
        pokemonJ1Actif = new Pokemon(salamecheCarte);
        joueur1.setPokemonActif(pokemonJ1Actif);

        // Ajouter une carte énergie à la main du joueur1
        energieFeuEnMain = new EnergieFeu();
        joueur1.ajouterCarteMain(energieFeuEnMain);

        // Créer un joueur 2 simple (peut ne pas être nécessaire pour tous les tests de Joueur)
        // Pour que Jeu soit valide, il a besoin de deux joueurs.
        List<Carte> deckInitialJ2 = new ArrayList<>();
        Joueur joueur2 = new Joueur("Test Joueur 2", deckInitialJ2);
        // Mettre un Pokémon actif pour J2 pour éviter des NPE si la logique du jeu le requiert
        Salameche salamecheJ2 = new Salameche();
        Pokemon pokemonJ2Actif = new Pokemon(salamecheJ2);
        joueur2.setPokemonActif(pokemonJ2Actif);

        // Initialiser le jeu et lier les joueurs
        jeu = new Jeu(joueur1, joueur2);
        joueur1.setJeu(jeu); // Assurer que le joueur a une référence au jeu
        joueur2.setJeu(jeu);

        // Mettre joueur1 comme joueur actif et dans un état de base
        // Note: Le constructeur de Jeu met joueurs[0] (joueur1 ici) comme joueurActif initialement.
        // Si ce n'est pas le cas, ou pour être explicite :
        // jeu.joueurActifProperty().set(joueur1);

        joueur1.setEtatCourant(new TourNormal(joueur1)); // Mettre le joueur dans un état où il peut jouer
        joueur1.onDebutTour(); // Pour s'assurer que peutJouerEnergie est true
    }

    @Test
    void testAttacherEnergieReussi() {
        assertTrue(joueur1.peutJouerEnergie(), "Le joueur devrait pouvoir jouer une énergie au début.");
        assertEquals(1, joueur1.getMain().size(), "La main du joueur devrait contenir 1 carte énergie.");
        assertTrue(joueur1.getMain().contains(energieFeuEnMain), "La carte énergie devrait être en main.");
        // Pokemon.getEnergie() returns Map<Type, Integer>. Summing values gives total energy cards.
        assertEquals(0, pokemonJ1Actif.getEnergie().values().stream().mapToInt(Integer::intValue).sum(), "Le Pokémon actif ne devrait avoir aucune énergie attachée initialement.");

        // Simuler la sélection de la carte énergie de la main
        // Cela devrait faire passer joueur1 à l'état AttenteChoixPokemonPourEnergie
        assertNotNull(energieFeuEnMain.getId(), "ID de la carte énergie ne doit pas être null.");
        jeu.uneCarteDeLaMainAEteChoisie(energieFeuEnMain.getId());

        // L'instruction du jeu devrait changer (vérification optionnelle mais utile)
        // assertTrue(jeu.instructionProperty().get().contains("Choisissez un de vos Pokémon"));

        // Simuler la sélection du Pokémon actif comme cible
        // Cela devrait déclencher l'attachement dans AttenteChoixPokemonPourEnergie
        assertNotNull(pokemonJ1Actif.getCartePokemon().getId(), "ID de la carte Pokémon active ne doit pas être null.");
        jeu.carteSurTerrainCliquee(pokemonJ1Actif.getCartePokemon().getId());

        // Vérifications après attachement
        assertFalse(joueur1.peutJouerEnergie(), "Le joueur ne devrait plus pouvoir jouer une énergie ce tour.");
        assertEquals(0, joueur1.getMain().size(), "La main du joueur devrait être vide.");
        assertFalse(joueur1.getMain().contains(energieFeuEnMain), "La carte énergie ne devrait plus être en main.");

        assertEquals(1, pokemonJ1Actif.getEnergie().values().stream().mapToInt(Integer::intValue).sum(), "Le Pokémon actif devrait avoir 1 énergie attachée.");
        // On pourrait aussi vérifier le type d'énergie si getEnergie() le permettait plus finement
        // ou en accédant directement à la liste des cartes attachées.
        assertTrue(pokemonJ1Actif.getCartes().contains(energieFeuEnMain), "La carte énergie devrait être dans les cartes attachées au Pokémon.");

        assertTrue(joueur1.getEtatCourant() instanceof TourNormal, "Le joueur devrait revenir à l'état TourNormal après l'attachement.");
    }

    // D'autres tests suivront ici (une seule énergie par tour, cible invalide, annulation, etc.)
}
