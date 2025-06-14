package fr.umontpellier.iut.ptcgJavaFX;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

public interface IJeu {

    ObjectProperty<String> instructionProperty();
    ObjectProperty<? extends IJoueur> joueurActifProperty();
    BooleanProperty finDePartieProperty();

    IJoueur[] getJoueurs();
    String getNomDuGagnant();

    void passerAEteChoisi();
    void uneCarteDeLaMainAEteChoisie(String idCarteChoisie);
    void unEmplacementVideDuBancAEteChoisi(String indiceBanc);
    void uneAttaqueAEteChoisie(String nomAttaque);
    void retraiteAEteChoisie();
    void melangerAEteChoisi();
    void ajouterAEteChoisi();
    void defausserEnergieAEteChoisi();
    void defausserEnergieNAPasEteChoisi();
    void uneCarteEnergieAEteChoisie(String idCarteEnergie);
    void uneCarteComplementaireAEteChoisie(String idCartecomplementaire);
    void carteSurTerrainCliquee(String idCarte);

    void talentAEteAccepte();
    void talentAEteRefuse();
}