package org.pingouinfini;

import org.pingouinfini.geojson.*;
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
                .borderColor("darkblue").filename(fileName4).fillPattern(FillPattern.NONE).weight(3.0).lineStyle(LineStyle.DASH);
        polygons.add(poly1);
        features.add(ExportGenerator.createPolygonFeature(poly1));

        // === POLYGON 2 ===
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

        // === POLYGON 3 ===
        List<Coordonnee> polygonRing3 = Arrays.asList(
                new Coordonnee(48.80266440080202, 2.0836168196017097),
                new Coordonnee(48.804852967976565, 2.08367751106341),
                new Coordonnee(48.80564242767274, 2.0897163115025994),
                new Coordonnee(48.80202480149371, 2.087045887187782)
        );

        Polygon poly3 = new Polygon(polygonRing3).name("INRAE").fillPattern(FillPattern.FULL).lineStyle(LineStyle.DOT)
                .fillColor("cyan").fillOpacity(0.8);
        features.add(ExportGenerator.createPolygonFeature(poly3));

        // === POLYGON 4 ===
        List<Coordonnee> polygonRing4 = Arrays.asList(
                new Coordonnee(48.78804210972139, 2.100575900209618),
                new Coordonnee(48.791064399580165, 2.101764533826793),
                new Coordonnee(48.79046485047943, 2.1067605094989825),
                new Coordonnee(48.78745476101201, 2.1051261382753665)
        );

        Polygon poly4 = new Polygon(polygonRing4).name("STAT").fillPattern(FillPattern.DIAGONAL_LEFT).fillColor("RED")
                .lineStyle(LineStyle.MIXED).weight(3.0).fillOpacity(0.8);
        features.add(ExportGenerator.createPolygonFeature(poly4));

        // === POLYGON 5 ===
        List<Coordonnee> polygonRing5 = Arrays.asList(
                new Coordonnee(48.83009583774547, 2.114081520504228),
                new Coordonnee(48.82795888333487, 2.1193119958506013),
                new Coordonnee(48.82503738695459, 2.118079591495158),
                new Coordonnee(48.82550007135308, 2.1145031920540527)
        );

        Polygon poly5 = new Polygon(polygonRing5).name("Parly2").fillPattern(FillPattern.DIAGONAL_RIGHT)
                .lineStyle(LineStyle.MIXED_TWO_POINT).weight(2.0).fillColor("YELLOW").fillOpacity(0.8);
        features.add(ExportGenerator.createPolygonFeature(poly5));

        // === POLYGON 6 ===
        List<Coordonnee> polygonRing6 = Arrays.asList(
                new Coordonnee(48.80186740422758, 2.129628741809515),
                new Coordonnee(48.80153988543428, 2.130798729671492),
                new Coordonnee(48.800703739521275, 2.130161086286715),
                new Coordonnee(48.80091952044616, 2.129096397332315)
        );

        Polygon poly6 = new Polygon(polygonRing6).name("Mairie Versailles").fillPattern(FillPattern.GRID)
                .lineStyle(LineStyle.DOT_LONG).weight(2.0).fillColor("GREY").fillOpacity(1.0);
        features.add(ExportGenerator.createPolygonFeature(poly6));

        // === POLYGON 7 ===
        List<Coordonnee> polygonRing7 = Arrays.asList(
                new Coordonnee(48.79590518349168, 2.1345089479474373),
                new Coordonnee(48.796111442472366, 2.13660437968734),
                new Coordonnee(48.795088072272314, 2.1370379172886995),
                new Coordonnee(48.794818343317, 2.1346895886146706)
        );

        Polygon poly7 = new Polygon(polygonRing7).name("Gare des Chantiers").fillPattern(FillPattern.MESH)
                .fillColor("Lime").fillOpacity(1.0);
        features.add(ExportGenerator.createPolygonFeature(poly7));

        // === POLYGON 8 ===
        List<Coordonnee> polygonRing8 = Arrays.asList(
                new Coordonnee(48.80645048196352, 2.1211373993181404),
                new Coordonnee(48.80599045944829, 2.1231364893688527),
                new Coordonnee(48.802659136002376, 2.1209447159397588),
                new Coordonnee(48.803166780532514, 2.1194273343350014)
        );

        Polygon poly8 = new Polygon(polygonRing8).name("Chateau").fillPattern(FillPattern.HORIZONTAL)
                .fillColor("ORANGE").fillOpacity(1.0);
        features.add(ExportGenerator.createPolygonFeature(poly8));

        // === POLYGON 9 ===
        List<Coordonnee> polygonRing9 = Arrays.asList(
                new Coordonnee(48.803085853674894, 2.1304336451816854),
                new Coordonnee(48.80286654306353, 2.1311640303485753),
                new Coordonnee(48.802387829473, 2.1309420505429517),
                new Coordonnee(48.80261893315613, 2.1301436393065964)
        );

        Polygon poly9 = new Polygon(polygonRing9).name("Préfecture").fillPattern(FillPattern.VERTICAL)
                .fillColor("Fuchsia").fillOpacity(1.0);
        features.add(ExportGenerator.createPolygonFeature(poly9));

        // === LINE 1 ===
        List<Coordonnee> lineCoord1 = Arrays.asList(
                new Coordonnee(48.83164463572279, 2.14765557981711),
                new Coordonnee(48.83679231076493, 2.186575523902859),
                new Coordonnee(48.83930609936617, 2.196214575475498),
                new Coordonnee(48.83990460186796, 2.219493794367908),
                new Coordonnee(48.84804352590949, 2.225313599091011),
                new Coordonnee(48.848488787524566, 2.2530594957312995)
        );
        Line line1 = new Line(lineCoord1).name("A13").lineStyle(LineStyle.CONTINUOUS);
        features.add(ExportGenerator.createLineFeature(line1));

        // === LINE 2 ===
        List<Coordonnee> lineCoord2 = Arrays.asList(
                new Coordonnee(48.846048038308545, 2.2585084955916948),
                new Coordonnee(48.88744905076601, 2.290085344496615),
                new Coordonnee(48.900388729010025, 2.320580794466436),
                new Coordonnee(48.899677844631896, 2.3926019635440965),
                new Coordonnee(48.83608392085882, 2.412499703949936),
                new Coordonnee(48.819994805502596, 2.3493460061400957),
                new Coordonnee(48.84476704853975, 2.258941055165735)
        );
        Line line2 = new Line(lineCoord2).name("Périphérique").lineStyle(LineStyle.DOT).color("red").weight(10.0);
        features.add(ExportGenerator.createLineFeature(line2));

        // === LINE 3 ===
        List<Coordonnee> lineCoord3 = Arrays.asList(
                new Coordonnee(48.84486175156046, 2.2735400427290626),
                new Coordonnee(48.824829359947216, 2.2495295767955006),
                new Coordonnee(48.82714507090403, 2.229189500558789),
                new Coordonnee(48.83680949163688, 2.2235309831094785),
                new Coordonnee(48.86599277494265, 2.2285777689426474),
                new Coordonnee(48.89596339507701, 2.2645170013909723)
        );
        Line line3 = new Line(lineCoord3).name("SEINE").lineStyle(LineStyle.DASH).color("orange").arrowStyle(ArrowStyle.END);
        features.add(ExportGenerator.createLineFeature(line3));

        // === LINE 4 ===
        List<Coordonnee> lineCoord4 = Arrays.asList(
                new Coordonnee(48.857809062435074, 2.2952909083071096),
                new Coordonnee(48.85283940460138, 2.3029727550927612)
        );
        Line line4 = new Line(lineCoord4).name("Champ-de-Mars").lineStyle(LineStyle.MIXED).color("cyan").arrowStyle(ArrowStyle.BOTH);
        features.add(ExportGenerator.createLineFeature(line4));

        // === LINE 5 ===
        List<Coordonnee> lineCoord5 = Arrays.asList(
                new Coordonnee(48.87380141404359, 2.2955415867919147),
                new Coordonnee(48.86572140218105, 2.3207196324567585)
        );
        Line line5 = new Line(lineCoord5).name("Champs-Élysées (direction Arc de Triomphe) ").lineStyle(LineStyle.MIXED_TWO_POINT).color("purple").arrowStyle(ArrowStyle.START);
        features.add(ExportGenerator.createLineFeature(line5));

        // === LINE 6 ===
        List<Coordonnee> lineCoord6 = Arrays.asList(
                new Coordonnee(48.79528571636546, 2.1359229542601943),
                new Coordonnee(48.79668841194676, 2.1536283626620096),
                new Coordonnee(48.80068533222095, 2.1711014056101083),
                new Coordonnee(48.799548759158796, 2.1844024663302912),
                new Coordonnee(48.8033207925206, 2.234514131049336),
                new Coordonnee(48.81703348818656, 2.249518488759423)
        );
        Line line6 = new Line(lineCoord6).name("RER C").lineStyle(LineStyle.DOT_LONG).color("yellow").arrowStyle(ArrowStyle.NONE);
        features.add(ExportGenerator.createLineFeature(line6));

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
        //TileDownloader.downloadTiles(0, 5, "KORTANA:s_5da0bf5419ef73d98541518e1fd64e51__255a7c81-bdfc-4e99-998d-09f2afbcc120", Paths.get("outputDirectory"));

        // Export GeoJSON
//        GeoJsonExporter.exportGeoJson(points, polygons, "export.geojson");
    }
}