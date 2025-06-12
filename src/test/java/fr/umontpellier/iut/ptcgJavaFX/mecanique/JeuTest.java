package fr.umontpellier.iut.ptcgJavaFX.mecanique;

// import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.FabriqueCartes; // Not used in this file
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.Salameche;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type; // Added import for Type enum
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList; // Already present, but ensure Attaque is handled if needed by CartePokemon
// import java.util.Collections; // Not strictly needed for List.of
import java.util.List;

// Stub pour une carte Pikachu si elle n'existe pas (juste pour le test)
// Si Pikachu.java existe et est une CartePokemon, utiliser cet import à la place.
// import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.Pikachu; // Actual Pikachu class

class PikachuTestPokemon extends CartePokemon {
    public PikachuTestPokemon() {
        // Adjusted to match the expected CartePokemon constructor:
        // String name, String id, int pv, Type type, Type faiblesse, Type resistance, int coutRetraite
        super("Pikachu Test", "pikachu-test-id", 60, Type.ELECTRIQUE, Type.COMBAT, Type.INCOLORE, 1);
        // Assuming Type.INCOLORE for no resistance, or Type.NONE if it exists.
        // Attaques would be added separately if needed for this test class.
    }

    // Added missing abstract method from Carte (or its superclass)
    @Override
    public int getRangComparaison() {
        return 1; // Stub implementation, adjust if specific logic is needed for tests
    }

    @Override
    public void jouer(Joueur joueur) {
        // Stub implementation for abstract method
    }

    @Override
    public boolean peutJouer(Joueur joueur) {
        // Stub implementation for abstract method
        return true; // Default for stub, allows tests to proceed
    }

    // getNom(), getPv() etc. are assumed to be concrete in CartePokemon or its superclasses.
    // If not, they would also need to be implemented here.
}


public class JeuTest {

    private Jeu jeu;
    private Joueur joueur1;
    private Joueur joueur2;
    private Pokemon pokemonJ1Actif;
    private Pokemon pokemonJ1Banc1;
    private Pokemon pokemonJ2Actif;

    private CartePokemon salamecheCarte;
    private CartePokemon pikachuTestCarte; // Renamed for clarity to match class name
    private CartePokemon autreCartePourJ2;   // Renamed for clarity

    @BeforeEach
    void setUp() {
        System.out.println("JeuTest.setUp: Start");
        salamecheCarte = new Salameche();
        System.out.println("JeuTest.setUp: Created salamecheCarte: " + salamecheCarte.getId());
        pikachuTestCarte = new PikachuTestPokemon();
        System.out.println("JeuTest.setUp: Created pikachuTestCarte: " + pikachuTestCarte.getId());
        autreCartePourJ2 = new Salameche();
        System.out.println("JeuTest.setUp: Created autreCartePourJ2: " + autreCartePourJ2.getId());

        // Ensure IDs are unique for testing purposes if not handled by constructors with given string IDs
        // If IDs are auto-generated, this step is not needed.
        // If string IDs are literal, ensure they are different if needed for specific tests,
        // but the current tests select based on object instance's card's ID.

        List<Carte> deckJ1 = new ArrayList<>(List.of(salamecheCarte, pikachuTestCarte));
        List<Carte> deckJ2 = new ArrayList<>(List.of(autreCartePourJ2));
        System.out.println("JeuTest.setUp: Decks created");

        joueur1 = new Joueur("Joueur 1", deckJ1);
        System.out.println("JeuTest.setUp: Joueur 1 created");
        joueur2 = new Joueur("Joueur 2", deckJ2);
        System.out.println("JeuTest.setUp: Joueur 2 created");

        jeu = new Jeu(joueur1, joueur2);
        System.out.println("JeuTest.setUp: Jeu instance created");

        joueur1.setJeu(jeu); // Link players to the game instance
        joueur2.setJeu(jeu);
        System.out.println("JeuTest.setUp: Players linked to game");

        // Manually set up Pokémon on field
        pokemonJ1Actif = new Pokemon(salamecheCarte);
        joueur1.setPokemonActif(pokemonJ1Actif);
        System.out.println("JeuTest.setUp: pokemonJ1Actif set");

        pokemonJ1Banc1 = new Pokemon(pikachuTestCarte);
        if (!joueur1.getIndicesDeBancVides().isEmpty()) {
            joueur1.setPokemonBanc(pokemonJ1Banc1, Integer.parseInt(joueur1.getIndicesDeBancVides().get(0)));
            System.out.println("JeuTest.setUp: pokemonJ1Banc1 set");
        } else {
            fail("Banc de Joueur 1 est plein, ne peut pas placer pokemonJ1Banc1 pour le test.");
        }

        pokemonJ2Actif = new Pokemon(autreCartePourJ2);
        joueur2.setPokemonActif(pokemonJ2Actif);
        System.out.println("JeuTest.setUp: pokemonJ2Actif set");
        System.out.println("JeuTest.setUp: End");
    }

    @Test
    void testSelectionPokemonActifJoueur1() {
        assertNotNull(pokemonJ1Actif.getCartePokemon().getId(), "L'ID de la carte du Pokémon actif J1 ne doit pas être null.");
        jeu.carteSurTerrainCliquee(pokemonJ1Actif.getCartePokemon().getId());
        assertEquals(pokemonJ1Actif.getCartePokemon(), jeu.getCarteSelectionnee(), "Le Pokémon actif de J1 devrait être sélectionné.");
        assertEquals(pokemonJ1Actif.getCartePokemon(), jeu.carteSelectionneeProperty().get(), "La propriété de sélection devrait refléter le Pokémon actif de J1.");
    }

    @Test
    void testDeselectionPokemonActifJoueur1() {
        String id = pokemonJ1Actif.getCartePokemon().getId();
        assertNotNull(id, "L'ID de la carte du Pokémon actif J1 ne doit pas être null pour le test de déselection.");
        // Sélection
        jeu.carteSurTerrainCliquee(id);
        assertEquals(pokemonJ1Actif.getCartePokemon(), jeu.getCarteSelectionnee(), "Le Pokémon actif de J1 devrait être sélectionné initialement.");
        // Déselection
        jeu.carteSurTerrainCliquee(id);
        assertNull(jeu.getCarteSelectionnee(), "Le Pokémon actif de J1 devrait être déselectionné.");
        assertNull(jeu.carteSelectionneeProperty().get(), "La propriété de sélection devrait être null après déselection.");
    }

    @Test
    void testChangementDeSelection() {
        String idJ1Actif = pokemonJ1Actif.getCartePokemon().getId();
        String idJ1Banc1 = pokemonJ1Banc1.getCartePokemon().getId();
        assertNotNull(idJ1Actif, "L'ID de J1 Actif ne doit pas être null.");
        assertNotNull(idJ1Banc1, "L'ID de J1 Banc1 ne doit pas être null.");
        assertNotEquals(idJ1Actif, idJ1Banc1, "Les IDs des Pokémon testés doivent être différents.");


        // Sélectionner J1 Actif
        jeu.carteSurTerrainCliquee(idJ1Actif);
        assertEquals(pokemonJ1Actif.getCartePokemon(), jeu.getCarteSelectionnee(), "J1 Actif devrait être sélectionné.");

        // Sélectionner J1 Banc1 (devrait changer la sélection)
        jeu.carteSurTerrainCliquee(idJ1Banc1);
        assertEquals(pokemonJ1Banc1.getCartePokemon(), jeu.getCarteSelectionnee(), "J1 Banc1 devrait maintenant être sélectionné.");
        assertEquals(pokemonJ1Banc1.getCartePokemon(), jeu.carteSelectionneeProperty().get());
    }

    @Test
    void testSelectionPokemonActifJoueur2() {
        assertNotNull(pokemonJ2Actif.getCartePokemon().getId(), "L'ID de la carte du Pokémon actif J2 ne doit pas être null.");
        jeu.carteSurTerrainCliquee(pokemonJ2Actif.getCartePokemon().getId());
        assertEquals(pokemonJ2Actif.getCartePokemon(), jeu.getCarteSelectionnee(), "Le Pokémon actif de J2 devrait être sélectionné.");
    }

    @Test
    void testClicIdInvalide() {
        String idJ1Actif = pokemonJ1Actif.getCartePokemon().getId();
        assertNotNull(idJ1Actif, "L'ID de J1 Actif ne doit pas être null pour ce test.");

        // Sélectionner un Pokémon d'abord
        jeu.carteSurTerrainCliquee(idJ1Actif);
        assertEquals(pokemonJ1Actif.getCartePokemon(), jeu.getCarteSelectionnee(), "J1 Actif devrait être sélectionné initialement.");

        // Cliquer sur un ID invalide
        jeu.carteSurTerrainCliquee("id-totalement-invalide-123");
        assertEquals(pokemonJ1Actif.getCartePokemon(), jeu.getCarteSelectionnee(), "La sélection ne devrait pas changer après un clic sur un ID invalide.");

        // Si rien n'est sélectionné et on clique sur un ID invalide
        jeu.carteSurTerrainCliquee(idJ1Actif); // Resélectionne (car était sélectionné) puis déselectionne
        jeu.carteSurTerrainCliquee(idJ1Actif);
        assertNull(jeu.getCarteSelectionnee(), "Aucun Pokémon ne devrait être sélectionné après double-clic.");

        jeu.carteSurTerrainCliquee("id-totalement-invalide-123");
        assertNull(jeu.getCarteSelectionnee(), "La sélection devrait rester null après un clic invalide sur une sélection vide.");
    }
}
