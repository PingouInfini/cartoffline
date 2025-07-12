package org.pingouinfini;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.pingouinfini.geojson.Coordonnee;
import org.pingouinfini.geojson.Point;
import org.pingouinfini.geojson.Polygon;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GeoJsonExporter {

    public static void exportGeoJson(List<Point> points, List<Polygon> polygons, String filename) throws IOException {
        GeometryFactory geometryFactory = new GeometryFactory();

        // Schéma commun avec tous les attributs potentiels
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("GeoJsonFeature");
        typeBuilder.add("geometry", Geometry.class);
        typeBuilder.add("name", String.class);
        typeBuilder.add("description", String.class);
        typeBuilder.add("icon", String.class);
        typeBuilder.add("filename", String.class);
        typeBuilder.add("color", String.class);
        typeBuilder.add("weight", Double.class);
        typeBuilder.add("fillColor", String.class);
        typeBuilder.add("fillOpacity", Double.class);
        SimpleFeatureType featureType = typeBuilder.buildFeatureType();

        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();

        // Ajout des points
        for (Point point : points) {
            Coordinate coord = new Coordinate(point.getLongitude(), point.getLatitude());
            org.locationtech.jts.geom.Point jtsPoint = geometryFactory.createPoint(coord);

            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
            featureBuilder.add(jtsPoint);                        // geometry
            featureBuilder.add(point.getName());                 // name
            featureBuilder.add(point.getDescription());          // description
            featureBuilder.add(point.getIcon());                 // icon
            featureBuilder.add(point.getFilename());             // filename
            featureBuilder.add(null);                            // color
            featureBuilder.add(null);                            // weight
            featureBuilder.add(null);                            // fillColor
            featureBuilder.add(null);                            // fillOpacity

            SimpleFeature feature = featureBuilder.buildFeature(null);
            featureCollection.add(feature);
        }

        // Ajout des polygones
        for (Polygon polygon : polygons) {
            List<Coordonnee> coords = polygon.getCoordinateArray();
            Coordinate[] jtsCoords = coords.stream()
                    .map(c -> new Coordinate(c.getLongitude(), c.getLatitude()))
                    .toArray(Coordinate[]::new);

            // Fermeture automatique du polygone si nécessaire
            if (!jtsCoords[0].equals2D(jtsCoords[jtsCoords.length - 1])) {
                Coordinate[] closedCoords = new Coordinate[jtsCoords.length + 1];
                System.arraycopy(jtsCoords, 0, closedCoords, 0, jtsCoords.length);
                closedCoords[closedCoords.length - 1] = jtsCoords[0];
                jtsCoords = closedCoords;
            }

            LinearRing shell = geometryFactory.createLinearRing(jtsCoords);
            org.locationtech.jts.geom.Polygon jtsPolygon = geometryFactory.createPolygon(shell);

            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
            featureBuilder.add(jtsPolygon);                          // geometry
            featureBuilder.add(polygon.getName());                   // name
            featureBuilder.add(polygon.getDescription());            // description
            featureBuilder.add(polygon.getIcon());                   // icon
            featureBuilder.add(polygon.getFilename());               // filename
            featureBuilder.add(polygon.getColor());                  // color
            featureBuilder.add(polygon.getWeight());                 // weight
            featureBuilder.add(polygon.getFillColor());              // fillColor
            featureBuilder.add(polygon.getFillOpacity());            // fillOpacity

            SimpleFeature feature = featureBuilder.buildFeature(null);
            featureCollection.add(feature);
        }

        // Écriture dans le fichier GeoJSON
        try (FileWriter writer = new FileWriter(filename)) {
            FeatureJSON fjson = new FeatureJSON();
            fjson.writeFeatureCollection(featureCollection, writer);
        }
    }
}