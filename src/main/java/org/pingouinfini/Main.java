package org.pingouinfini;

import gov.nasa.worldwind.geom.LatLon;
import org.pingouinfini.geojson.Feature;
import org.pingouinfini.geojson.Point;
import org.pingouinfini.geojson.Polygon;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gov.nasa.worldwind.geom.LatLon.fromDegrees;
import static org.pingouinfini.data.DataGenerator.*;


public class Main {

    // Par choix, nous utiliserons une définition de point au standard [lon, lat]
    public static void main(String[] args) throws IOException {
        Path outputPath = Paths.get("outputDirectory");

        // Etape 1 : générer le répertoire de sortie à partir du template
        ExportGenerator.generateNewExport(outputPath);

        // Etape 2 : créer le fichier js avec la liste des Feature (les points et les polygones à afficher)
        List<Feature> features = new ArrayList<>();

        // POINTS
        List<Point> points = new ArrayList<>();
        generatePointsSample(outputPath, points, features);

        // POLYGONS
        List<Polygon> polygons = new ArrayList<>();
        generatePolygonsSample(outputPath, polygons, features);

        // LINES
        generateLinesSample(features);

        // === EXPORT JS ===
        MapDataExporter.generateLeafletJSFromGeoJson(features, outputPath + "/data/data.js");

        // === KML ===
        MapDataExporter.addKmlToLeafletMap(Paths.get("src/main/resources/samples/kml/balade-a-versailles.kml"), outputPath + "/data/data.js");
        MapDataExporter.addKmlToLeafletMap(Paths.get("src/main/resources/samples/kml/arrondissements-paris.kml"), outputPath + "/data/data.js");

        boolean USE_SAMPLE = true;

        // Etape 3a : Export des tuiles depuis le répertoire sample
        if (USE_SAMPLE) {
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
        } else {
            // Etape 3b : export de tuiles depuis geoserver
            List<String> layers = Arrays.asList("s_548b69ac3886e076a5f4a0a8d07f497b__99b4d0a6-c0e3-4c94-8ac1-29e33437b6a1",
                    "s_577604db5c0db6e34e8ee0d8e8d74040__66336714-d8b9-4472-ba06-7adcff84dcc2",
                    "s_5648fbcd8e65963604a6b4305e224278__69ddec48-ecb9-4a75-9ee1-2dd096848033");
            LatLon latLonMin = fromDegrees(48.70374254612109, 1.8136593222179598);
            LatLon latLonMax = fromDegrees(48.964550947457376, 2.5978297845196505);
            TileDownloader.downloadTiles(latLonMin, latLonMax, layers, outputPath);
        }
        // Etape 4 : Export GeoJSON
        GeoJsonExporter.exportGeoJson(points, polygons, "export.geojson");
    }
}