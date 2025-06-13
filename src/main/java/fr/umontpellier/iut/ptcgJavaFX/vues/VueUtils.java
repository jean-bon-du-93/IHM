package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type; // Assurez-vous que Type est importé
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream; // Pour charger les ressources

public class VueUtils {

    private static final String CHEMIN_IMAGES_CARTES = "/images/cartes/";
    private static final String CHEMIN_IMAGES_ICONES_ENERGIE = "/images/energie/";
    private static final String CHEMIN_DOS_CARTE = "/images/cartes/BACK.png";

    // Méthode auxiliaire pour configurer la taille d'ImageView
    private static void configurerTailleVueImage(ImageView vueImageCible, double largeurDesiree, double hauteurDesiree) {
        vueImageCible.setFitWidth(largeurDesiree);
        vueImageCible.setFitHeight(hauteurDesiree);
        vueImageCible.setPreserveRatio(true);
    }

    // Méthode auxiliaire pour charger une image dans une ImageView
    private static boolean essayerChargerImage(ImageView vueImageCible, String cheminFichierImage, String messageSiNonTrouvee) {
        try {
            InputStream fluxImage = VueUtils.class.getResourceAsStream(cheminFichierImage);
            if (fluxImage != null) {
                Image image = new Image(fluxImage);
                vueImageCible.setImage(image);
                return true;
            } else {
                System.err.println(messageSiNonTrouvee + " : " + cheminFichierImage);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image " + cheminFichierImage + " : " + e.getMessage());
            return false;
        }
    }

    // Méthode auxiliaire spécifique pour l'image principale de la carte
    private static boolean essayerChargerImageCartePrincipale(ImageView vueImageCible, String cheminFichierImageCarte) {
        return essayerChargerImage(vueImageCible, cheminFichierImageCarte, "Image de carte spécifique non trouvée");
    }

    public static ImageView creerVueImagePourCarte(ICarte laCarte, double largeurDesiree, double hauteurDesiree) {
        ImageView vueImage = new ImageView();
        configurerTailleVueImage(vueImage, largeurDesiree, hauteurDesiree);

        boolean imagePrincipaleChargee = false;
        if (laCarte != null && laCarte.getCode() != null && !laCarte.getCode().isEmpty()) {
            String nomFichierImage = laCarte.getCode() + ".png";
            String cheminImageCarteSpecifique = CHEMIN_IMAGES_CARTES + nomFichierImage;
            imagePrincipaleChargee = essayerChargerImageCartePrincipale(vueImage, cheminImageCarteSpecifique);
        } else {
            System.err.println("Carte ou code de carte nul. Tentative de chargement du dos de carte.");
        }

        if (!imagePrincipaleChargee) {
            // Repli sur le dos de carte si l'image spécifique n'est pas trouvée ou si la carte/code est invalide
            essayerChargerImage(vueImage, CHEMIN_DOS_CARTE, "Image du dos de carte non trouvée pour le repli");
        }
        return vueImage;
    }

    public static ImageView creerVueImagePourDosCarte(double largeurDesiree, double hauteurDesiree) {
        ImageView vueImage = new ImageView();
        configurerTailleVueImage(vueImage, largeurDesiree, hauteurDesiree);
        essayerChargerImage(vueImage, CHEMIN_DOS_CARTE, "Image du dos de carte non trouvée");
        return vueImage;
    }

    public static ImageView creerVueImagePourIconeEnergie(Type leTypeEnergie, double tailleDesiree) {
        ImageView vueImage = new ImageView();
        configurerTailleVueImage(vueImage, tailleDesiree, tailleDesiree);

        if (leTypeEnergie != null) {
            String nomFichierImage = leTypeEnergie.asLetter() + ".png";
            String cheminImageComplet = CHEMIN_IMAGES_ICONES_ENERGIE + nomFichierImage;
            essayerChargerImage(vueImage, cheminImageComplet, "Image non trouvée pour l'icône d'énergie");
        } else {
            System.err.println("Type d'énergie nul pour creerVueImagePourIconeEnergie.");
        }
        return vueImage;
    }
}
