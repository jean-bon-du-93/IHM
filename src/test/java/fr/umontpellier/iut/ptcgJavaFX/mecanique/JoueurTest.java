package fr.umontpellier.iut.ptcgJavaFX.mecanique;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.energie.EnergieFeu;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.Salameche;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.Reptincel; // Added import
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemon; // Added import for casting if needed, and for reptincelCarte field
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.TourNormal;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.ChoixPokemon; // Added import

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

// Note: PikachuTestPokemon stub is not needed here as it's not used by this specific test class.

public class JoueurTest {

    private Jeu jeu;
    private Joueur joueur1;
    private Pokemon pokemonJ1Actif; // This will be Salameche
    private EnergieFeu energieFeuEnMain;
    private CartePokemon reptincelCarte; // Added field
    // private Pokemon salamecheSurBanc; // Not used in these specific tests yet

    @BeforeEach
    void setUp() {
        // Cartes pour le deck initial
        List<Carte> deckInitialJ1 = new ArrayList<>();

        joueur1 = new Joueur("Test Joueur 1", deckInitialJ1);

        // Créer un Salameche et le mettre en jeu comme actif pour joueur1
        // pokemonJ1Actif was initialized with a new Salameche in previous version, keep it for consistency
        // If salamecheCarte field is needed, it should be declared at class level too. For now, local is fine.
        CartePokemon salamecheCartePourActif = new Salameche(); // Instance for active
        pokemonJ1Actif = new Pokemon(salamecheCartePourActif);
        joueur1.setPokemonActif(pokemonJ1Actif);

        // Ajouter une carte énergie à la main du joueur1
        energieFeuEnMain = new EnergieFeu();
        joueur1.ajouterCarteMain(energieFeuEnMain);

        // Créer une carte Reptincel et l'ajouter à la main de joueur1
        this.reptincelCarte = new Reptincel();
        joueur1.ajouterCarteMain(this.reptincelCarte);

        // Créer un joueur 2 simple
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

        joueur1.setEtatCourant(new TourNormal(joueur1));
        joueur1.onDebutTour(); // Assure que peutJouerEnergie est true

        // S'assurer que le Pokémon actif peut évoluer (a été "en jeu" au moins un tour)
        this.pokemonJ1Actif.onFinTour(joueur1); // Salameche actif peut maintenant évoluer
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

    @Test
    void testUneSeuleEnergieParTour() {
        // Attacher une première énergie (basé sur testAttacherEnergieReussi)
        assertTrue(joueur1.peutJouerEnergie(), "Devrait pouvoir jouer une énergie initialement.");
        EnergieFeu premiereEnergie = energieFeuEnMain; // energieFeuEnMain est déjà dans la main de joueur1 via setUp

        assertNotNull(premiereEnergie.getId(), "ID de la première énergie ne doit pas être null.");
        jeu.uneCarteDeLaMainAEteChoisie(premiereEnergie.getId());
        assertNotNull(pokemonJ1Actif.getCartePokemon().getId(), "ID du Pokémon actif ne doit pas être null.");
        jeu.carteSurTerrainCliquee(pokemonJ1Actif.getCartePokemon().getId());

        // Vérifications après le premier attachement
        assertFalse(joueur1.peutJouerEnergie(), "Ne devrait plus pouvoir jouer une énergie après le premier attachement.");
        // La main contient maintenant reptincelCarte, donc sa taille n'est pas 0.
        // assertEquals(0, joueur1.getMain().size(), "La main devrait être vide après avoir attaché la seule énergie.");
        assertTrue(pokemonJ1Actif.getCartes().contains(premiereEnergie), "La première énergie devrait être attachée.");

        // Tenter d'attacher une deuxième énergie
        EnergieFeu deuxiemeEnergie = new EnergieFeu(); // Nouvelle instance d'énergie
        assertNotNull(deuxiemeEnergie.getId(), "ID de la deuxième énergie ne doit pas être null.");
        joueur1.ajouterCarteMain(deuxiemeEnergie); // Mettre la deuxième énergie en main
        // La main contient reptincelCarte et deuxiemeEnergie
        assertEquals(2, joueur1.getMain().size(), "La main devrait maintenant contenir deux cartes.");

        // Essayer de sélectionner la deuxième énergie de la main
        jeu.uneCarteDeLaMainAEteChoisie(deuxiemeEnergie.getId());

        // Vérifications : l'état du joueur ne devrait pas être AttenteChoixPokemonPourEnergie,
        // ou s'il y passe brièvement, il devrait en sortir sans attacher.
        // L'implémentation actuelle de TourNormalSansRetraite.carteChoisie() met une instruction
        // et reste dans TourNormal si on ne peut pas jouer d'énergie.
        assertTrue(joueur1.getEtatCourant() instanceof TourNormal, "Le joueur devrait rester en état TourNormal (ou similaire).");
        assertTrue(jeu.instructionProperty().get().contains("Vous avez déjà joué une carte Énergie ce tour"), "L'instruction devrait indiquer l'impossibilité de jouer une énergie.");

        // La deuxième énergie devrait toujours être en main
        assertEquals(2, joueur1.getMain().size(), "La main devrait toujours contenir deux cartes.");
        assertTrue(joueur1.getMain().contains(deuxiemeEnergie), "La deuxième énergie devrait toujours être en main.");

        // Le Pokémon actif ne devrait avoir que la première énergie
        long nombreEnergiesAttachees = pokemonJ1Actif.getCartes().stream().filter(c -> c.getTypeEnergie() != null).count();
        assertEquals(1, nombreEnergiesAttachees, "Le Pokémon actif ne devrait avoir qu'une seule énergie attachée.");
        assertFalse(pokemonJ1Actif.getCartes().contains(deuxiemeEnergie), "La deuxième énergie ne devrait pas être attachée.");
    }

    @Test
    void testEvolutionReussiePokemonActif() {
        // Préconditions : reptincelCarte est en main, pokemonJ1Actif (Salameche) peut évoluer (fait dans setUp via onFinTour).
        // Salameche initial de pokemonJ1Actif pour vérifier qu'il est conservé
        CartePokemon carteSalamecheOriginal = pokemonJ1Actif.getCartePokemon();

        assertTrue(reptincelCarte.peutJouer(joueur1), "Reptincel devrait être jouable.");
        assertTrue(pokemonJ1Actif.getPeutEvoluer(), "Salameche actif devrait pouvoir évoluer.");
        int mainInitialSize = joueur1.getMain().size(); // Devrait être 2 (energieFeuEnMain jouée, reptincelCarte) -> Non, setUp met 1 energie et 1 reptincel. Energie est jouée dans testUneSeuleEnergieParTour.
                                                        // Si ce test est lancé seul, main a 2 cartes : energieFeuEnMain et reptincelCarte.
                                                        // Si testUneSeuleEnergieParTour est lancé avant, l'état de la main peut être différent.
                                                        // @BeforeEach remet la main à energieFeuEnMain + reptincelCarte. Donc taille 2.

        // 1. Sélectionner Reptincel de la main
        assertNotNull(reptincelCarte.getId());
        jeu.uneCarteDeLaMainAEteChoisie(reptincelCarte.getId());

        assertEquals(reptincelCarte, joueur1.carteEnJeuProperty().get(), "Reptincel devrait être la carte en jeu.");
        assertTrue(joueur1.getEtatCourant() instanceof fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.ChoixPokemon, "Le joueur devrait être en état ChoixPokemon.");

        // 2. Sélectionner Salameche actif comme cible
        String salamecheActifId = pokemonJ1Actif.getCartePokemon().getId(); // ID du Salameche
        assertNotNull(salamecheActifId);
        jeu.carteSurTerrainCliquee(salamecheActifId);
        jeu.uneCarteComplementaireAEteChoisie(salamecheActifId);

        // Vérifications après évolution
        assertEquals(mainInitialSize - 1, joueur1.getMain().size(), "La main devrait avoir une carte de moins.");
        assertFalse(joueur1.getMain().contains(reptincelCarte), "Reptincel ne devrait plus être en main.");

        assertSame(reptincelCarte, pokemonJ1Actif.getCartePokemon(), "Le Pokémon actif devrait maintenant être Reptincel.");
        assertTrue(pokemonJ1Actif.getCartes().contains(reptincelCarte), "Reptincel devrait être dans les cartes du Pokémon.");
        assertTrue(pokemonJ1Actif.getCartes().contains(carteSalamecheOriginal), "Salameche (carte initiale) devrait toujours être dans les cartes du Pokémon comme sous-évolution.");
        assertFalse(pokemonJ1Actif.getPeutEvoluer(), "Le Pokémon ne devrait plus pouvoir évoluer ce tour-ci.");
        assertNull(joueur1.carteEnJeuProperty().get(), "Aucune carte ne devrait être 'en jeu' après l'évolution.");
        assertTrue(joueur1.getEtatCourant() instanceof fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.TourNormal, "Le joueur devrait revenir à l'état TourNormal.");
    }

    @Test
    void testEvolutionImpossiblePokemonJoueCeTour() {
        // Créer un nouveau Salameche qui vient d'être "joué" (peutEvoluer sera false par défaut)
        CartePokemon nouveauSalamecheCarte = new Salameche();
        Pokemon nouveauSalamechePokemon = new Pokemon(nouveauSalamecheCarte);
        // Remplacer le Pokémon actif par ce nouveau Salameche non-évolutif
        joueur1.setPokemonActif(nouveauSalamechePokemon);
        // L'ancien pokemonJ1Actif n'est plus actif. Son état peutEvoluer n'importe plus pour ce test précis.

        assertFalse(nouveauSalamechePokemon.getPeutEvoluer(), "Nouveau Salameche ne devrait pas pouvoir évoluer ce tour.");

        // Mettre Reptincel en jeu (simule la sélection depuis la main)
        joueur1.setCarteEnJeu(reptincelCarte);
        // Normalement, l'état serait ChoixPokemon, mais on le force pour tester la logique de la carte
        // et la vérification de la cible par l'état ChoixPokemon.
        ChoixPokemon etatChoix = new ChoixPokemon(joueur1, "Évolution"); // "Test" ou un type d'action pertinent
        joueur1.setEtatCourant(etatChoix);

        // Vérifier si la carte Reptincel considère le nouveau Salameche comme une cible valide
        List<String> ciblesPossibles = reptincelCarte.getChoixPossibles(joueur1);
        assertFalse(ciblesPossibles.contains(nouveauSalamecheCarte.getId()), "Le nouveau Salameche (non évolutif) ne devrait pas être une cible d'évolution possible.");

        // Simuler une tentative de clic sur ce Pokémon non-évolutif
        // L'état ChoixPokemon.carteChoisie va appeler carteEnJeu.checkSiPeutJouerSur(pokemonCible)
        // Si ce check échoue, il devrait mettre une instruction et rester dans ChoixPokemon.
        etatChoix.carteChoisie(nouveauSalamecheCarte.getId()); // Simule le choix de la cible par son ID

        // Vérifier que l'évolution n'a pas eu lieu
        assertSame(nouveauSalamecheCarte, joueur1.getPokemonActif().getCartePokemon(), "Le Pokémon actif devrait toujours être le nouveau Salameche.");
        assertEquals(reptincelCarte, joueur1.carteEnJeuProperty().get(), "Reptincel devrait toujours être la carte en jeu (action non résolue car cible invalide).");
        assertTrue(joueur1.getEtatCourant() instanceof ChoixPokemon, "Le joueur devrait rester en état ChoixPokemon.");
        // On pourrait aussi vérifier l'instruction, e.g.
        // assertTrue(jeu.instructionProperty().get().contains("ne peut pas évoluer ce tour-ci"));
    }
}
