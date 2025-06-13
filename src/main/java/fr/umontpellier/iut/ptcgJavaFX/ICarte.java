package fr.umontpellier.iut.ptcgJavaFX;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type;

public interface ICarte {

    String getId();
    String getNom();
    String getCode();
    Type getTypeEnergie();

    fr.umontpellier.iut.ptcgJavaFX.mecanique.Type getFaiblesse();
    fr.umontpellier.iut.ptcgJavaFX.mecanique.Type getResistance();
    int getCoutRetraite();
}
