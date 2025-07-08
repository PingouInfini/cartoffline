package org.pingouinfini;

import org.pingouinfini.geojson.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class MapDataExporter {

    static int ICON_SIZE = 36;

    public static void generateLeafletJSFromGeoJson(List<Feature> features, String outputPath) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath), StandardCharsets.UTF_8)) {

            // Ajout style pour popup width
            writer.write("const style = document.createElement('style');\n");
            writer.write("style.innerHTML = `.leaflet-popup-content { width: 200px !important; }`;\n");
            writer.write("document.head.appendChild(style);\n\n");

            for (Feature feature : features) {
                Geometry geometry = feature.getGeometry();
                String type = geometry.getType();
                String name = String.valueOf(feature.getProperties().get("name"));
                String description = String.valueOf(feature.getProperties().get("description"));
                String icon = String.valueOf(feature.getProperties().getOrDefault("icon", "default.png"));
                String imageFilename = String.valueOf(feature.getProperties().getOrDefault("imageFilename", null));

                boolean hasDescription = description != null && !description.equalsIgnoreCase("null") && !description.isEmpty();
                boolean hasImage = imageFilename != null && !imageFilename.equals("null");
                boolean hasAdditionalContent = hasDescription || hasImage;

                StringBuilder popupContent = new StringBuilder("<div style=\\\"width:200px;\\\">");

                if (name != null && !name.equalsIgnoreCase("null") && !name.isEmpty()) {
                    popupContent.append("<b>Nom : </b>").append(escape(name)).append("<br>");
                }

                if (hasAdditionalContent) {
                    popupContent.append("<div class=\\\"toggle-description\\\" style=\\\"cursor:pointer; color:blue; text-decoration:underline;\\\">▶ Plus d'informations</div>");
                    popupContent.append("<div class=\\\"additional-content\\\" style=\\\"display:none; margin-top:5px;\\\">");

                    if (hasDescription) {
                        popupContent.append("<b>Description : </b>").append(escape(description));
                    }

                    if (hasImage) {
                        popupContent.append("<br><img src='images/illustration/")
                                .append(imageFilename)
                                .append("' width='200' style='margin-top:10px;'>");
                    }

                    popupContent.append("</div>");
                }

                popupContent.append("</div>");

                if (type.equals("Point")) {
                    managedFeaturePoint(geometry, writer, icon, popupContent, hasAdditionalContent);
                } else if (type.equals("Polygon")) {
                    managedFeaturePolygon(feature, geometry, writer, popupContent, hasAdditionalContent);
                } else if (type.equals("Line")) {
                    managedFeatureLine(feature, geometry, writer, popupContent);
                } else {
                    System.err.println("Unsupported geometry type: " + type);
                }
            }
            System.out.println("Fichier JS généré avec succès : " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void addKmlToLeafletMap(Path kmlFilePath, String outputPath) throws IOException {
        byte[] bytes = Files.readAllBytes(kmlFilePath);
        String content = new String(bytes, StandardCharsets.UTF_8);
        addKmlToLeafletMap(content, outputPath);
    }

    public static void addKmlToLeafletMap(String kmlContent, String outputPath) throws IOException {
        // Échapper les simples quotes
        String escapedContent = kmlContent.replace("'", "\\'");

        // Supprimer tous les sauts de ligne (Unix \n, Windows \r\n)
        escapedContent = escapedContent.replaceAll("\\r?\\n", "");

        // Générer un UUID JS-compatible
        String uuid = UUID.randomUUID().toString().replace("-", "_");

        StringBuilder jsCode = new StringBuilder();
        jsCode.append("\n");
        jsCode.append("const parser_").append(uuid).append(" = new DOMParser();\n");
        jsCode.append("const kml_").append(uuid).append(" = parser_").append(uuid)
                .append(".parseFromString('").append(escapedContent).append("', 'text/xml');\n");
        jsCode.append("const track_").append(uuid).append(" = new L.KML(kml_").append(uuid).append(");\n");
        jsCode.append("map.addLayer(track_").append(uuid).append(");\n");

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(jsCode.toString());
        }
    }

    private static void managedFeaturePoint(Geometry geometry, BufferedWriter writer, String icon, StringBuilder popupContent, boolean hasAdditionalContent) throws IOException {
        List<Double> coords = (List<Double>) geometry.getCoordinates();
        double lat = coords.get(1);
        double lon = coords.get(0);
        String markerId = String.format("marker_%s", geometry.hashCode());
        writer.write(String.format(Locale.ENGLISH,
                "const %s = L.marker([%f, %f],{icon:L.icon({iconUrl:\"images/marker/%s\",iconSize:[%d,%d]})}).addTo(map).bindPopup(\"%s\");\n",
                markerId, lat, lon, icon, ICON_SIZE, ICON_SIZE, popupContent));
        if (hasAdditionalContent) {
            writer.write(generatePopupToggleJS(markerId));
        }
        writer.newLine();
    }

    private static void managedFeaturePolygon(Feature feature, Geometry geometry, BufferedWriter writer, StringBuilder popupContent, boolean hasAdditionalContent) throws IOException {
        List<List<Coordonnee>> coords = (List<List<Coordonnee>>) geometry.getCoordinates();
        List<Coordonnee> ring = coords.get(0);

        String coordString = ring.stream()
                .map(c -> String.format(Locale.ENGLISH, "[%f, %f]", c.getLatitude(), c.getLongitude()))
                .collect(Collectors.joining(",\n  "));

        String color = feature.getProperties().getOrDefault("color", "#000000").toString();
        double weight = Optional.ofNullable(feature.getProperties().get("weight"))
                .map(obj -> (obj instanceof Number) ? ((Number) obj).doubleValue() : Double.parseDouble(obj.toString()))
                .orElse(1.0);

        String fillColor = feature.getProperties().getOrDefault("fillColor", "#727272").toString();
        double fillOpacity = Optional.ofNullable(feature.getProperties().get("fillOpacity"))
                .map(obj -> (obj instanceof Number) ? ((Number) obj).doubleValue() : Double.parseDouble(obj.toString()))
                .orElse(0.5);

        LineStyle lineStyle = Optional.ofNullable(feature.getProperties().get("lineStyle"))
                .filter(LineStyle.class::isInstance)
                .map(LineStyle.class::cast)
                .orElse(LineStyle.CONTINUOUS);

        String dashArray;
        switch (lineStyle) {
            case DOT:
                dashArray = "\"1, 6\"";
                break;
            case DASH:
                dashArray = "\"8, 6\"";
                break;
            case MIXED:
                dashArray = "\"8, 6, 1, 6\"";
                break;
            case MIXED_TWO_POINT:
                dashArray = "\"8, 6, 1, 6, 1, 6\"";
                break;
            case DOT_LONG:
                dashArray = "\"4, 10\"";
                break;
            case CONTINUOUS:
            default:
                dashArray = null;
        }

        Object patternObj = feature.getProperties().get("fillPattern");
        String polygonBaseId = "polygon_" + geometry.hashCode();

        boolean usePattern = false;
        boolean bindPopupOnOverlay = false;
        List<Integer> angles = new ArrayList<>();
        StringBuilder patternJS = new StringBuilder();

        if (patternObj instanceof FillPattern) {
            FillPattern pattern = (FillPattern) patternObj;

            if (pattern == FillPattern.NONE) {
                fillOpacity = 0.0;
            } else if (pattern == FillPattern.FULL) {
                // rien
            } else {
                usePattern = true;

                if (pattern == FillPattern.DIAGONAL_LEFT) {
                    angles.add(45);
                } else if (pattern == FillPattern.DIAGONAL_RIGHT) {
                    angles.add(-45);
                } else if (pattern == FillPattern.HORIZONTAL) {
                    angles.add(0);
                } else if (pattern == FillPattern.VERTICAL) {
                    angles.add(90);
                } else if (pattern == FillPattern.GRID) {
                    angles.add(0);
                    angles.add(90);
                    bindPopupOnOverlay = true;
                } else if (pattern == FillPattern.MESH) {
                    angles.add(45);
                    angles.add(-45);
                    bindPopupOnOverlay = true;
                }

                for (int i = 0; i < angles.size(); i++) {
                    String patternName = "pattern_" + polygonBaseId + "_a" + i;
                    patternJS.append(String.format(Locale.ENGLISH,
                            "const %s = new L.StripePattern({\n" +
                                    "  weight: 3,\n" +
                                    "  spaceWeight: 3,\n" +
                                    "  color: '%s',\n" +
                                    "  opacity: %f,\n" +
                                    "  angle: %d\n" +
                                    "});\n" +
                                    "%s.addTo(map);\n",
                            patternName, fillColor, 1.0, angles.get(i), patternName));
                }
            }
        }

        if (usePattern && !angles.isEmpty()) {
            writer.write(patternJS.toString());

            if (angles.size() == 1) {
                String polygonId = polygonBaseId;
                String patternRef = "pattern_" + polygonBaseId + "_a0";
                String dashArrayLine = dashArray != null ? "  dashArray: " + dashArray + ",\n" : "";

                writer.write(String.format(Locale.ENGLISH,
                        "const %s = L.polygon([\n  %s\n], {\n" +
                                "  color: \"%s\",\n" +
                                "  weight: %f,\n" +
                                dashArrayLine +
                                "  fillColor: \"%s\",\n" +
                                "  fillOpacity: %f,\n" +
                                "  fillPattern: %s\n" +
                                "}).addTo(map);\n",
                        polygonId, coordString, color, weight, fillColor, fillOpacity, patternRef));

                writer.write(String.format(Locale.ENGLISH, "%s.bindPopup(\"%s\");\n", polygonId, popupContent));

            } else {
                writer.write(String.format(Locale.ENGLISH, "const polygonCoords_%s = [\n  %s\n];\n", polygonBaseId, coordString));

                for (int i = 0; i < angles.size(); i++) {
                    String polygonInstanceId = polygonBaseId + "_" + (i == 0 ? "main" : "overlay" + i);
                    String patternRef = "pattern_" + polygonBaseId + "_a" + i;
                    String dashArrayLine = dashArray != null ? "  dashArray: " + dashArray + ",\n" : "";

                    writer.write(String.format(Locale.ENGLISH,
                            "const %s = L.polygon(polygonCoords_%s, {\n" +
                                    "  color: \"%s\",\n" +
                                    "  weight: %f,\n" +
                                    dashArrayLine +
                                    "  fillColor: \"transparent\",\n" +
                                    "  fillOpacity: 1.0,\n" +
                                    "  fillPattern: %s\n" +
                                    "}).addTo(map);\n",
                            polygonInstanceId, polygonBaseId, color, weight, patternRef));

                    boolean isMain = (i == 0);
                    boolean isLastOverlay = (i == angles.size() - 1);
                    if ((bindPopupOnOverlay && isLastOverlay) || (!bindPopupOnOverlay && isMain)) {
                        writer.write(String.format(Locale.ENGLISH, "%s.bindPopup(\"%s\");\n", polygonInstanceId, popupContent));
                    }
                }
            }

        } else {
            String dashArrayLine = dashArray != null ? "  dashArray: " + dashArray + ",\n" : "";

            writer.write(String.format(Locale.ENGLISH,
                    "const %s = L.polygon([\n  %s\n], {\n" +
                            "  color: \"%s\",\n" +
                            "  weight: %f,\n" +
                            dashArrayLine +
                            "  fillColor: \"%s\",\n" +
                            "  fillOpacity: %f\n" +
                            "}).addTo(map).bindPopup(\"%s\");\n",
                    polygonBaseId, coordString, color, weight, fillColor, fillOpacity, popupContent));
        }

        if (hasAdditionalContent) {
            String targetPolygonId = (usePattern && angles.size() > 1)
                    ? (bindPopupOnOverlay ? polygonBaseId + "_overlay" + (angles.size() - 1) : polygonBaseId + "_main")
                    : polygonBaseId;
            writer.write(generatePopupToggleJS(targetPolygonId));
        }

        writer.newLine();
    }

    private static void managedFeatureLine(Feature feature, Geometry geometry, BufferedWriter writer, StringBuilder popupContent) throws IOException {
        List<List<Coordonnee>> coords = (List<List<Coordonnee>>) geometry.getCoordinates();
        List<Coordonnee> ring = coords.get(0);

        String coordString = ring.stream()
                .map(c -> String.format(Locale.ENGLISH, "[%f, %f]", c.getLatitude(), c.getLongitude()))
                .collect(Collectors.joining(",\n  "));

        Map<String, Object> props = feature.getProperties();

        String color = props.getOrDefault("color", "#000000").toString();

        double weight = Optional.ofNullable(props.get("weight"))
                .map(obj -> (obj instanceof Number) ? ((Number) obj).doubleValue() : Double.parseDouble(obj.toString()))
                .orElse(1.0);

        LineStyle lineStyle = Optional.ofNullable(props.get("lineStyle"))
                .filter(LineStyle.class::isInstance)
                .map(LineStyle.class::cast)
                .orElse(LineStyle.CONTINUOUS);

        ArrowStyle arrowStyle = Optional.ofNullable(props.get("arrowStyle"))
                .map(Object::toString)
                .map(s -> {
                    try {
                        return ArrowStyle.valueOf(s.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return ArrowStyle.NONE;
                    }
                })
                .orElse(ArrowStyle.NONE);

        Coordonnee first = ring.get(0);
        String baseId = String.format("arrow_%s_%s",
                String.valueOf(first.getLatitude()).replace(".", "_"),
                String.valueOf(first.getLongitude()).replace(".", "_"));

        if (lineStyle == LineStyle.CONTINUOUS) {
            writer.write(String.format(Locale.ENGLISH,
                    "var %s = L.polyline([\n  %s\n], {color: \"%s\", weight: %.1f}).addTo(map).bindPopup(\"%s\");\n",
                    baseId, coordString, color, weight, popupContent));
        } else {
            List<String> patternList = new ArrayList<>();

            // Ligne décorative selon style
            if (lineStyle == LineStyle.DOT) {
                patternList.add(String.format(Locale.ENGLISH,
                        "{ offset: 0, repeat: 5, symbol: L.Symbol.dash({pixelSize: 0, pathOptions: {color: '%s', weight: %.1f}}) }",
                        color, weight));
            } else if (lineStyle == LineStyle.DASH) {
                patternList.add(String.format(Locale.ENGLISH,
                        "{ offset: 12, repeat: 20, symbol: L.Symbol.dash({pixelSize: 10, pathOptions: {color: '%s', weight: %.1f}}) }",
                        color, weight));
            } else if (lineStyle == LineStyle.MIXED) {
                patternList.add(String.format(Locale.ENGLISH,
                        "{ offset: 12, repeat: 25, symbol: L.Symbol.dash({pixelSize: 10, pathOptions: {color: '%s', weight: %.1f}}) }",
                        color, weight));
                patternList.add(String.format(Locale.ENGLISH,
                        "{ offset: 0, repeat: 25, symbol: L.Symbol.dash({pixelSize: 0, pathOptions: {color: '%s', weight: %.1f}}) }",
                        color, weight));
            } else if (lineStyle == LineStyle.MIXED_TWO_POINT) {
                patternList.add(String.format(Locale.ENGLISH,
                        "{ offset: 8, repeat: 20, symbol: L.Symbol.dash({pixelSize: 0, pathOptions: {color: '%s', weight: %.1f}}) }",
                        color, weight));
                patternList.add(String.format(Locale.ENGLISH,
                        "{ offset: 12, repeat: 20, symbol: L.Symbol.dash({pixelSize: 0, pathOptions: {color: '%s', weight: %.1f}}) }",
                        color, weight));
                patternList.add(String.format(Locale.ENGLISH,
                        "{ offset: 20, repeat: 20, symbol: L.Symbol.dash({pixelSize: 5, pathOptions: {color: '%s', weight: %.1f}}) }",
                        color, weight));
            } else if (lineStyle == LineStyle.DOT_LONG) {
                patternList.add(String.format(Locale.ENGLISH,
                        "{ offset: 0, repeat: 10, symbol: L.Symbol.dash({pixelSize: 3, pathOptions: {color: '%s', weight: %.1f}}) }",
                        color, weight));
            }

            // Ajout des flèches dans les patterns
            if (arrowStyle == ArrowStyle.START || arrowStyle == ArrowStyle.BOTH) {
                patternList.add(String.format(
                        "{ offset: 0, symbol: L.Symbol.arrowHead({pixelSize: 10, polygon: false, headAngle: 270, pathOptions: {stroke: true, color: '%s'}}) }",
                        color));
            }
            if (arrowStyle == ArrowStyle.END || arrowStyle == ArrowStyle.BOTH) {
                patternList.add(String.format(
                        "{ offset: '100%%', symbol: L.Symbol.arrowHead({pixelSize: 10, polygon: false, pathOptions: {stroke: true, color: '%s'}}) }",
                        color));
            }

            writer.write(String.format(Locale.ENGLISH,
                    "var %s = L.polylineDecorator([\n  %s\n], {\n  patterns: [\n    %s\n  ]\n}).addTo(map).bindPopup(\"%s\");\n",
                    baseId, coordString, String.join(",\n    ", patternList), popupContent));
        }

        writer.newLine();
    }

    private static String escape(String input) {
        return input.replace("\"", "\\\"").replace("\n", "").replace("\r", "");
    }

    private static String generatePopupToggleJS(String elementId) {
        return String.format(
                "%s.on('popupopen', (e) => {\n" +
                        "  const container = e.popup.getElement();\n" +
                        "  const toggle = container?.querySelector('.toggle-description');\n" +
                        "  const content = container?.querySelector('.additional-content');\n" +
                        "  if (toggle && content) {\n" +
                        "    toggle.addEventListener('click', () => {\n" +
                        "      const visible = content.style.display === 'block';\n" +
                        "      content.style.display = visible ? 'none' : 'block';\n" +
                        "      toggle.textContent = visible ? '▶ Plus d\\'information' : '▼ Masquer les informations';\n" +
                        "    });\n" +
                        "  }\n" +
                        "});", elementId);
    }
}
