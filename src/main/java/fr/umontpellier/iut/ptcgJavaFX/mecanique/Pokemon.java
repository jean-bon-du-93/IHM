package fr.umontpellier.iut.ptcgJavaFX.mecanique;

import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.Attaque;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemonEvolution; // Added import
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Classe représentant un Pokémon en jeu.
 * Un pokémon est caractérisé par :
 * <ul>
 * <li>une Carte Pokémon qui représente l'espèce du pokémon
 * <li>la liste des cartes qui lui sont associées : cartes pokémon (incluant les
 * niveaux précédents s'il s'agit d'une évolution), cartes énergie et
 * éventuellement des outils
 * <li>les variables supplémentaires décrivant son état (dégâts, statuts, etc.)
 * </ul>
 */
public class Pokemon implements IPokemon {
    /**
     * La carte Pokémon qui représente l'espèce du pokémon
     */
    private ObjectProperty<CartePokemon> cartePokemon;
    /**
     * La liste des cartes qui sont attachées à ce pokémon (incluant la carte
     * Pokémon principale)
     */
    private final ObservableList<Carte> cartes;
    /**
     * Quantité de dégâts subis par le pokémon
     */
    private IntegerProperty degats;
    /**
     * Points de vie restants du pokémon
     */
    private IntegerProperty pointsDeVie;
    /**
     * Attribut qui indique si le pokémon peut évoluer, c'est-à-dire s'il a été joué
     * avant le tour actuel du joueur
     */
    private boolean peutEvoluer;
    /**
     * Indique si le pokémon est actuellement dans l'état "brûlé"
     */
    private BooleanProperty estBrule;
    /**
     * Indique si le pokémon bénéficie d'une protection contre les effets et dégâts
     * des attaques adverses
     */
    private BooleanProperty estProtegeEffetsAttaques;
    /**
     * La liste des noms des attaques qui deviennent possibles
     * quand les dégats changent
     */
    private ObservableList<String> attaques;
    /**
     * Le dictionnaire des énergies par type d'énergie
     */
    private ObservableMap<String, List<String>> energie;

    private BooleanProperty estEmpoisonne = new SimpleBooleanProperty(false);
    private BooleanProperty estEndormi = new SimpleBooleanProperty(false);
    private BooleanProperty estParalyse = new SimpleBooleanProperty(false);
    private BooleanProperty estConfus = new SimpleBooleanProperty(false);

    public Pokemon(CartePokemon cartePokemon) {
        this.cartePokemon = new SimpleObjectProperty<>(cartePokemon);
        cartes = FXCollections.observableArrayList();
        cartes.add(cartePokemon);
        peutEvoluer = false;
        estBrule = new SimpleBooleanProperty(false);
        estProtegeEffetsAttaques = new SimpleBooleanProperty(false);
        degats = new SimpleIntegerProperty(0);
        pointsDeVie = new SimpleIntegerProperty(0);
        pointsDeVie.bind(Bindings.createIntegerBinding(() -> this.cartePokemon.getValue().getPointsVie() - degats.getValue(), this.cartePokemon, degats));
        attaques = FXCollections.observableArrayList();
        energie = FXCollections.observableHashMap();
        miseAJourAttaquesEtEnergie(); // Sets up listener on cartes

        // Add listener for cartePokemon changes (evolution)
        this.cartePokemon.addListener((obs, oldCard, newCard) -> {
            updateObservableAttackList();
            // PointsDeVie is already bound to this.cartePokemon, so it updates automatically.
            // Energy map is updated by the listener on 'cartes' when evolution card is added.
        });
        // Initial population of attacks, relying on the cartes.add in constructor triggering the listener.
        // If that's not robust enough, an explicit call here might be needed,
        // but the current logic with the modified listener should handle it.
    }

    private void updateObservableAttackList() {
        if (this.cartePokemon == null || this.cartePokemon.getValue() == null) { // Guard against null cartePokemon
            this.attaques.clear();
            return;
        }
        List<String> nomsAttaquesPossibles = getAttaquesPossibles().stream()
                .map(fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.Attaque::getNom)
                .collect(java.util.stream.Collectors.toList());
        this.attaques.setAll(nomsAttaquesPossibles);
    }

    public CartePokemon getCartePokemon() {
        return cartePokemon.getValue();
    }

    public int getCoutRetraite() {
        return getCartePokemon().getCoutRetraite(this);
    }

    public List<Carte> getCartes() {
        return new ArrayList<>(cartes);
    }

    public boolean getPeutEvoluer() {
        return peutEvoluer;
    }

    public void setEstBrule() {
        estBrule.setValue(true);
    }

    public boolean getEstProtegeEffetsAttaques() {
        return estProtegeEffetsAttaques.getValue();
    }

    public void setEstProtegeEffetsAttaques() {
        estProtegeEffetsAttaques.setValue(true);
    }

    public void ajouterCarte(Carte carte) {
        cartes.add(carte);
    }

    public boolean retirerCarte(Carte carte) {
        return cartes.remove(carte);
    }

    /**
     * Ajoute des dégâts au pokémon.
     * 
     * @param degats les dégâts à ajouter
     */
    public void ajouterDegats(int degats) {
        this.degats.setValue(this.degats.getValue() + degats);
    }

    /**
     * Ajoute des dégâts au pokémon suite à une attaque d'un pokémon adverse.
     * <p>
     * Cette méthode tient compte des faiblesses et résistances du pokémon, et n'est
     * appelée que si le pokémon est le pokémon actif du joueur.
     * 
     * @param degats les dégâts infligés par l'attaque
     * @param type   le type du pokémon attaquant
     */
    public void ajouterDegats(int degats, Type type) {
        if (getCartePokemon().getFaiblesse() == type) {
            ajouterDegats(2 * degats);
        } else if (getCartePokemon().getResistance() == type) {
            ajouterDegats(Math.max(0, degats - 20));
        } else {
            this.degats.setValue(this.degats.getValue() + degats);
        }
    }

    /**
     * Retire les effets de statut du pokémon qui ne sont applicables qu'au pokémon
     * actif.
     */
    public void retirerEffets() {
        estBrule.setValue(false);
        estProtegeEffetsAttaques.setValue(false);
        estEmpoisonne.setValue(false);
        estEndormi.setValue(false);
        estParalyse.setValue(false);
        estConfus.setValue(false);
    }

    /**
     * Cette méthode est appelée lorsqu'un pokémon du joueur est mis KO.
     * 
     * @param pokemon le pokémon qui est mis KO
     * @param joueur  le joueur possédant le pokémon
     */
    public void onPokemonKO(Pokemon pokemon, Joueur joueur) {
        getCartePokemon().onPokemonKO(pokemon, joueur);
    }

    /**
     * @return un dictionnaire qui associe à chaque type d'énergie le nombre de
     *         cartes énergie correspondantes attachées au pokémon
     */
    public Map<Type, Integer> getEnergie() {
        Map<Type, Integer> energie = new HashMap<>();
        for (Type type : Type.values()) {
            energie.put(type, 0);
        }

        for (Carte carte : cartes) {
            Type t = carte.getTypeEnergie();
            if (t != null) {
                energie.put(t, energie.get(t) + 1);
            }
        }
        return energie;
    }

    /**
     * Teste si un pokémon a assez d'énergie attachée pour payer un coût donné.
     * <p>
     * Le coût est passé sous la forme d'un dictionnaire. Les types d'énergie autres
     * que {@code INCOLORE} sont comparées directement au nombre de cartes de ce type
     * attachées au pokémon. Le coût en énergie {@code INCOLORE} est testé en
     * comptant le nombre total de cartes énergie attachées.
     * 
     * @param coutEnergie le coût à payer, sous la forme d'un dictionnaire associant
     *                    à un type d'énergie le nombre d'énergies de ce type à payer
     * @return {@code true} si le pokémon peut payer le coût, {@code false} sinon
     */
    public boolean peutPayerCout(Map<Type, Integer> coutEnergie) {
        Map<Type, Integer> energieTotale = getEnergie();
        for (Type type : coutEnergie.keySet()) {
            if (type != Type.INCOLORE && energieTotale.get(type) < coutEnergie.get(type)) {
                return false;
            }
        }

        int sommeCoutEnergie = coutEnergie.values().stream().mapToInt(Integer::intValue).sum();
        int sommeEnergieTotale = energieTotale.values().stream().mapToInt(Integer::intValue).sum();
        return sommeEnergieTotale >= sommeCoutEnergie;
    }

    /**
     * @return {@code true} si le pokémon a assez d'énergie attachée pour battre en
     *         retraite
     */
    public boolean peutRetraite() {
        if (estEndormi.get() || estParalyse.get()) {
            return false;
        }
        Map<Type, Integer> cout = new HashMap<>();
        // getCartePokemon() might be null if the Pokemon object is not fully initialized
        // or in a weird state, though unlikely for an active Pokemon.
        // Adding a null check for robustness.
        CartePokemon cp = getCartePokemon();
        if (cp == null) {
            return false; // Cannot determine retreat cost
        }
        cout.put(Type.INCOLORE, cp.getCoutRetraite(this));
        return peutPayerCout(cout);
    }

    /**
     * @return la liste des attaques que le pokémon peut utiliser avec les cartes
     *         énergie qui lui sont attachées
     */
    public List<Attaque> getAttaquesPossibles() {
        return getCartePokemon().getAttaques().stream().filter(attaque -> peutPayerCout(attaque.getCoutEnergie()))
                .toList();
    }

    /**
     * @return {@code true} si le pokémon est KO, c'est-à-dire s'il a subi des
     *         dégâts au moins égaux à ses points de vie.
     */
    public boolean estKO() {
        return degats.getValue() >= getCartePokemon().getPointsVie();
    }

    /**
     * Exécute l'attaque passée en paramètre.
     * <p>
     * Prérequis : le pokémon doit avoir assez d'énergie attachée pour payer le coût
     * de l'attaque.
     * 
     * @param nomAttaque nom de l'attaque à exécuter
     * @param joueur     le joueur qui exécute l'attaque
     */
    public void utiliserAttaque(String nomAttaque, Joueur joueur) {
        Attaque attaque = getCartePokemon().getAttaques().stream().filter(a -> a.getNom().equals(nomAttaque)).findFirst()
                .orElseThrow();
        attaque.attaquer(joueur);
    }

    // Note: The method signature was changed from CartePokemon to CartePokemonEvolution
    // The logic inside remains largely the same as what was provided in the prompt,
    // as the original simple evoluer method already did most of it.
    public void evoluer(CartePokemonEvolution carteEvolution) {
        // La carte d'évolution est ajoutée à la liste des cartes attachées au Pokémon.
        // La carte principale du Pokémon (celle qui définit son nom, ses PV, ses attaques)
        // devient la carte d'évolution.
        this.cartes.add(carteEvolution); // Ajoute la nouvelle évolution à la pile de cartes du Pokémon
        this.cartePokemon.set(carteEvolution); // Change la "face" du Pokémon par la carte d'évolution

        // Les dégâts restent. Les énergies restent (car elles sont dans this.cartes).

        // L'évolution guérit les conditions spéciales
        retirerEffets();
        // Si d'autres conditions spéciales existent (Confus, Empoisonné, Paralysé, Endormi),
        // il faudrait les réinitialiser ici aussi.
        // Par exemple : si des champs comme this.estConfus (BooleanProperty) existent, les mettre à false.

        this.peutEvoluer = false; // Ne peut plus évoluer ce tour-ci.
                                   // Sera remis à true à la fin du prochain tour du joueur via onFinTour().

        // La mise à jour de this.cartePokemon.set() et this.cartes.add() devrait déclencher
        // les listeners nécessaires pour mettre à jour l'UI (nom, PV, attaques, énergies).
        // La méthode miseAJourAttaquesEtEnergie est déjà attachée comme listener à this.cartes.
    }

    /**
     * Cette méthode est appelée au début du tour du joueur.
     * 
     * @param joueur joueur qui possède le pokémon
     */
    public void onDebutTour(Joueur joueur) {
        estProtegeEffetsAttaques.setValue(false);
    }

    /**
     * Cette méthode est appelée à la fin du tour du joueur.
     * 
     * @param joueur joueur qui possède le pokémon
     */
    public void onFinTour(Joueur joueur) {
        peutEvoluer = true;
        if (estParalyse.getValue()) {
            estParalyse.setValue(false);
            // Optionally, add a UI instruction/log that the Pokemon is no longer Paralyzed
            // if (joueur.getJeu() != null && getCartePokemon() != null) {
            //     joueur.getJeu().instructionProperty().setValue(getCartePokemon().getNom() + " n'est plus Paralysé.");
            // }
        }
        // It's important that getCartePokemon().onFinTour(joueur) is called AFTER
        // status conditions like Paralyzed are cleared, in case a card's own end-of-turn
        // effect depends on its status or wants to re-apply a status.
        // However, most built-in status conditions clear before card-specific end-of-turn text.
        // If getCartePokemon() could be null (e.g. if a Pokemon object exists without a card), add a null check.
        CartePokemon cp = getCartePokemon();
        if (cp != null) {
            cp.onFinTour(joueur); // For card-specific end-of-turn effects
        }
    }

    /**
     * @return {@code true} si le pokémon peut utiliser le talent de sa carte
     *         pokémon
     */
    public boolean peutUtiliserTalent() {
        return getCartePokemon().peutUtiliserTalent();
    }

    /**
     * Exécute l'action du talent de la carte pokémon.
     * 
     * @param joueur joueur qui possède le pokémon
     */
    public void utiliserTalent(Joueur joueur) {
        getCartePokemon().utiliserTalent(joueur);
    }

    /**
     * Exécute la phase de contrôle sur un pokémon
     */
    public void controlePokemon(Joueur joueur) {
        if (estBrule.getValue()) {
            ajouterDegats(20);
            if (joueur.lancerPiece()) {
                estBrule.setValue(false);
            }
        }
        if (estEmpoisonne.getValue()) {
            // TODO: Maybe add a log or UI instruction that poison damage is being applied.
            // joueur.getJeu().instructionProperty().setValue(getCartePokemon().getNom() + " subit 10 dégâts de Poison !");
            ajouterDegats(10); // 1 damage counter for poison
        }
        if (estEndormi.getValue()) {
            // It's important that the Joueur passed to controlePokemon is the owner of this Pokemon
            // so that lancerPiece() attributes the action/randomness to the correct player context if needed.
            // Also, Joueur needs access to Jeu to set instructionProperty. Let's assume Joueur has getJeu().
            if (joueur.getJeu() != null) { // Check if Jeu instance is available
                joueur.getJeu().instructionProperty().setValue(getCartePokemon().getNom() + " est Endormi. " + joueur.getNom() + " lance une pièce...");
            }
            if (joueur.lancerPiece()) { // True for heads
                estEndormi.setValue(false);
                if (joueur.getJeu() != null) {
                    joueur.getJeu().instructionProperty().setValue(getCartePokemon().getNom() + " s'est réveillé !");
                }
            } else {
                if (joueur.getJeu() != null) {
                    joueur.getJeu().instructionProperty().setValue(getCartePokemon().getNom() + " reste Endormi.");
                }
            }
        }
    }

    /**
     * Mise à jour des propriétés attaques et énergie du pokemon
     * lorsqu'on lui ajoute de nouvelles cartes
     */
    private void miseAJourAttaquesEtEnergie() {
        // Le champ 'attaques' est déjà initialisé dans le constructeur comme FXCollections.observableArrayList()
        // Le champ 'energie' est déjà initialisé dans le constructeur comme FXCollections.observableHashMap()

        cartes.addListener((ListChangeListener.Change<? extends Carte> change) -> {
            updateObservableAttackList(); // Call the new method

            // Keep the energy map update logic here
            Map<String, List<String>> cartesEnergie = cartes.stream()
                    .filter(c -> c.getTypeEnergie() != null)
                    .collect(Collectors.groupingBy(
                            c -> c.getTypeEnergie().asLetter(),
                            Collectors.mapping(Carte::getId, Collectors.toList())
                    ));
            // Remplacer : energie = FXCollections.observableMap(cartesEnergie);
            // Par :
            this.energie.clear();
            this.energie.putAll(cartesEnergie);
        });
    }

    /**
     * Propriétés exportées à utiliser dans l'IHM
     */
    @Override
    public ObjectProperty<CartePokemon> cartePokemonProperty() {
        return cartePokemon;
    }

    @Override
    public ObservableList<Carte> cartesProperty() {
        return cartes;
    }

    @Override
    public IntegerProperty pointsDeVieProperty() {
        return pointsDeVie;
    }

    @Override
    public ObservableList<String> attaquesProperty() {
        return attaques;
    }

    @Override
    public ObservableMap<String, List<String>> energieProperty() {
        return energie;
    }

    @Override
    public BooleanProperty estBruleProperty() {
        return estBrule;
    }

    @Override
    public BooleanProperty estProtegeEffetsAttaquesProperty() {
        return estProtegeEffetsAttaques;
    }

    public BooleanProperty estEmpoisonneProperty() {
        return estEmpoisonne;
    }

    public BooleanProperty estEndormiProperty() {
        return estEndormi;
    }

    public BooleanProperty estParalyseProperty() {
        return estParalyse;
    }

    public BooleanProperty estConfusProperty() {
        return estConfus;
    }
}
