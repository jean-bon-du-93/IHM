package fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon;

import java.util.List;
import java.util.stream.Collectors;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Pokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.attaque.FrapEclair;

public class Lanturn extends CartePokemonEvolution {
    public Lanturn() {
        super(
                "Lanturn",
                "CES050",
                110,
                Type.ELECTRIQUE,
                Type.COMBAT,
                Type.METAL,
                2,
                "Loupio",
                1);

        ajouterAttaque(new Attaque("Frap'Éclair", this, Type.ELECTRIQUE, 2, Type.INCOLORE, 1) {
            @Override
            public void attaquer(Joueur joueur) {
                joueur.setPeutDefausserEnergie(true);
                joueur.setEtatCourant(new FrapEclair(joueur));
                joueur.setPeutDefausserEnergie(true);
            }
        });
    }

    @Override
    public void onPokemonKO(Pokemon koPokemon, Joueur ownerOfLanturn) {
        // Condition: Was KO by opponent's attack? Approximation: is it opponent's turn?
        // This is a simplification. A more robust solution needs better game state info.
        boolean koByOpponentAttack = (ownerOfLanturn.getJeu().getJoueurActif() == ownerOfLanturn.getAdversaire());
        if (!koByOpponentAttack) {
            return;
        }

        // TODO: Add "once per turn" check for this specific Lanturn/power if needed.
        // if (ownerOfLanturn.aDejaUtiliseTalentCeTour("EnergyGrounding_" + this.getId())) return;

        Pokemon thisLanturnInPlay = ownerOfLanturn.getPokemon(this);
        if (thisLanturnInPlay == null || thisLanturnInPlay.estKO()) {
            return;
        }

        List<fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte> basicEnergiesOnKOPokemon = koPokemon.getCartes().stream()
            .filter(fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte::isBasicEnergy) // Relies on isBasicEnergy() method in Carte.java
            .collect(java.util.stream.Collectors.toList());

        if (basicEnergiesOnKOPokemon.isEmpty()) {
            return;
        }

        ownerOfLanturn.setEtatCourant(
            new fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.talent.EtatChoixUtiliserEnergyGrounding(ownerOfLanturn, koPokemon, thisLanturnInPlay, basicEnergiesOnKOPokemon)
        );
    }
}
