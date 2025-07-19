package org.pingouinfini.geojson;

public class Point {
    private final Double latitude;
    private final Double longitude;
    private String name;
    private String description;
    private String icon;
    private String filename;
    private DisplayLayer displayLayer;

    public Point build() {
        return new Point(latitude, longitude, name, description, icon, filename, displayLayer);
    }

    public Point(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Point(Double latitude, Double longitude, String name, String description) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.description = description;
    }

    public Point(Double latitude, Double longitude, String name, String description, String icon, String filename, DisplayLayer displayLayer) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.filename = filename;
        this.displayLayer = displayLayer;
    }

    public Point name(String name) {
        this.name = name;
        return this;
    }

    public Point description(String description) {
        this.description = description;
        return this;
    }

    public Point icon(String icon) {
        this.icon = icon;
        return this;
    }

    public Point filename(String filename) {
        this.filename = filename;
        return this;
    }

    public Point displayLayer(DisplayLayer displayLayer) {
        this.displayLayer = displayLayer;
        return this;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public String getFilename() {
        return filename;
    }

    public DisplayLayer getDisplayLayer() {
        return displayLayer;
    }
}
