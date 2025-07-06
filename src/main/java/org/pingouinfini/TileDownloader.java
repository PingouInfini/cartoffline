package org.pingouinfini;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.*;

public class TileDownloader {

    private static final String GEOSERVER_URL = "10.10.10.10:8082";
    private static final String PROJECTION = "WebMercatorQuad";
    private static final String TILE_URL_TEMPLATE = String.format(
            "http://%s/geoserver/gwc/service/tms/1.0.0/%%s@%s@png/%%d/%%d/%%d.png",
            GEOSERVER_URL, PROJECTION
    );

    public static void downloadTiles(int zoomMin, int zoomMax, String layerId, Path outputDirectory) {
        // FIXME: reprendre le code

        ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, zoomMax - zoomMin + 1));
        try {
            for (int zoom = zoomMin; zoom <= zoomMax; zoom++) {
                final int zoomLevel = zoom;

                // Calculer le nombre maximum de tuiles pour ce niveau de zoom
                int maxTileCoord = (int) Math.pow(2, zoomLevel) - 1; // max coord est 2^Z - 1

                // Parcourir toutes les colonnes pour ce niveau de zoom
                for (int tileCol = 0; tileCol <= maxTileCoord; tileCol++) {
                    // Parcourir toutes les lignes pour ce niveau de zoom
                    for (int tileRow = 0; tileRow <= maxTileCoord; tileRow++) {
                        final int currentTileCol = tileCol;
                        final int currentTileRow = tileRow;

                        executor.submit(() -> {
                            try {
                                String urlStr = String.format(TILE_URL_TEMPLATE, layerId, zoomLevel, currentTileCol, currentTileRow);
                                URL url = new URL(urlStr);

                                Path tilePath = outputDirectory.resolve(Paths.get("cache-carto",
                                        String.valueOf(zoomLevel),
                                        String.valueOf(currentTileCol),
                                        currentTileRow + ".png"));

                                Files.createDirectories(tilePath.getParent());

                                try (InputStream in = url.openStream()) {
                                    Files.copy(in, tilePath, StandardCopyOption.REPLACE_EXISTING);
                                    // DEBUG
                                    // System.out.println("Tuile téléchargée : " + tilePath);
                                }

                            } catch (IOException e) {
                                System.err.println("Erreur au niveau de zoom " + zoomLevel + ", tuile (" + currentTileCol + "," + currentTileRow + ") : " + e.getMessage());
                            }
                        });
                    }
                }
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}