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

    private static String POINT_EMOJI = "\uD83D\uDCCD";
    private static String POLYGON_EMOJI = " \u2B1F ";
    private static String LINE_EMOJI = "\u303D\uFE0F";
    private static String KML_EMOJI = "\uD83D\uDDFA\uFE0F";

    public static void generateLeafletJSFromGeoJson(List<Feature> features, String outputPath) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath), StandardCharsets.UTF_8)) {

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

                switch (type) {
                    case "Point":
                        managedFeaturePoint(feature, geometry, writer, name, icon, popupContent, hasAdditionalContent);
                        break;
                    case "Polygon":
                        managedFeaturePolygon(feature, geometry, writer, popupContent, hasAdditionalContent);
                        break;
                    case "Line":
                        managedFeatureLine(feature, geometry, writer, popupContent);
                        break;
                    default:
                        System.err.println("Unsupported geometry type: " + type);
                        break;
                }
            }
            System.out.println("Fichier JS généré avec succès : " + outputPath);
        } catch (IOException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }


    public static void addKmlToLeafletMap(Path kmlFilePath, String outputPath) throws IOException {
        byte[] bytes = Files.readAllBytes(kmlFilePath);
        String content = new String(bytes, StandardCharsets.UTF_8);
        addKmlToLeafletMap(content, outputPath, kmlFilePath.getFileName().toString(), DisplayLayer.KML);
    }

    public static void addKmlToLeafletMap(String kmlContent, String outputPath, String name, DisplayLayer kmlDisplayLayer) throws IOException {
        // Échapper les simples quotes
        String escapedContent = kmlContent.replace("'", "\\'");

        // Supprimer tous les sauts de ligne (Unix \n, Windows \r\n)
        escapedContent = escapedContent.replaceAll("\\r?\\n", "");

        // Générer un UUID JS-compatible
        String uuid = UUID.randomUUID().toString().replace("-", "_");

        // Nom par défaut si null ou vide
        if (name == null || name.isEmpty()) {
            name = "kml_" + uuid;
        }

        StringBuilder jsCode = new StringBuilder();
        jsCode.append("\n");
        jsCode.append("const parser_").append(uuid).append(" = new DOMParser();\n");
        jsCode.append("const kml_").append(uuid).append(" = parser_").append(uuid)
                .append(".parseFromString('").append(escapedContent).append("', 'text/xml');\n");
        jsCode.append("const kmlLayer_").append(uuid).append(" = new L.KML(kml_").append(uuid).append(");\n");
        jsCode.append("kmlLayer_").append(uuid).append(".addTo(map);\n");
        jsCode.append(kmlDisplayLayer.getLabel()).append(".children.push({ label: \"").append(KML_EMOJI)
                .append(truncateLabel(escape(name))).append("\", layer: kmlLayer_").append(uuid).append(" });\n");

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(jsCode.toString());
        }
    }


    private static void managedFeaturePoint(
            Feature feature,
            Geometry geometry,
            BufferedWriter writer,
            String name,
            String icon,
            StringBuilder popupContent,
            boolean hasAdditionalContent) throws IOException {

        List<Double> coords = (List<Double>) geometry.getCoordinates();
        double lat = coords.get(1);
        double lon = coords.get(0);
        String markerId = String.format("marker_%s", geometry.hashCode());

        // Coordonnées
        writer.write(String.format(Locale.ENGLISH,
                "const origin_%s = L.latLng(%f, %f);\n",
                markerId, lat, lon));

        // Crée le marker
        writer.write(String.format(Locale.ENGLISH,
                "const %s = L.marker(origin_%s, { draggable: true," +
                        "icon: createIcon(\"images/marker/%s\", getIconSize(map.getZoom()))" +
                        "}).bindPopup(\"%s\");\n",
                markerId, markerId, icon, popupContent));

        // Crée une ligne reliant le marker à sa coordonnée en cas de drag & drop
        writer.write(String.format(Locale.ENGLISH,
                "const line_%s = L.polyline([origin_%s,origin_%s], { color: 'magenta' }).addTo(map)\n",
                markerId, markerId, markerId));

        writer.write(String.format(Locale.ENGLISH,
                "%s.on('drag', function (e) { const newPos = e.target.getLatLng(); line_%s.setLatLngs([origin_%s, newPos])});\n",
                        markerId, markerId, markerId));

        // L'ajoute à la carte directement
        writer.write(String.format(Locale.ENGLISH,
                "%s.addTo(map);\n", markerId));

        // L'ajoute dans le contrôle de couches arborescent sous "Ponctuel"
        DisplayLayer displayLayer = (DisplayLayer) feature.getProperties().getOrDefault("displayLayer", DisplayLayer.AUTRE);
        writer.write(String.format(Locale.ENGLISH,
                "%s.children.push({ label: \"%s%s\", layer: %s });\n",
                displayLayer.getLabel(),
                POINT_EMOJI,
                truncateLabel(escape(name != null && !name.equalsIgnoreCase("null") && !name.isEmpty() ? name : markerId)), // label visible
                markerId // identifiant technique du marker
        ));

        // Enregistre le marker
        writer.write(String.format(Locale.ENGLISH,
                "mapMarkers.push({ leafletMarker: %s, iconUrl: \"images/marker/%s\" });\n",
                markerId, icon));

        // Contenu additionnel ?
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

        FillPattern pattern = null;
        Object patternObj = feature.getProperties().get("fillPattern");
        String polygonBaseId = "polygon_" + geometry.hashCode();
        boolean usePattern = false;
        List<Integer> angles = new ArrayList<>();
        StringBuilder patternJS = new StringBuilder();

        if (patternObj instanceof FillPattern) {
            pattern = (FillPattern) patternObj;

            if (pattern == FillPattern.NONE) {
                fillOpacity = 0.0;
            } else if (pattern != FillPattern.FULL) {
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
                } else if (pattern == FillPattern.MESH) {
                    angles.add(45);
                    angles.add(-45);
                }

                for (int i = 0; i < angles.size(); i++) {
                    String patternName = "pattern_" + polygonBaseId + "_a" + i;
                    patternJS.append(String.format(Locale.ENGLISH,
                            "const %s = new L.StripePattern({\n" +
                                    "  weight: 3,\n" +
                                    "  spaceWeight: 3,\n" +
                                    "  color: '%s',\n" +
                                    "  opacity: 1.0,\n" +
                                    "  angle: %d\n" +
                                    "});\n" +
                                    "%s.addTo(map);\n",
                            patternName, fillColor, angles.get(i), patternName));
                }
            }
        }

        DisplayLayer displayLayer = (DisplayLayer) feature.getProperties().getOrDefault("displayLayer", DisplayLayer.AUTRE);
        String name = Optional.ofNullable((String) feature.getProperties().get("name")).orElse(polygonBaseId);

        if (usePattern && !angles.isEmpty()) {
            writer.write(patternJS.toString());

            if (angles.size() == 1) {
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
                                "}).addTo(map).bindPopup(\"%s\");\n",
                        polygonBaseId, coordString, color, weight, fillColor, fillOpacity, patternRef, popupContent));

                writer.write(String.format("%s.children.push({ label: \"%s%s\", layer: %s });\n",
                        displayLayer.getLabel(), POLYGON_EMOJI, truncateLabel(escape(name)), polygonBaseId));
                if (hasAdditionalContent) {
                    writer.write(generatePopupToggleJS(polygonBaseId));
                }

            } else {
                String pathD1 = "";
                String pathD2 = "";
                int width = 8;
                int height = 8;
                if (pattern.equals(FillPattern.GRID)) {
                    pathD1 = String.format("M 0 %d L %d %d", height / 2, width, height / 2);
                    pathD2 = String.format("M %d 0 L %d %d", width / 2, width / 2, height);
                } else if (pattern.equals(FillPattern.MESH)) {
                    width = width * 2;
                    height = height * 2;
                    pathD1 = String.format("M 0 0 L %d %d", height, height);
                    pathD2 = String.format("M %d 0 L 0 %d", height, height);
                }

                writer.write(String.format("const crossHatchPattern_%s = new L.Pattern({width: %d,height: %d,patternUnits: 'userSpaceOnUse'});\n",
                        polygonBaseId, width, height));
                writer.write(String.format("const diag1_%s = new L.PatternPath({d: '%s',color: '%s','stroke-width': 2});\ncrossHatchPattern_%s.addShape(diag1_%s);\n",
                        polygonBaseId, pathD1, fillColor, polygonBaseId, polygonBaseId));
                writer.write(String.format("const diag2_%s = new L.PatternPath({d: '%s',color: '%s','stroke-width': 2});\ncrossHatchPattern_%s.addShape(diag2_%s);\n",
                        polygonBaseId, pathD2, fillColor, polygonBaseId, polygonBaseId));
                writer.write(String.format("crossHatchPattern_%s.addTo(map);\n",
                        polygonBaseId));
                writer.write(String.format("const polygonCoords_%s = [\n  %s\n];\n", polygonBaseId, coordString));
                writer.write(String.format("const %s = L.polygon(polygonCoords_%s, {color: '#000000', weight: 1, fillOpacity: 1, fillPattern: crossHatchPattern_%s}).addTo(map);\n",
                        polygonBaseId, polygonBaseId, polygonBaseId));
                writer.write(String.format("%s.bindPopup(\"%s\");\n", polygonBaseId, popupContent));
                writer.write(String.format("%s.children.push({ label: \"%s%s\", layer: %s });\n",
                        displayLayer.getLabel(), POLYGON_EMOJI, truncateLabel(escape(name)), polygonBaseId));

                if (hasAdditionalContent) {
                    writer.write(generatePopupToggleJS(polygonBaseId));
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

            writer.write(String.format("%s.children.push({ label: \"%s%s\", layer: %s });\n",
                    displayLayer.getLabel(), POLYGON_EMOJI, truncateLabel(escape(name)), polygonBaseId));
            if (hasAdditionalContent) {
                writer.write(generatePopupToggleJS(polygonBaseId));
            }
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
        String baseId = String.format("line_%s_%s", String.valueOf(first.getLatitude()).replace(".", "_"), String.valueOf(first.getLongitude()).replace(".", "_"));

        DisplayLayer displayLayer = (DisplayLayer) feature.getProperties().getOrDefault("displayLayer", DisplayLayer.AUTRE);
        String name = Optional.ofNullable((String) feature.getProperties().get("name")).orElse(baseId);

        if (lineStyle == LineStyle.CONTINUOUS && arrowStyle == ArrowStyle.NONE) {
            writer.write(String.format(Locale.ENGLISH,
                    "const %s = L.polyline([\n  %s\n], { color: \"%s\", weight: %.1f }).addTo(map).bindPopup(\"%s\");\n",
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

            if (arrowStyle == ArrowStyle.START || arrowStyle == ArrowStyle.BOTH) {
                patternList.add(String.format("{ offset: 0, symbol: L.Symbol.arrowHead({ pixelSize: 10, polygon: false, headAngle: 270, pathOptions: { stroke: true, color: '%s' } }) }", color));
            }
            if (arrowStyle == ArrowStyle.END || arrowStyle == ArrowStyle.BOTH) {
                patternList.add(String.format("{ offset: '100%%', symbol: L.Symbol.arrowHead({ pixelSize: 10, polygon: false, pathOptions: { stroke: true, color: '%s' } }) }", color));
            }

            writer.write(String.format(Locale.ENGLISH,
                    "const %s = L.polylineDecorator([\n  %s\n], {\n  patterns: [\n    %s\n  ]\n}).addTo(map).bindPopup(\"%s\");\n",
                    baseId, coordString, String.join(",\n    ", patternList), popupContent));
        }

        writer.write(String.format("%s.children.push({ label: \"%s%s\", layer: %s });\n",
                displayLayer.getLabel(), LINE_EMOJI, truncateLabel(escape(name)), baseId));
        writer.newLine();
    }

    private static String truncateLabel(String input) {
        if (input == null) return "";
        return input.length() > 30 ? input.substring(0, 27) + "…" : input;
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
