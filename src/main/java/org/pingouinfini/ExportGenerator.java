package org.pingouinfini;

import org.pingouinfini.geojson.*;
import org.pingouinfini.geojson.Point;
import org.pingouinfini.geojson.Polygon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;

public class ExportGenerator {

    public static void saveIcon(Image image, Path outputDirectory, String fileName) throws IOException {
        saveImageInternal(image, outputDirectory.resolve(Paths.get("images", "marker")), fileName);
    }

    public static void saveImage(Image image, Path outputDirectory, String fileName) throws IOException {
        saveImageInternal(image, outputDirectory.resolve(Paths.get("images", "illustration")), fileName);
    }

    public static void saveImageInternal(Image image, Path outputDirectory, String fileName) throws IOException {
        Files.createDirectories(outputDirectory);

        // Convertit Image en BufferedImage si nécessaire
        BufferedImage bufferedImage;
        if (image instanceof BufferedImage) {
            bufferedImage = (BufferedImage) image;
        } else {
            // Utiliser ARGB si PNG pour conserver la transparence
            boolean isPng = fileName.toLowerCase().endsWith(".png");
            int imageType = isPng ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            bufferedImage = new BufferedImage(
                    image.getWidth(null),
                    image.getHeight(null),
                    imageType
            );
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
        }

        // Déduction automatique du format à partir de l'extension du fichier
        String format = getFileExtension(fileName);
        Path outputPath = outputDirectory.resolve(fileName);
        ImageIO.write(bufferedImage, format, outputPath.toFile());
    }

    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        } else {
            return "png"; // par défaut, ou lève une exception si tu préfères
        }
    }

    public static Feature createPointFeature(Point point) {
        List<Double> pointCoords = Arrays.asList(point.getLongitude(), point.getLatitude()); // [lon, lat]
        Map<String, Object> pointProps = new HashMap<>();
        Optional.ofNullable(point.getName()).ifPresent(name -> pointProps.put("name", name));
        Optional.ofNullable(point.getDescription()).ifPresent(description -> pointProps.put("description", description));
        Optional.ofNullable(point.getIcon()).ifPresent(icon -> pointProps.put("icon", icon));
        Optional.ofNullable(point.getFilename()).ifPresent(imageFilename -> pointProps.put("imageFilename", imageFilename));
        Optional.ofNullable(point.getDisplayLayer()).ifPresent(displayLayer -> pointProps.put("displayLayer", displayLayer));

        Geometry pointGeometry = new Geometry("Point", pointCoords);

        return new Feature(pointGeometry, pointProps);
    }


    public static Feature createPolygonFeature(Polygon polygon) {
        List<List<Coordonnee>> polygonCoords = Arrays.asList(polygon.getCoordinateArray());
        Map<String, Object> polygonProps = new HashMap<>();
        Optional.ofNullable(polygon.getName()).ifPresent(name -> polygonProps.put("name", name));
        Optional.ofNullable(polygon.getDescription()).ifPresent(description -> polygonProps.put("description", description));
        Optional.ofNullable(polygon.getColor()).ifPresent(color -> polygonProps.put("color", color));
        Optional.ofNullable(polygon.getLineStyle()).ifPresent(lineStyle -> polygonProps.put("lineStyle", lineStyle));
        Optional.ofNullable(polygon.getWeight()).ifPresent(weight -> polygonProps.put("weight", weight));
        Optional.ofNullable(polygon.getFillPattern()).ifPresent(fillPattern -> polygonProps.put("fillPattern", fillPattern));
        Optional.ofNullable(polygon.getFillColor()).ifPresent(fillColor -> polygonProps.put("fillColor", fillColor));
        Optional.ofNullable(polygon.getFillOpacity()).ifPresent(fillOpacity -> polygonProps.put("fillOpacity", fillOpacity));
        Optional.ofNullable(polygon.getFilename()).ifPresent(imageFilename -> polygonProps.put("imageFilename", imageFilename));
        Optional.ofNullable(polygon.getDisplayLayer()).ifPresent(displayLayer -> polygonProps.put("displayLayer", displayLayer));

        Geometry polygonGeometry = new Geometry("Polygon", polygonCoords);
        return new Feature(polygonGeometry, polygonProps);
    }

    public static Feature createLineFeature(Line line) {
        List<List<Coordonnee>> lineCoords = Arrays.asList(line.getCoordinateArray());
        Map<String, Object> lineProps = new HashMap<>();
        Optional.ofNullable(line.getColor()).ifPresent(color -> lineProps.put("color", color));
        Optional.ofNullable(line.getName()).ifPresent(name -> lineProps.put("name", name));
        Optional.ofNullable(line.getLineStyle()).ifPresent(lineStyle -> lineProps.put("lineStyle", lineStyle));
        Optional.ofNullable(line.getWeight()).ifPresent(weight -> lineProps.put("weight", weight));
        Optional.ofNullable(line.getArrowStyle()).ifPresent(arrowStyle -> lineProps.put("arrowStyle", arrowStyle));
        Optional.ofNullable(line.getDisplayLayer()).ifPresent(displayLayer -> lineProps.put("displayLayer", displayLayer));

        Geometry lineGeometry = new Geometry("Line", lineCoords);
        return new Feature(lineGeometry, lineProps);
    }


    public static void generateNewExport(Path outputDirectory) throws IOException {
        // Supprimer le répertoire s'il existe
        if (Files.exists(outputDirectory)) {
            deleteDirectoryRecursively(outputDirectory);
        }

        // Chemin du dossier template
        Path templateDir = Paths.get("src/main/resources/template");

        // Copier récursivement le dossier template vers outputDirectory
        copyDirectory(templateDir, outputDirectory);
    }

    private static void deleteDirectoryRecursively(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
