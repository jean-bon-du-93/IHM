package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type; // Importer la classe Type
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream; // Pour charger les ressources

public class VueUtils {

    private static final String IMAGE_PATH_CARTES = "/images/cartes/";
    private static final String IMAGE_PATH_ENERGIE_ICONES = "/images/energie/";
    private static final String DOS_CARTE_PATH = "/images/cartes/BACK.png";
    // private static final String DOS_CARTE_POKEMON_PATH = "/images/cartes/BACK.png";

    public static ImageView creerImageViewPourCarte(ICarte carte, double largeur, double hauteur) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(largeur);
        imageView.setFitHeight(hauteur);
        imageView.setPreserveRatio(true);

        if (carte != null && carte.getCode() != null && !carte.getCode().isEmpty()) {
            String nomImage = carte.getCode() + ".png";
            try {
                InputStream stream = VueUtils.class.getResourceAsStream(IMAGE_PATH_CARTES + nomImage);
                if (stream != null) {
                    Image image = new Image(stream);
                    imageView.setImage(image);
                } else {
                    System.err.println("Image non trouvée pour la carte : " + IMAGE_PATH_CARTES + nomImage + ". Utilisation du dos de carte.");
                    // Fallback sur le dos de carte si l'image spécifique n'est pas trouvée
                    InputStream dosStream = VueUtils.class.getResourceAsStream(DOS_CARTE_PATH);
                    if (dosStream != null) {
                        imageView.setImage(new Image(dosStream));
                    } else {
                        System.err.println("Image de dos de carte non trouvée pour fallback : " + DOS_CARTE_PATH);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image pour la carte " + nomImage + ": " + e.getMessage());
                try {
                    InputStream dosStream = VueUtils.class.getResourceAsStream(DOS_CARTE_PATH);
                    if (dosStream != null) {
                        imageView.setImage(new Image(dosStream));
                    } else {
                        System.err.println("Image de dos de carte non trouvée pour fallback : " + DOS_CARTE_PATH);
                    }
                } catch (Exception e2) {
                     System.err.println("Erreur lors du chargement de l'image de dos de carte pour fallback : " + e2.getMessage());
                }
            }
        } else {
            System.err.println("Carte ou code de carte null. Utilisation du dos de carte.");
             try {
                InputStream dosStream = VueUtils.class.getResourceAsStream(DOS_CARTE_PATH);
                if (dosStream != null) {
                    imageView.setImage(new Image(dosStream));
                } else {
                    System.err.println("Image de dos de carte non trouvée pour fallback : " + DOS_CARTE_PATH);
                }
            } catch (Exception e) {
                 System.err.println("Erreur lors du chargement de l'image de dos de carte pour fallback : " + e.getMessage());
            }
        }
        return imageView;
    }

    public static ImageView creerImageViewPourDosCarte(double largeur, double hauteur) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(largeur);
        imageView.setFitHeight(hauteur);
        imageView.setPreserveRatio(true);
        try {
            InputStream stream = VueUtils.class.getResourceAsStream(DOS_CARTE_PATH);
            if (stream != null) {
                Image image = new Image(stream);
                imageView.setImage(image);
            } else {
                System.err.println("Image de dos de carte non trouvée : " + DOS_CARTE_PATH);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image de dos de carte : " + e.getMessage());
        }
        return imageView;
    }

    public static ImageView creerImageViewPourIconeEnergie(Type typeEnergie, double taille) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(taille);
        imageView.setFitHeight(taille);
        imageView.setPreserveRatio(true);

        if (typeEnergie != null) {
            String nomImage = typeEnergie.asLetter() + ".png"; // Utilise la représentation lettre du Type Enum
            try {
                InputStream stream = VueUtils.class.getResourceAsStream(IMAGE_PATH_ENERGIE_ICONES + nomImage);
                if (stream != null) {
                    Image image = new Image(stream);
                    imageView.setImage(image);
                } else {
                    System.err.println("Image non trouvée pour l'icône d'énergie : " + IMAGE_PATH_ENERGIE_ICONES + nomImage);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'icône d'énergie " + nomImage + ": " + e.getMessage());
            }
        } else {
            System.err.println("Type d'énergie null pour creerImageViewPourIconeEnergie.");
        }
        return imageView;
    }
}
