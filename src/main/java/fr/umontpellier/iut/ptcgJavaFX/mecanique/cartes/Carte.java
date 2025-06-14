package fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Carte implements Comparable<Carte>, ICarte {
    /**
     * Permet de donner un identifiant unique à chaque carte
     * (utile pour l'interface utilisateur)
     */
    private static int compteur = 0;
    /**
     * Dictionnaire de toutes les cartes du jeu,
     * indexées par leur identifiant
     */
    private static final Map<String, Carte> listeCartes = new HashMap<>();
    private final String id;
    private final String nom;
    private final String code;

    public Carte(String nom, String code) {
        this.id = "%d".formatted(compteur);
        compteur++;
        listeCartes.put(this.id, this);
        this.nom = nom;
        this.code = code;
    }

    public String getNom() {
        return nom;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getCode() {
        return code;
    }

    public static Carte get(String id) {
        return listeCartes.get(id);
    }

    /**
     * @return le type de l'énergie fournie pour les cartes énergie
     *         et {@code null} pour les autres cartes
     */
    public Type getTypeEnergie() {
        return null;
    }

    /**
     * @return le type du pokémon pour les cartes pokemon
     *         et {@code null} pour les autres cartes
     */
    public Type getTypePokemon() {
        return null;
    }

    public boolean estPokemonDeBase() {
        return false;
    }

    public boolean estPokemonEvolutif() {
        return false;
    }

    /**
     * @return {@code true} si le joueur peut jouer cette carte depuis sa main,
     *         {@code false} sinon
     */
    public abstract boolean peutJouer(Joueur joueur);

    public boolean peutJouerInit(Joueur joueur) {
        return false;
    }

    public abstract void jouer(Joueur joueur);

    public void jouerQuandEnJeu(Joueur joueur, String num) {}

    public abstract int getRangComparaison();

    public List<String> getChoixPossibles(Joueur joueur) {
        return null;
    }

    public int compareTo(Carte o) {
        if (this.getRangComparaison() != o.getRangComparaison()) {
            return this.getRangComparaison() - o.getRangComparaison();
        }
        if (this.nom.equals(o.nom)) {
            return this.id.compareTo(o.id);
        }
        return this.nom.compareTo(o.nom);
    }

    // Add these methods to Carte.java
    @Override
    public fr.umontpellier.iut.ptcgJavaFX.mecanique.Type getFaiblesse() {
        return null; // Default for non-Pokemon cards
    }

    @Override
    public fr.umontpellier.iut.ptcgJavaFX.mecanique.Type getResistance() {
        return null; // Default for non-Pokemon cards
    }

    @Override
    public int getCoutRetraite() {
        return 0; // Default for non-Pokemon cards
    }

    public boolean isBasicEnergy() {
        // Basic implementation: checks if it's a known basic energy type by name.
        // This needs to be robust based on how energy cards are named in the game.
        // Example:
        if (this.getTypeEnergie() == null) return false; // Not an energy

        // Assuming basic energies are simply named "Énergie <Type>" e.g. "Énergie Feu"
        // and special energies have more complex names or a different classification.
        // This is a placeholder, actual project may have better way to identify basic energy.
        switch (this.getNom()) {
            case "Énergie Feu":
            case "Énergie Eau":
            case "Énergie Plante":
            case "Énergie Électrique":
            case "Énergie Psy":
            case "Énergie Combat":
            case "Énergie Obscurité":
            case "Énergie Métal":
            case "Énergie Fée":
                return true;
            default:
                // Could also check if name *starts* with "Énergie " and has no other special words
                return false;
        }
    }
}
