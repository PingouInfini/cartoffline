package org.pingouinfini;

import org.geotools.geojson.geom.GeometryJSON;
import org.pingouinfini.geojson.Coordonnee;
import org.pingouinfini.geojson.Feature;
import org.pingouinfini.geojson.Point;
import org.pingouinfini.geojson.Polygon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class Main {

    // Par choix, nous utiliserons une définition de point au standard [lon, lat]
    public static void main(String[] args) throws IOException {
        Path outputPath = Paths.get("outputDirectory");

        // Etape 1 : générer le répertoire de sortie à partir du template
        ExportGenerator.generateNewExport(outputPath);

        // Etape 2 : créer le fichier js avec la liste des Feature (les points et les polygones à afficher)
        List<Feature> features = new ArrayList<>();

        List<Point> points = new ArrayList<>();
        // === POINT 1 ===
        String resourcePath1 = "/samples/images/piece_eau_suisses.jpg";
        String fileName1 = Paths.get(resourcePath1).getFileName().toString();
        Image image1 = ImageIO.read(Objects.requireNonNull(Main.class.getResourceAsStream(resourcePath1)));
        ExportGenerator.saveImage(image1, outputPath, fileName1);

        String resourcePathIcon1 = "/samples/marker/vagues.png";
        String fileNameIcone1 = Paths.get(resourcePathIcon1).getFileName().toString();
        Image icon1 = ImageIO.read(Objects.requireNonNull(Main.class.getResourceAsStream(resourcePathIcon1)));
        ExportGenerator.saveIcon(icon1, outputPath, fileNameIcone1);

        Point p1 = new Point(48.79795482082478, 2.115781744016367, "Pièce d'Eau des Suisses", "La pièce d'eau des Suisses est un bassin faisant partie du parc du château de Versailles, construit entre 1679 et 1682. Elle doit son nom au fait d'avoir été creusée par un régiment de Gardes suisses. Elle a été créée pour drainer le potager du Roi.",
                fileNameIcone1, fileName1);
        points.add(p1);
        features.add(ExportGenerator.createPointFeature(p1));

        // === POINT 2 ===
        String resourcePathIcon2 = "/samples/marker/question.png";
        String fileNameIcone2 = Paths.get(resourcePathIcon2).getFileName().toString();
        Image icon2 = ImageIO.read(Objects.requireNonNull(Main.class.getResourceAsStream(resourcePathIcon2)));
        ExportGenerator.saveIcon(icon2, outputPath, fileNameIcone2);
        Point p2 = new Point(48.81434371678219, 2.0839889191819077).icon(fileNameIcone2);
        points.add(p2);
        features.add(ExportGenerator.createPointFeature(p2));

        // === POINT 3 ===
        Point p3 = new Point(48.80425807017104, 2.0922632311615756).name("La Lanterne").build();
        points.add(p3);
        features.add(ExportGenerator.createPointFeature(p3));

        // === POINT 4 ===
        String resourcePath2 = "/samples/images/char_apollon.jpg";
        String fileName2 = Paths.get(resourcePath2).getFileName().toString();
        Image image2 = ImageIO.read(Objects.requireNonNull(Main.class.getResourceAsStream(resourcePath2)));
        ExportGenerator.saveImage(image2, outputPath, fileName2);
        Point p4 = new Point(48.80748446195294, 2.110689627619864).description("Chef-d’œuvre de la sculpture française du XVIIe siècle, Apollon sur son char appartient aux premières grandes commandes pour les jardins de Versailles.").filename(fileName2).build();
        points.add(p4);
        features.add(ExportGenerator.createPointFeature(p4));

        // === POINT 5 ===
        String resourcePath3 = "/samples/images/hameau_reine.jpg";
        String fileName3 = Paths.get(resourcePath3).getFileName().toString();
        Image image3 = ImageIO.read(Objects.requireNonNull(Main.class.getResourceAsStream(resourcePath3)));
        ExportGenerator.saveImage(image3, outputPath, fileName3);
        Point p5 = new Point(48.819594882175274, 2.113171086205837).name("Hameau de la Reine").description("Le hameau de la Reine est une dépendance du Petit Trianon située dans le parc du château de Versailles.").filename(fileName3).build();
        points.add(p5);
        features.add(ExportGenerator.createPointFeature(p5));

        List<Polygon> polygons = new ArrayList<>();
        // === POLYGON 1 ===
        String resourcePath4 = "/samples/images/grand_canal_versailles.jpg";
        String fileName4 = Paths.get(resourcePath4).getFileName().toString();
        Image image4 = ImageIO.read(Objects.requireNonNull(Main.class.getResourceAsStream(resourcePath4)));
        ExportGenerator.saveImage(image4, outputPath, fileName4);
        List<Coordonnee> polygonRing1 = Arrays.asList(
                new Coordonnee(48.81370902693686, 2.088759321722403),
                new Coordonnee(48.81274817237158, 2.0879439301527656),
                new Coordonnee(48.809922022801516, 2.099016089361526),
                new Coordonnee(48.80548464659237, 2.0967844913814657),
                new Coordonnee(48.80528679264919, 2.0976427982968735),
                new Coordonnee(48.80944156153209, 2.1007541608652267),
                new Coordonnee(48.80751967041602, 2.1079424812817664),
                new Coordonnee(48.80856541442403, 2.108800788197174),
                new Coordonnee(48.810430741480545, 2.101204771995816),
                new Coordonnee(48.813836197484676, 2.10279263978932),
                new Coordonnee(48.81401988770648, 2.101891417528142),
                new Coordonnee(48.81095358585557, 2.099595446529426)
        );
        Polygon poly1 = new Polygon(polygonRing1, "Grand Canal de Versailles", "Plus grand bassin du parc du château de Versailles. En forme de croix, il fut construit entre 1667 et 1679, à l'instigation de Le Nôtre.")
                .fillColor("pink").filename(fileName4);
        polygons.add(poly1);
        features.add(ExportGenerator.createPolygonFeature(poly1));

        List<Coordonnee> polygonRing2 = Arrays.asList(
                new Coordonnee(48.82308811184643, 2.098874917723104),
                new Coordonnee(48.822424117995205, 2.101310363502395),
                new Coordonnee(48.82002236465385, 2.099658122753448),
                new Coordonnee(48.81988814563051, 2.0990358502635855),
                new Coordonnee(48.82191552101063, 2.097458711366864)
        );

        Polygon poly2 = new Polygon(polygonRing2);
        polygons.add(poly2);
        features.add(ExportGenerator.createPolygonFeature(poly2));

        List<Coordonnee> polygonRing3 = Arrays.asList(
                new Coordonnee(48.80266440080202, 2.0836168196017097),
                new Coordonnee(48.804852967976565, 2.08367751106341),
                new Coordonnee(48.80564242767274, 2.0897163115025994),
                new Coordonnee(48.80202480149371, 2.087045887187782)
        );

        Polygon poly3 = new Polygon(polygonRing3).name("INRAE").fillColor("cyan").fillOpacity(0.8);
        features.add(ExportGenerator.createPolygonFeature(poly3));


        // === EXPORT JS ===
        MapDataExporter.generateLeafletJSFromGeoJson(features, outputPath + "/data/data.js");

        // Etape 3 : Stocker des tuiles (à remplacer par des appels multithread à geoserver sur un layer)
        Path sourceDir = Paths.get("src/main/resources/samples/tiles");
        Path targetDir = outputPath.resolve("cache-carto");
        Files.walk(sourceDir).forEach(source -> {
            try {
                Path target = targetDir.resolve(sourceDir.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        // Etape 4 : export de tuiles
        TileDownloader.downloadTiles(0, 5, "KORTANA:s_5da0bf5419ef73d98541518e1fd64e51__255a7c81-bdfc-4e99-998d-09f2afbcc120", Paths.get("outputDirectory"));

        // Export GeoJSON
        GeoJsonExporter.exportGeoJson(points, polygons, "export.geojson");
    }
}