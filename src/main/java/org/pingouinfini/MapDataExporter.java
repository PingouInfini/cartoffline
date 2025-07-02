package org.pingouinfini;

import org.pingouinfini.geojson.Coordonnee;
import org.pingouinfini.geojson.Feature;
import org.pingouinfini.geojson.FillPattern;
import org.pingouinfini.geojson.Geometry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class MapDataExporter {

    static int ICON_SIZE = 36;

    public static void generateLeafletJSFromGeoJson(List<Feature> features, String outputPath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

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
                    List<Double> coords = (List<Double>) geometry.getCoordinates();
                    double lat = coords.get(1);
                    double lon = coords.get(0);
                    String markerId = String.format("marker_%s_%s",
                            String.valueOf(lat).replace(".", "_"),
                            String.valueOf(lon).replace(".", "_"));
                    writer.write(String.format(Locale.ENGLISH,
                            "const %s = L.marker([%f, %f],{icon:L.icon({iconUrl:\"images/marker/%s\",iconSize:[%d,%d]})}).addTo(map).bindPopup(\"%s\");\n",
                            markerId, lat, lon, icon, ICON_SIZE, ICON_SIZE, popupContent));
                    if (hasAdditionalContent) {
                        writer.write(generatePopupToggleJS(markerId));
                    }
                    writer.newLine();

                } else if (type.equals("Polygon")) {
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

                    Object patternObj = feature.getProperties().get("fillPattern");
                    String polygonId = "polygon_" + geometry.hashCode();

                    boolean usePattern = false;
                    String patternRef = null;
                    StringBuilder patternJS = new StringBuilder();

                    if (patternObj instanceof FillPattern) {
                        FillPattern pattern = (FillPattern) patternObj;

                        List<Integer> angles = new ArrayList<>();

                        if (pattern == FillPattern.NONE) {
                            fillOpacity = 0.0;
                        } else if (pattern == FillPattern.FULL) {
                            // No pattern, solid fill
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
                            } else if (pattern == FillPattern.MESH) {
                                angles.add(45);
                                angles.add(-45);
                            }

                            List<String> patternNames = new ArrayList<>();
                            for (int i = 0; i < angles.size(); i++) {
                                String patternName = "pattern_" + polygonId + "_a" + i;
                                patternNames.add(patternName);

                                patternJS.append(String.format(Locale.ENGLISH,
                                        "const %s = new L.StripePattern({\n" +
                                                "  weight: 4,\n" +
                                                "  spaceWeight: 4,\n" +
                                                "  color: '%s',\n" +
                                                "  opacity: %f,\n" +
                                                "  angle: %d\n" +
                                                "});\n" +
                                                "%s.addTo(map);\n",
                                        patternName, fillColor, fillOpacity, angles.get(i), patternName));
                            }

                            // Si un seul motif : on peut utiliser directement fillPattern: motif
                            // Si plusieurs : on utilise un groupe de layers
                            if (patternNames.size() == 1) {
                                patternRef = patternNames.get(0);
                            } else {
                                // Crée un LayerGroup combiné de plusieurs patterns (Leaflet ne le supporte pas nativement, mais on empile les layers en JS)
                                String groupName = "pattern_" + polygonId + "_group";
                                patternJS.append(String.format("const %s = L.layerGroup([%s]);\n",
                                        groupName, String.join(", ", patternNames)));
                                patternRef = groupName;
                            }
                        }
                    }

                    if (usePattern) {
                        writer.write(patternJS.toString());
                        writer.write(String.format(Locale.ENGLISH,
                                "const %s = L.polygon([\n  %s\n], {\n" +
                                        "  color: \"%s\",\n" +
                                        "  weight: %f,\n" +
                                        "  fillPattern: %s\n" +
                                        "}).addTo(map).bindPopup(\"%s\");\n",
                                polygonId, coordString, color, weight, patternRef, popupContent));
                    } else {
                        writer.write(String.format(Locale.ENGLISH,
                                "const %s = L.polygon([\n  %s\n], {\n" +
                                        "  color: \"%s\",\n" +
                                        "  weight: %f,\n" +
                                        "  fillColor: \"%s\",\n" +
                                        "  fillOpacity: %f\n" +
                                        "}).addTo(map).bindPopup(\"%s\");\n",
                                polygonId, coordString, color, weight, fillColor, fillOpacity, popupContent));
                    }

                    if (hasAdditionalContent) {
                        writer.write(generatePopupToggleJS(polygonId));
                    }

                    writer.newLine();
                } else {
                    System.err.println("Unsupported geometry type: " + type);
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
