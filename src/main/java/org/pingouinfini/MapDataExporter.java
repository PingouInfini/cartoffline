package org.pingouinfini;


import org.pingouinfini.geojson.Coordonnee;
import org.pingouinfini.geojson.Feature;
import org.pingouinfini.geojson.Geometry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;


public class MapDataExporter {

    static int ICON_SIZE = 36;

    public static void generateLeafletJSFromGeoJson(List<Feature> features, String outputPath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (Feature feature : features) {
                Geometry geometry = feature.getGeometry();
                String name = String.valueOf(feature.getProperties().get("name"));
                String description = String.valueOf(feature.getProperties().get("description"));
                String icon = String.valueOf(feature.getProperties().getOrDefault("icon", "default.png"));
                String imageFilename = String.valueOf(feature.getProperties().getOrDefault("imageFilename", null));
                String htmlImage = "";
                if (imageFilename != null && !imageFilename.equals("null")) {
                    htmlImage = String.format("<br><img src='images/illustration/%s' width='200' style='margin-top:10px;'>", imageFilename);
                }

                // Construction de la popup
                StringBuilder popupContent = new StringBuilder();
                if (name != null && !name.equalsIgnoreCase("null") && !name.isEmpty()) {
                    popupContent.append("<b>Nom : </b>").append(escape(name)).append("<br>");
                }
                if (description != null && !description.equalsIgnoreCase("null") && !description.isEmpty()) {
                    popupContent.append("<b>Description : </b>").append(escape(description));
                }
                if (popupContent.length() == 0) {
                    popupContent.append("Non renseigné");
                }
                popupContent.append(htmlImage);

                switch (geometry.getType()) {
                    case "Point":
                        List<Double> coords = (List<Double>) geometry.getCoordinates(); // [lon, lat] ou [lat, lon] selon ton modèle
                        double lat = coords.get(1);
                        double lon = coords.get(0);
                        String pointLine = String.format(
                                Locale.ENGLISH,
                                "L.marker([%f, %f],{icon:L.icon({iconUrl:\"images/marker/%s\",iconSize:[%d,%d]})})" +
                                        ".addTo(map).bindPopup(\"%s\");",
                                lat, lon, icon, ICON_SIZE, ICON_SIZE, popupContent
                        );
                        writer.write(pointLine);
                        writer.newLine();
                        break;

                    case "Polygon":
                        // On suppose que geometry.getCoordinates() retourne List<List<List<Double>>>
                        // car GeoJSON encode les Polygons ainsi : [[[lon, lat], [lon, lat], ...]]
                        List<Coordonnee> polygonCoords = (List<Coordonnee>) geometry.getCoordinates().get(0); // outer ring
                        String coordString = polygonCoords.stream()
                                .map(coord -> String.format(Locale.ENGLISH, "[%f, %f]", coord.getLatitude(), coord.getLongitude())) // lat, lon
                                .collect(Collectors.joining(",\n  "));
                        String color = feature.getProperties().getOrDefault("color", "#000000").toString();
                        Double weight = Optional.ofNullable(feature.getProperties().get("weight"))
                                .map(obj -> (obj instanceof Number) ? ((Number) obj).doubleValue() : Double.parseDouble(obj.toString()))
                                .orElse(0.5);
                        String fillColor = feature.getProperties().getOrDefault("fillColor", "#727272").toString();
                        Double fillOpacity = Optional.ofNullable(feature.getProperties().get("fillOpacity"))
                                .map(obj -> (obj instanceof Number) ? ((Number) obj).doubleValue() : Double.parseDouble(obj.toString()))
                                .orElse(0.5);

                        String polygonLine = String.format(
                                Locale.ENGLISH,
                                "L.polygon([\n  %s\n], {\n" +
                                        "  color: \"%s\",\n" +
                                        "  weight: %f,\n" +
                                        "  fillColor: \"%s\",\n" +
                                        "  fillOpacity: %f\n" +
                                        "}).addTo(map).bindPopup(\"%s\");",
                                coordString, color, weight, fillColor, fillOpacity, popupContent
                        );
                        writer.write(polygonLine);
                        writer.newLine();
                        break;

                    default:
                        System.err.println("Unsupported geometry type: " + geometry.getType());
                }
            }
            System.out.println("Fichier JS généré avec succès : " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String escape(String input) {
        return input.replace("\"", "\\\"").replace("\n", "").replace("\r", "");
    }
}
