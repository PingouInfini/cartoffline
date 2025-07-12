package org.pingouinfini;

import gov.nasa.worldwind.geom.LatLon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TileDownloader {
    private static final Logger LOGGER = Logger.getLogger(TileDownloader.class.getName());


    private static final String GEOSERVER_IP_PORT = "192.168.20.100:8082";
    private static final String WORKSPACE = "SAER";

    private static final String GEOSERVER_ADMIN = "admin";
    private static final String GEOSERVER_PASS = "geoserver";

    private static final String PROJECTION = "WebMercatorQuad";
    private static final String URL_GEOSERVER = "http://" + GEOSERVER_IP_PORT + "/geoserver";
    private static final String REST_LAYERGROUPS_URL = String.format("%s/rest/workspaces/%s/layergroups", URL_GEOSERVER, WORKSPACE);
    private static final String REST_LAYER_URL_TEMPLATE = String.format("%s/gwc/rest/layers/%%s.xml", URL_GEOSERVER);
    private static final String REST_DELETE_LAYERGROUP_URL_TEMPLATE = String.format("%s/rest/workspaces/%s/layergroups/%%s", URL_GEOSERVER,
            WORKSPACE);
    private static final String GET_TILE_URL_TEMPLATE = String.format("%s/gwc/service/tms/1.0.0/%%s@%s@png/%%d/%%d/%%d.png", URL_GEOSERVER,
            PROJECTION);
    private static final String INFO_LAYER_URL_TEMPLATE = String.format("%s/gwc/service/tms/1.0.0/%%s@%s@png", URL_GEOSERVER, PROJECTION);

    private static final int NB_CONNEXION_THREADS = 10;
    private static final int TILE_SIZE = 256;
    private static final int MIN_PIXEL_RESOLUTION = 1080; // La résolution minimale par défaut de la bounding box (1080x720p) si un niveau
    // de zoom maximum n'a pas été donné

    public static enum ZoomLevel {
        LOW(0.5),
        MEDIUM(1.0),
        HIGH(2.0),
        VERY_HIGH(4.0);

        private final double multiplier;

        ZoomLevel(final double multiplier) {
            this.multiplier = multiplier;
        }

        public double getMultiplier() {
            return this.multiplier;
        }
    }

    private static Point2D latlonToTileIndexes(final LatLon latLon, final int zoomLevel) throws Exception {
        final int n = 1 << zoomLevel; // 2^zoomLevel
        double xTile, yTile;

        switch (PROJECTION) {
            case "EPSG:3857":
            case "WebMercatorQuad":
            case "EPSG:900913":
                final double latRad = latLon.getLatitude().getRadians();
                xTile = (latLon.getLongitude().getDegrees() + 180) / 360 * n;
                yTile = (1 + Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2 * n;
                break;
            case "EPSG:4326":
                xTile = (latLon.getLongitude().getDegrees() + 180) / 180 * n;
                yTile = (90 + latLon.getLatitude().getDegrees()) / 180 * n;
                break;
            default:
                throw new Exception("Projection non supportée."); //$NON-NLS-1$
        }
        return new Point2D.Double(xTile, yTile);
    }

    private static int[] bboxToTileRange(final LatLon latLonMin, final LatLon latLonMax, final int zoomLevel)
            throws Exception {
        final Point2D tile1 = latlonToTileIndexes(latLonMin, zoomLevel);
        final Point2D tile2 = latlonToTileIndexes(latLonMax, zoomLevel);

        final boolean invertLon = latLonMax.getLongitude().getDegrees() - latLonMin.getLongitude().getDegrees() > 180;
        final double xMin = invertLon ? tile2.getX() : tile1.getX();
        final double xMax = invertLon ? tile1.getX() : tile2.getX();
        final double yMin = tile1.getY();
        final double yMax = tile2.getY();

        final int n = PROJECTION != "EPSG:4326" ? 1 << zoomLevel : 2 << zoomLevel;
        final int pixelSpanHeight = (int) ((yMax - yMin) * TILE_SIZE);
        final int pixelSpanWidth = (int) (((xMax - xMin) % n + n) % n * TILE_SIZE);
        final int tileNb = (1 + (((int) xMax - (int) xMin) % n + n) % n) * (1 + (int) yMax - (int) yMin);

        return new int[]{(int) xMin, (int) xMax, (int) yMin, (int) yMax, pixelSpanWidth, pixelSpanHeight, tileNb};
    }

    private static Element createTextElement(final Document doc, final String tagName, final String lonMin) {
        final Element element = doc.createElement(tagName);
        element.setTextContent(lonMin);
        return element;
    }

    private static boolean updateLayerGroup(final String layerId) {
        boolean updated = false;
        try {
            // Récupère les paramètres du layer
            final URL url = new URL(String.format(REST_LAYER_URL_TEMPLATE, layerId));
            final HttpURLConnection getCon = (HttpURLConnection) url.openConnection();
            getCon.setRequestMethod("GET");
            getCon.setRequestProperty("Accept", "application/xml");
            getCon.setRequestProperty("Authorization",
                    "Basic " + new String(Base64.getEncoder().encodeToString((GEOSERVER_ADMIN + ":" + GEOSERVER_PASS).getBytes("UTF-8"))));
            getCon.setDoInput(true);

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(getCon.getInputStream());
            getCon.disconnect();

            // Modifie les paramètres du layer
            final Element gridSubsets = (Element) doc.getElementsByTagName("gridSubsets").item(0);
            final Element gridSubset = doc.createElement("gridSubset");
            gridSubset.appendChild(createTextElement(doc, "gridSetName", PROJECTION));
            gridSubsets.appendChild(gridSubset);
            doc.getElementsByTagName("inMemoryCached").item(0).setTextContent("false");
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            final StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            final String xmlString = writer.toString();

            // Envoie les paramètres du layer modifiés
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/xml");
            con.setRequestProperty("Authorization",
                    "Basic " + new String(Base64.getEncoder().encodeToString((GEOSERVER_ADMIN + ":" + GEOSERVER_PASS).getBytes("UTF-8"))));
            con.setDoOutput(true);
            final byte[] output = xmlString.getBytes(StandardCharsets.UTF_8);
            con.setFixedLengthStreamingMode(output.length);

            try (final OutputStream os = con.getOutputStream()) {
                os.write(output);
                updated = con.getResponseCode() == HttpURLConnection.HTTP_OK;
            }
            con.disconnect();
            return updated;
        } catch (ParserConfigurationException | IOException | TransformerException | SAXException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la modification du LayerGroup:", e);
        }
        return updated;
    }

    private static boolean createLayerGroup(final String layerGroupName, final List<String> layers, final LatLon latLonMin,
                                            final LatLon latLonMax) {
        boolean created = false;
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.newDocument();

            // LayerGroup
            final Element layerGroup = doc.createElement("layerGroup");
            layerGroup.appendChild(createTextElement(doc, "name", layerGroupName));
            layerGroup.appendChild(createTextElement(doc, "mode", "SINGLE"));
            layerGroup.appendChild(createTextElement(doc, "enabled", "true"));
            doc.appendChild(layerGroup);

            // Workspace
            final Element workspace = doc.createElement("workspace");
            workspace.appendChild(createTextElement(doc, "name", WORKSPACE));
            layerGroup.appendChild(workspace);

            // Layers
            final Element layersEl = doc.createElement("layers");
            for (final String layerName : layers) {
                layersEl.appendChild(createTextElement(doc, "layer", layerName));
            }
            layerGroup.appendChild(layersEl);

            // Bounds
            final Element bounds = doc.createElement("bounds");
            final double minx = latLonMin.getLongitude().getDegrees();
            final double maxx = latLonMax.getLongitude().getDegrees();
            if (maxx - minx > 180) {
                bounds.appendChild(createTextElement(doc, "minx", "-180.0"));
                bounds.appendChild(createTextElement(doc, "maxx", "180.0"));
            } else {
                bounds.appendChild(createTextElement(doc, "minx", Double.toString(minx)));
                bounds.appendChild(createTextElement(doc, "maxx", Double.toString(maxx)));
            }
            bounds.appendChild(createTextElement(doc, "miny", Double.toString(latLonMin.getLatitude().getDegrees())));
            bounds.appendChild(createTextElement(doc, "maxy", Double.toString(latLonMax.getLatitude().getDegrees())));
            bounds.appendChild(createTextElement(doc, "crs", "EPSG:4326"));
            layerGroup.appendChild(bounds);

            // Metadata
            final Element metadata = doc.createElement("metadata");
            final Element cached = createTextElement(doc, "entry", "false");
            cached.setAttribute("key", "cached");
            final Element cachingEnabled = createTextElement(doc, "entry", "false");
            cachingEnabled.setAttribute("key", "cachingEnabled");
            metadata.appendChild(cached);
            metadata.appendChild(cachingEnabled);
            final Element gridSubsetsEntry = doc.createElement("entry");
            gridSubsetsEntry.setAttribute("key", "gridSubsets");
            final Element gridSubsets = doc.createElement("gridSubsets");
            final Element gridSubset = doc.createElement("gridSubset");
            gridSubset.appendChild(createTextElement(doc, "gridSetName", PROJECTION));
            gridSubsets.appendChild(gridSubset);
            gridSubsetsEntry.appendChild(gridSubsets);
            metadata.appendChild(gridSubsetsEntry);
            layerGroup.appendChild(metadata);

            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            final StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            final String xmlString = writer.toString();

            final URL url = new URL(REST_LAYERGROUPS_URL);
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/xml");
            con.setRequestProperty("Authorization",
                    "Basic " + new String(Base64.getEncoder().encodeToString((GEOSERVER_ADMIN + ":" + GEOSERVER_PASS).getBytes("UTF-8"))));
            con.setDoOutput(true);
            final byte[] output = xmlString.getBytes(StandardCharsets.UTF_8);
            con.setFixedLengthStreamingMode(output.length);

            try (final OutputStream os = con.getOutputStream()) {
                os.write(output);
                created = con.getResponseCode() == HttpURLConnection.HTTP_CREATED;
                if (created) {
                    updateLayerGroup(WORKSPACE + ':' + layerGroupName);
                }
            }
            con.disconnect();
            return created;
        } catch (ParserConfigurationException | IOException | TransformerException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création du LayerGroup:", e);
        }
        return created;
    }

    private static boolean deleteLayerGroup(final String layerGroupName) {
        boolean deleted = false;
        try {
            final URL url = new URL(String.format(REST_DELETE_LAYERGROUP_URL_TEMPLATE, layerGroupName));
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");
            con.setRequestProperty("Authorization",
                    "Basic " + new String(Base64.getEncoder().encodeToString((GEOSERVER_ADMIN + ":" + GEOSERVER_PASS).getBytes("UTF-8"))));

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                deleted = true;
            }
            con.disconnect();
            return deleted;
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression du LayerGroup:", e);
        }
        return deleted;
    }

    private static ArrayList<Integer> getAvailableZoomLevels(final String layerId) {
        final String urlStr = String.format(INFO_LAYER_URL_TEMPLATE, layerId);
        final ArrayList<Integer> zoomLevels = new ArrayList<>();
        try (InputStream in = new URL(urlStr).openStream()) {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(in);

            doc.getDocumentElement().normalize();

            final NodeList tileSetList = doc.getElementsByTagName("TileSet");

            for (int i = 0; i < tileSetList.getLength(); i++) {
                final Element tileSet = (Element) tileSetList.item(i);
                zoomLevels.add(Integer.parseInt(tileSet.getAttribute("order")));
            }
        } catch (final SAXException | ParserConfigurationException | IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de récupération des niveaux de zoom du layer " + layerId + ":", e);
        }

        return zoomLevels;
    }

    private static boolean respectsPixelResolution(final int pixelSpanWidth, final int pixelSpanHeight, final ZoomLevel zoomLevel) {
        return pixelSpanHeight >= MIN_PIXEL_RESOLUTION * zoomLevel.getMultiplier()
                && pixelSpanWidth >= MIN_PIXEL_RESOLUTION * zoomLevel.getMultiplier() / 0.5625;
    }

    public static void downloadTiles(final LatLon latLonMin, final LatLon latLonMax, final List<String> layers, final Path outputDirectory) {
        downloadTiles(latLonMin, latLonMax, ZoomLevel.VERY_HIGH, layers, outputDirectory);
    }

    public static void downloadTiles(final LatLon latLonMin, final LatLon latLonMax, final ZoomLevel zoomLevel, final List<String> layers,
                                     final Path outputDirectory) {
        final String layerGroupName = "temp_" + UUID.randomUUID().toString();

        Collections.reverse(layers);

        createLayerGroup(layerGroupName, layers, latLonMin, latLonMax);

        final String layerId = WORKSPACE + ':' + layerGroupName;

        final ExecutorService executor = Executors.newFixedThreadPool(NB_CONNEXION_THREADS);
        final AtomicInteger completedTasks = new AtomicInteger(0);

        final AtomicInteger nbTasks = new AtomicInteger(0);
        final AtomicBoolean lastZoom = new AtomicBoolean(false);

        try {
            final List<Integer> availableZoomLevels = getAvailableZoomLevels(layerId);

            // Parcourir toutes les niveaux de zoom de la carte
            for (final int zoom : availableZoomLevels) {
                final int[] tileRange = bboxToTileRange(latLonMin, latLonMax, zoom);
                final int colStart = tileRange[0];
                final int colEnd = tileRange[1];
                final int rowStart = tileRange[2];
                final int rowEnd = tileRange[3];
                final int pixelSpanHeight = tileRange[4];
                final int pixelSpanWidth = tileRange[5];
                final boolean respectsPixelResolution = respectsPixelResolution(pixelSpanWidth, pixelSpanHeight, zoomLevel);

                final boolean isLastZoomLevel = respectsPixelResolution
                        || availableZoomLevels.indexOf(zoom) == availableZoomLevels.size() - 1;

                final int n = nbTasks.addAndGet(tileRange[6]);
                lastZoom.set(isLastZoomLevel);

                // // Calculer le nombre maximum de colonnes de tuiles pour ce niveau de zoom
                final int tileColMaxNb = PROJECTION != "EPSG:4326" ? 1 << zoom : 2 << zoom;

                // Parcourir toutes les colonnes de la bounding box pour ce niveau de zoom
                for (int tileCol = colStart; colStart <= colEnd ? tileCol <= colEnd : tileCol <= colEnd + tileColMaxNb; tileCol++) {
                    // Parcourir toutes les lignes de la bounding box pour ce niveau de zoom
                    for (int tileRow = rowStart; tileRow <= rowEnd; tileRow++) {
                        final int currentTileCol = tileCol % tileColMaxNb;
                        final int currentTileRow = tileRow;

                        executor.submit(() -> {
                            final String urlStr = String.format(GET_TILE_URL_TEMPLATE, layerId, zoom, currentTileCol, currentTileRow);
                            try {
                                final URL url = new URL(urlStr);
                                final Path tilePath = outputDirectory.resolve(Paths.get("cache-carto", String.valueOf(zoom),
                                        String.valueOf(currentTileCol), currentTileRow + ".png"));
                                try (InputStream in = url.openStream()) {
                                    Files.createDirectories(tilePath.getParent());
                                    Files.copy(in, tilePath, StandardCopyOption.REPLACE_EXISTING);
                                    final int current = completedTasks.incrementAndGet();
                                    final int total = nbTasks.get();

                                    // Fin de l'export
                                    if (lastZoom.get() && current >= total) {
                                        executor.shutdown();
                                        deleteLayerGroup(layerGroupName);
                                    }
                                } catch (final Throwable e) {
                                    LOGGER.log(Level.SEVERE, "Erreur au niveau de zoom " + zoom + ", tuile (" + currentTileCol + "," //$NON-NLS-1$
                                            + currentTileRow + ") : ", e); //$NON-NLS-1$
                                    // On réessaye une deuxième fois
                                    executor.submit(() -> {
                                        try {
                                            final URL subUrl = new URL(urlStr);
                                            final InputStream subIn = subUrl.openStream();
                                            Files.createDirectories(tilePath.getParent());
                                            Files.copy(subIn, tilePath, StandardCopyOption.REPLACE_EXISTING);
                                            completedTasks.getAndIncrement();
                                            subIn.close();
                                        } catch (final IOException e1) {
                                            LOGGER.log(Level.SEVERE,
                                                    "Erreur n°2 au niveau de zoom " + zoom + ", tuile (" + currentTileCol + "," //$NON-NLS-1$
                                                            + currentTileRow + ") : ", //$NON-NLS-1$
                                                    e1);
                                            nbTasks.getAndDecrement();
                                        } finally {
                                            final int current = completedTasks.get();
                                            final int total = nbTasks.get();
                                            // Fin de l'export
                                            if (lastZoom.get() && current >= total) {
                                                executor.shutdown();
                                                deleteLayerGroup(layerGroupName);
                                            }
                                        }
                                    });
                                }
                            } catch (final MalformedURLException e1) {
                                LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                                final int total = nbTasks.decrementAndGet();
                                final int current = completedTasks.get();
                                // Fin de l'export
                                if (lastZoom.get() && current >= total) {
                                    executor.shutdown();
                                    deleteLayerGroup(layerGroupName);
                                }
                            }
                        });
                    }
                }
                if (isLastZoomLevel) {
                    break;
                }
            }
        } catch (final Exception e1) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'export de tuiles : ", e1); //$NON-NLS-1$
        }
    }
}