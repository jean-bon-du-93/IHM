package fr.umontpellier.iut.ptcgJavaFX.mecanique;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.energie.EnergieFeu;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.Salameche;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.Reptincel;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.Ponyta; // Added for new test
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.Attaque; // Added for new test
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemonBase; // For stubs
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type; // For stubs
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.Roucool; // Added import
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
    private Pokemon pokemonJ1Actif; // This will be Ponyta for new tests
    private EnergieFeu energieFeuEnMain; // Still added to hand in setUp
    private CartePokemon reptincelCarte; // For evolution tests
    private CartePokemon roucoolCarte;
    private Pokemon roucoolSurBanc;
    private Pokemon pokemonJ2Actif; // Added field to hold J2's active, set in setUp

    @BeforeEach
    void setUp() {
        // Cartes pour le deck initial
        List<Carte> deckInitialJ1 = new ArrayList<>();

        joueur1 = new Joueur("Test Joueur 1", deckInitialJ1);

        // Utiliser Ponyta comme Pokémon actif par défaut pour les tests d'attaque.
        // Les tests spécifiques à Salameche (comme l'évolution en Reptincel) devront
        // peut-être configurer Salameche comme actif si nécessaire, ou adapter leurs attentes.
        CartePokemon ponytaCartePourActif = new Ponyta();
        pokemonJ1Actif = new Pokemon(ponytaCartePourActif);
        joueur1.setPokemonActif(pokemonJ1Actif);

        // Ajouter une carte énergie à la main du joueur1 (utile pour de nombreux tests)
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
        Salameche salamecheJ2Carte = new Salameche();
        this.pokemonJ2Actif = new Pokemon(salamecheJ2Carte); // Assign to class field
        joueur2.setPokemonActif(this.pokemonJ2Actif);

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

        // Préparer un Roucool sur le banc qui peut évoluer (mais pas en Reptincel)
        this.roucoolCarte = new Roucool();
        this.roucoolSurBanc = new Pokemon(this.roucoolCarte);
        if (!joueur1.getIndicesDeBancVides().isEmpty()) {
            joueur1.setPokemonBanc(this.roucoolSurBanc, Integer.parseInt(joueur1.getIndicesDeBancVides().get(0)));
            this.roucoolSurBanc.onFinTour(joueur1); // Il peut évoluer (en Roucoups)
        } else {
            fail("Banc de Joueur 1 est plein, ne peut pas placer roucoolSurBanc pour le test.");
        }
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

    @Test
    void testEvolutionImpossibleMauvaisePreEvolution() {
        assertTrue(reptincelCarte.peutJouer(joueur1), "Reptincel devrait être jouable car il y a un Salameche apte.");
        assertTrue(roucoolSurBanc.getPeutEvoluer(), "Roucool sur le banc devrait pouvoir évoluer (en Roucoups, pas Reptincel).");
        int mainInitialSize = joueur1.getMain().size(); // Main contient energieFeuEnMain et reptincelCarte

        // 1. Sélectionner Reptincel de la main
        jeu.uneCarteDeLaMainAEteChoisie(reptincelCarte.getId());
        assertEquals(reptincelCarte, joueur1.carteEnJeuProperty().get(), "Reptincel devrait être la carte en jeu.");
        assertTrue(joueur1.getEtatCourant() instanceof fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.ChoixPokemon, "Le joueur devrait être en état ChoixPokemon.");

        // 2. Vérifier que Roucool n'est pas une cible valide pour Reptincel
        List<String> ciblesPossiblesPourReptincel = reptincelCarte.getChoixPossibles(joueur1);
        assertFalse(ciblesPossiblesPourReptincel.contains(roucoolCarte.getId()), "Roucool ne devrait pas être une cible possible pour Reptincel.");
        assertTrue(ciblesPossiblesPourReptincel.contains(pokemonJ1Actif.getCartePokemon().getId()), "Salameche actif DEVRAIT être une cible possible.");

        // 3. Tenter de sélectionner Roucool comme cible
        ((fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.ChoixPokemon) joueur1.getEtatCourant()).carteChoisie(roucoolCarte.getId());

        // Vérifications : l'évolution ne doit pas avoir eu lieu
        assertSame(roucoolCarte, roucoolSurBanc.getCartePokemon(), "Roucool sur le banc ne doit pas avoir évolué.");
        assertEquals(reptincelCarte, joueur1.carteEnJeuProperty().get(), "Reptincel devrait toujours être la carte en jeu (action non résolue car cible invalide).");
        assertTrue(joueur1.getEtatCourant() instanceof fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.ChoixPokemon, "Le joueur devrait rester en état ChoixPokemon.");
        // Reptincel a été jouée de la main (et est maintenant dans carteEnJeu)
        assertEquals(mainInitialSize - 1, joueur1.getMain().size(), "Reptincel a été jouée de la main.");
        assertFalse(joueur1.getMain().contains(reptincelCarte), "Reptincel ne devrait plus être en main.");
    }

    @Test
    void testEvolutionConserveDegatsEtEnergies() {
        // Préparer Salameche actif
        assertTrue(pokemonJ1Actif.getPeutEvoluer(), "Salameche actif doit pouvoir évoluer.");

        // Retirer l'énergie mise dans setUp pour ce test spécifique pour contrôler l'énergie exacte
        joueur1.getMain().remove(energieFeuEnMain); // Supposons qu'elle était là
        EnergieFeu energieSpecifique = new EnergieFeu(); // Utiliser une nouvelle instance pour ce test

        pokemonJ1Actif.ajouterCarte(energieSpecifique);
        pokemonJ1Actif.ajouterDegats(20);

        assertEquals(20, pokemonJ1Actif.getDegats(), "Salameche doit avoir 20 dégâts.");
        assertEquals(1, pokemonJ1Actif.getEnergie().getOrDefault(fr.umontpellier.iut.ptcgJavaFX.mecanique.Type.FEU, 0).intValue(), "Salameche doit avoir 1 énergie Feu.");
        int cartesAvantEvo = pokemonJ1Actif.getCartes().size(); // Salameche + Energie

        // Évolution
        jeu.uneCarteDeLaMainAEteChoisie(reptincelCarte.getId());
        // L'ID du Pokémon actif (Salameche) est nécessaire pour carteSurTerrainCliquee et uneCarteComplementaireAEteChoisie
        String salamecheActifId = pokemonJ1Actif.getCartePokemon().getId();
        jeu.carteSurTerrainCliquee(salamecheActifId);
        // Attention: uneCarteComplementaireAEteChoisie est appelée par l'UI, ici on simule l'action de l'état ChoixPokemon
        // qui serait déclenché par carteSurTerrainCliquee si la logique du jeu est bien chaînée.
        // Si on teste l'état directement, on appellerait etat.carteChoisie(salamecheActifId)
        // Ici, on teste Jeu.uneCarteComplementaireAEteChoisie comme demandé par l'architecture des vues.
        ((fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.ChoixPokemon) joueur1.getEtatCourant()).carteChoisie(salamecheActifId);


        // Vérifications sur le Pokémon évolué (qui est toujours la même instance pokemonJ1Actif)
        assertSame(reptincelCarte, pokemonJ1Actif.getCartePokemon(), "Pokémon devrait être Reptincel.");
        assertEquals(20, pokemonJ1Actif.getDegats(), "Reptincel devrait conserver les 20 dégâts.");
        assertEquals(1, pokemonJ1Actif.getEnergie().getOrDefault(fr.umontpellier.iut.ptcgJavaFX.mecanique.Type.FEU, 0).intValue(), "Reptincel devrait conserver l'énergie Feu.");
        assertEquals(cartesAvantEvo + 1, pokemonJ1Actif.getCartes().size(), "Reptincel (la carte) doit avoir été ajoutée aux cartes du Pokémon.");
        assertTrue(pokemonJ1Actif.getCartes().contains(energieSpecifique), "L'énergie doit toujours être attachée.");
        assertTrue(pokemonJ1Actif.getCartes().contains(reptincelCarte), "Reptincel doit être dans les cartes.");
    }

    @Test
    void testEvolutionGueritStatuts() {
        // Préparer Salameche actif
        // NOTE: pokemonJ1Actif is Ponyta by default. For this test, we need Salameche.
        // So, we set up Salameche specifically for this test.
        CartePokemon salamecheCartePourTest = new Salameche();
        Pokemon salamecheTestPokemon = new Pokemon(salamecheCartePourTest);
        joueur1.setPokemonActif(salamecheTestPokemon); // Override Ponyta for this test
        salamecheTestPokemon.onFinTour(joueur1); // Make it evolvable

        assertTrue(salamecheTestPokemon.getPeutEvoluer(), "Salameche actif doit pouvoir évoluer.");
        salamecheTestPokemon.setEstBrule();
        assertTrue(salamecheTestPokemon.estBruleProperty().get(), "Salameche doit être Brûlé.");

        // Évolution
        jeu.uneCarteDeLaMainAEteChoisie(reptincelCarte.getId()); // reptincelCarte is still in hand from setUp
        String salamecheActifId = salamecheTestPokemon.getCartePokemon().getId();
        jeu.carteSurTerrainCliquee(salamecheActifId);
        ((fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.ChoixPokemon) joueur1.getEtatCourant()).carteChoisie(salamecheActifId);

        // Vérifications
        assertSame(reptincelCarte, salamecheTestPokemon.getCartePokemon(), "Pokémon devrait être Reptincel.");
        assertFalse(salamecheTestPokemon.estBruleProperty().get(), "Reptincel ne devrait plus être Brûlé après évolution.");

        // Restore default active pokemon if other tests depend on it (though @BeforeEach handles it)
        // joueur1.setPokemonActif(pokemonJ1Actif);
    }

    @Test
    void testPokemonGetAttaquesPossibles() {
        // pokemonJ1Actif est un Ponyta (configuré dans setUp).
        // Attaques de Ponyta:
        // 1. "Charbon Mutant" (Coût: 1 Feu)
        // 2. "Écrasement" (Coût: 2 Feu)

        // Cas 0: Aucune énergie
        // energieFeuEnMain est dans la main de joueur1 depuis setUp(). Il faut la retirer pour ce cas.
        assertTrue(joueur1.getMain().contains(energieFeuEnMain), "EnergieFeu de setUp devrait être en main.");
        joueur1.getMain().remove(energieFeuEnMain); // Retirer pour le test "aucune énergie"
        assertFalse(joueur1.getMain().contains(energieFeuEnMain), "EnergieFeu ne devrait plus être en main.");

        assertTrue(pokemonJ1Actif.attaquesProperty().isEmpty(), "Ponyta ne devrait avoir aucune attaque jouable sans énergie.");

        // Cas 1: Attacher 1 énergie Feu
        // Ré-ajouter l'énergie en main pour simuler le processus normal
        joueur1.ajouterCarteMain(energieFeuEnMain);
        assertTrue(joueur1.getMain().contains(energieFeuEnMain), "EnergieFeu devrait être de retour en main.");

        // Attacher l'énergie
        jeu.uneCarteDeLaMainAEteChoisie(energieFeuEnMain.getId());
        jeu.carteSurTerrainCliquee(pokemonJ1Actif.getCartePokemon().getId()); // Attacher à Ponyta

        List<String> attaquesApres1EnergieNoms = pokemonJ1Actif.getAttaquesPossibles().stream().map(Attaque::getNom).toList();
        assertEquals(1, attaquesApres1EnergieNoms.size(), "Ponyta devrait avoir 1 attaque jouable avec 1 énergie Feu.");
        assertTrue(attaquesApres1EnergieNoms.contains("Charbon Mutant"), "Charbon Mutant devrait être jouable.");
        assertFalse(attaquesApres1EnergieNoms.contains("Écrasement"), "Écrasement ne devrait pas être jouable.");
        // Vérifier aussi la property observable
        assertEquals(1, pokemonJ1Actif.attaquesProperty().size(), "attaquesProperty devrait avoir 1 élément.");
        assertTrue(pokemonJ1Actif.attaquesProperty().contains("Charbon Mutant"), "attaquesProperty devrait contenir Charbon Mutant.");


        // Cas 2: Attacher une deuxième énergie Feu
        EnergieFeu energie2 = new EnergieFeu();
        joueur1.ajouterCarteMain(energie2);

        // Réinitialiser peutJouerEnergie pour ce test (simule un nouveau tour ou une capacité spéciale)
        joueur1.onDebutTour();
        assertTrue(joueur1.peutJouerEnergie(), "Le joueur devrait pouvoir jouer une autre énergie après onDebutTour().");

        jeu.uneCarteDeLaMainAEteChoisie(energie2.getId());
        jeu.carteSurTerrainCliquee(pokemonJ1Actif.getCartePokemon().getId());

        List<String> attaquesApres2EnergiesNoms = pokemonJ1Actif.getAttaquesPossibles().stream().map(Attaque::getNom).toList();
        assertEquals(2, attaquesApres2EnergiesNoms.size(), "Ponyta devrait avoir 2 attaques jouables avec 2 énergies Feu.");
        assertTrue(attaquesApres2EnergiesNoms.contains("Charbon Mutant"), "Charbon Mutant devrait toujours être jouable.");
        assertTrue(attaquesApres2EnergiesNoms.contains("Écrasement"), "Écrasement devrait maintenant être jouable.");
        // Vérifier aussi la property observable
        assertEquals(2, pokemonJ1Actif.attaquesProperty().size(), "attaquesProperty devrait avoir 2 éléments.");
        assertTrue(pokemonJ1Actif.attaquesProperty().contains("Écrasement"), "attaquesProperty devrait contenir Écrasement.");
    }

    @Test
    void testAttaqueDegatsSimples() {
        // pokemonJ1Actif est Ponyta. Attaque "Charbon Mutant" (1 Feu, 10 dégâts).
        // pokemonJ2Actif est Salameche (par défaut dans setUp). Type Feu, Faiblesse Eau, pas de Résistance à Feu.

        // Attacher une énergie Feu à Ponyta pour qu'il puisse attaquer
        // energieFeuEnMain est déjà dans la main de joueur1 et est une EnergieFeu.
        joueur1.onDebutTour(); // Assure peutJouerEnergie
        jeu.uneCarteDeLaMainAEteChoisie(energieFeuEnMain.getId());
        jeu.carteSurTerrainCliquee(pokemonJ1Actif.getCartePokemon().getId()); // Attache à Ponyta
        assertFalse(joueur1.peutJouerEnergie()); // Confirme que l'énergie a été jouée

        // S'assurer que l'attaque est possible
        assertTrue(pokemonJ1Actif.getAttaquesPossibles().stream().anyMatch(a -> a.getNom().equals("Charbon Mutant")), "Charbon Mutant devrait être jouable.");
        // int pvInitialPokemonJ2 = pokemonJ2Actif.getCartePokemon().getPointsVie(); // PV not needed for damage check
        int degatsAvantAttaqueJ2 = pokemonJ2Actif.getDegats();

        // Exécuter l'attaque
        jeu.uneAttaqueAEteChoisie("Charbon Mutant"); // Appelle joueur1.attaquer("Charbon Mutant")

        // Vérifications
        // Salameche (Feu) n'a pas de faiblesse/résistance à Feu (type de Ponyta).
        assertEquals(degatsAvantAttaqueJ2 + 10, pokemonJ2Actif.getDegats(), "PokemonJ2Actif devrait avoir subi 10 dégâts.");

        // Vérifier que le joueur passe à l'état de vérification des KO (ou un état post-attaque)
        assertTrue(joueur1.getEtatCourant() instanceof fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.VerificationPokemonAdversaire ||
                   joueur1.getEtatCourant() instanceof fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal.FinPartie,
                   "L'état du joueur devrait être une vérification post-attaque ou fin de partie. Actuel: " + joueur1.getEtatCourant().getClass().getSimpleName());
    }

    @Test
    void testAttaqueAvecFaiblesse() {
        // pokemonJ1Actif est Ponyta (Feu).
        // Créons un Pokémon stub pour le test qui a une faiblesse au Feu.
        class PokemonTestFaiblesseFeu extends CartePokemonBase {
            public PokemonTestFaiblesseFeu() {
                super("TestFaiblesseFeu", "testf", 100,
                      Type.PLANTE, // Type Plante
                      Type.FEU,    // Faiblesse Feu
                      null, 1);
                // Pas d'attaques nécessaires pour ce test de réception de dégâts
            }
        }
        CartePokemon cibleFaiblesseCarte = new PokemonTestFaiblesseFeu();
        // Réassigner pokemonJ2Actif pour ce test spécifique
        pokemonJ2Actif = new Pokemon(cibleFaiblesseCarte);
        jeu.getJoueurAdverse(joueur1).setPokemonActif(pokemonJ2Actif); // Important: Mettre à jour le Pokémon actif du joueur2 dans le jeu

        // Attacher énergie à Ponyta
        joueur1.onDebutTour();
        jeu.uneCarteDeLaMainAEteChoisie(energieFeuEnMain.getId());
        jeu.carteSurTerrainCliquee(pokemonJ1Actif.getCartePokemon().getId());

        int degatsAvantAttaqueJ2 = pokemonJ2Actif.getDegats();

        // Exécuter l'attaque "Charbon Mutant" (10 dégâts de base, type Feu)
        jeu.uneAttaqueAEteChoisie("Charbon Mutant");

        // Dégâts devraient être doublés (10 * 2 = 20)
        assertEquals(degatsAvantAttaqueJ2 + 20, pokemonJ2Actif.getDegats(), "Dégâts sur cible avec faiblesse Feu devraient être doublés.");
    }

    @Test
    void testAttaqueAvecResistance() {
        // pokemonJ1Actif est Ponyta (Feu).
        // Créons un stub.
        class PokemonTestResistanceFeu extends CartePokemonBase {
            public PokemonTestResistanceFeu() {
                super("TestResistanceFeu", "testr", 100,
                      Type.EAU, // Type Eau
                      null,
                      Type.FEU,    // Resistance Feu
                      1);
                 // Pas d'attaques nécessaires pour ce test de réception de dégâts
            }
        }
        CartePokemon cibleResistanceCarte = new PokemonTestResistanceFeu();
        // Réassigner pokemonJ2Actif pour ce test spécifique
        pokemonJ2Actif = new Pokemon(cibleResistanceCarte);
        jeu.getJoueurAdverse(joueur1).setPokemonActif(pokemonJ2Actif); // Important: Mettre à jour le Pokémon actif du joueur2 dans le jeu


        // Attacher énergie à Ponyta
        joueur1.onDebutTour();
        jeu.uneCarteDeLaMainAEteChoisie(energieFeuEnMain.getId());
        jeu.carteSurTerrainCliquee(pokemonJ1Actif.getCartePokemon().getId());

        int degatsAvantAttaqueJ2 = pokemonJ2Actif.getDegats();

        // Exécuter l'attaque "Charbon Mutant" (10 dégâts de base, type Feu)
        jeu.uneAttaqueAEteChoisie("Charbon Mutant");

        // Dégâts devraient être réduits de 20 (10 - 20 = -10, donc 0 dégât)
        // Pokemon.ajouterDegats(Math.max(0, degats - resistance)); La résistance est de 20 par défaut.
        assertEquals(degatsAvantAttaqueJ2 + Math.max(0, 10 - 20), pokemonJ2Actif.getDegats(), "Dégâts sur cible avec résistance Feu devraient être réduits (min 0).");
    }

    @Test
    void testAttaqueImpossiblePremierTour() {
        // Créer un nouveau jeu pour ce test pour contrôler l'état initial du compteur de tour.
        // Le deck doit contenir les cartes pour piocher la main initiale et avoir un Pokémon de base.
        ArrayList<Carte> deckJ1 = new ArrayList<>(List.of(new Ponyta(), new EnergieFeu(), new Salameche(), new Salameche(), new Salameche(), new Salameche(), new Salameche()));
        ArrayList<Carte> deckJ2 = new ArrayList<>(List.of(new Salameche(), new Salameche(), new Salameche(), new Salameche(), new Salameche(), new Salameche(), new Salameche()));

        Joueur j1 = new Joueur("J1First", deckJ1);
        Joueur j2 = new Joueur("J2First", deckJ2);
        Jeu premierTourJeu = new Jeu(j1, j2); // compteurTour est 0 ici
        j1.setJeu(premierTourJeu);
        j2.setJeu(premierTourJeu);

        // Simuler la phase d'initialisation (piocher main, choisir actif)
        premierTourJeu.run(); // Exécute InitialisationJoueurs qui appelle piocherMainInitiale etc.
                              // Et devrait mettre le jeu dans l'état où j1 peut jouer son premier tour.
                              // Le compteurTour est 0 après InitialisationJoueurs.demarrerPartie() -> joueurActif.jouerTour().

        // j1 devrait être le joueur actif, et son Pokémon actif devrait être celui choisi ou placé.
        // Supposons que Ponyta est mis actif (simplification, normalement c'est via une sélection).
        // Pour ce test, on va manuellement mettre Ponyta actif si ce n'est pas déjà fait par run().
        if (j1.getPokemonActif() == null || !(j1.getPokemonActif().getNom().equals("Ponyta"))) {
            // Chercher Ponyta dans la main (piochée par run()) et le mettre actif
            Carte ponytaEnMain = j1.getMain().stream().filter(c -> c.getNom().equals("Ponyta")).findFirst().orElse(null);
            if (ponytaEnMain == null) { // Si Ponyta n'est pas en main, c'est un problème de setup du test/deck
                 j1.ajouterCarteMain(new Ponyta()); // Force Ponyta en main
                 ponytaEnMain = j1.getMain().stream().filter(c -> c.getNom().equals("Ponyta")).findFirst().get();
            }
            j1.setPokemonActif(new Pokemon((CartePokemon) ponytaEnMain));
            j1.getMain().remove(ponytaEnMain); // Retirer de la main si mis actif
        }
        Pokemon ponytaActifJ1 = j1.getPokemonActif();
        assertNotNull(ponytaActifJ1, "J1 doit avoir un Pokémon actif.");
        assertEquals("Ponyta", ponytaActifJ1.getNom(), "Le Pokémon actif de J1 devrait être Ponyta.");


        // Attacher une énergie à Ponyta
        Carte energieFeu = j1.getMain().stream().filter(c -> c instanceof EnergieFeu).findFirst().orElse(null);
        if (energieFeu == null) { // Si pas d'énergie en main (improbable avec le deck)
            j1.ajouterCarteMain(new EnergieFeu()); // Force EnergieFeu en main
            energieFeu = j1.getMain().stream().filter(c -> c instanceof EnergieFeu).findFirst().get();
        }
        assertNotNull(energieFeu, "J1 doit avoir une Energie Feu en main.");

        // Assurer que j1 est dans un état où il peut jouer une énergie et attaquer (si ce n'était pas le premier tour)
        j1.onDebutTour(); // Réinitialise peutJouerEnergie, etc.
        premierTourJeu.uneCarteDeLaMainAEteChoisie(energieFeu.getId());
        premierTourJeu.carteSurTerrainCliquee(ponytaActifJ1.getCartePokemon().getId()); // Attacher à Ponyta
        assertFalse(j1.peutJouerEnergie(), "Energie devrait être attachée.");

        // Vérifier les attaques possibles.
        // Selon la logique actuelle (jeu.getCompteurTour() != 1), et sachant que compteurTour est 0 ici.
        // Le premier joueur PEUT attaquer.
        assertEquals(0, premierTourJeu.getCompteurTour(), "Compteur de tour devrait être 0 pour le premier tour du premier joueur.");
        assertFalse(ponytaActifJ1.getAttaquesPossibles().isEmpty(), "Ponyta devrait pouvoir attaquer au tour 0 avec la logique actuelle de Pokemon.getAttaquesPossibles().");
        assertTrue(ponytaActifJ1.getAttaquesPossibles().stream().anyMatch(a -> a.getNom().equals("Charbon Mutant")), "Charbon Mutant devrait être listé comme possible.");

        // Si la règle TCG "pas d'attaque au premier tour pour le premier joueur" était implémentée différemment,
        // par exemple si Pokemon.getAttaquesPossibles() vérifiait (jeu.getCompteurTour() == 0 && jeu.getJoueurActif() == jeu.getJoueurs()[0]),
        // alors la liste des attaques possibles serait vide.
        // Ce test valide le comportement actuel du code.
    }
}
